package com.codetraininglab.coach.application;

import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.domain.AiProvider;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.FeedbackItemEntity;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.SubmissionReportEntity;
import com.codetraininglab.platform.persistence.SubmissionReportRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiCoachService {

  private static final int CODE_PROMPT_LIMIT = 6000;
  private static final int TASK_SUMMARY_LIMIT = 900;

  private final FeedbackItemRepository feedbackItemRepository;
  private final SubmissionReportRepository reportRepository;
  private final SubmissionRepository submissionRepository;
  private final ChallengeRepository challengeRepository;
  private final CtlProperties properties;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  public AiCoachService(
      FeedbackItemRepository feedbackItemRepository,
      SubmissionReportRepository reportRepository,
      SubmissionRepository submissionRepository,
      ChallengeRepository challengeRepository,
      CtlProperties properties) {
    this.feedbackItemRepository = feedbackItemRepository;
    this.reportRepository = reportRepository;
    this.submissionRepository = submissionRepository;
    this.challengeRepository = challengeRepository;
    this.properties = properties;
  }

  @Transactional
  public ExplainResponse explain(UUID userId, UUID itemId) {
    OwnedFeedback ownedFeedback = loadOwnedItem(userId, itemId);
    FeedbackItemEntity item = ownedFeedback.item();
    if (item.getAiExplanation() != null) {
      return new ExplainResponse(item.getAiExplanation());
    }
    String explanation =
        callProvider(
            coachPrompt(item, ownedFeedback.challenge(), ownedFeedback.submission()));
    item.setAiExplanation(explanation);
    feedbackItemRepository.save(item);
    return new ExplainResponse(explanation);
  }

  /**
   * Generic "review my submission" callable from the on-demand feedback-actions API. Returns
   * coach text describing the submitted code in the context of its challenge; does not require a
   * passing submission, since the user explicitly asked for feedback.
   */
  public String reviewSubmission(UUID submissionId) {
    SubmissionEntity submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    ChallengeEntity challenge =
        challengeRepository
            .findById(submission.getChallengeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    String prompt =
        """
        You are a supportive coding coach reviewing a learner's submission for challenge '%s' (%s).

        Task summary:
        %s

        %s

        %s
        """
            .formatted(
                challenge.getSlug(),
                displayLanguage(challenge.getLanguage()),
                truncateForPrompt(challenge.getDescriptionMd(), TASK_SUMMARY_LIMIT),
                learnerSubmissionBlock(submission.getSolutionCode()),
                coachAnalysisInstructions(challenge.getLanguage()))
            .trim();
    return callProvider(prompt);
  }

  public AlternativesResponse alternatives(UUID userId, String slug) {
    ChallengeEntity challenge = findChallenge(slug);
    SubmissionEntity submission =
        findLatestPassingSubmission(userId, challenge.getId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.CONFLICT, "Alternatives require a passing submission"));
    String text =
        callProvider(
            """
            You are a coding coach. The learner passed challenge '%s' in %s with the submission below.

            Task summary:
            %s

            %s

            Suggest 2–3 alternative approaches or refinements compared to what they wrote
            (trade-offs, complexity, readability). Reference their code where helpful.

            %s
            """
                .formatted(
                    slug,
                    displayLanguage(challenge.getLanguage()),
                    truncateForPrompt(challenge.getDescriptionMd(), TASK_SUMMARY_LIMIT),
                    learnerSubmissionBlock(submission.getSolutionCode()),
                    coachAnalysisInstructions(challenge.getLanguage()))
                .trim());
    return new AlternativesResponse(text);
  }

  private Optional<SubmissionEntity> findLatestPassingSubmission(UUID userId, UUID challengeId) {
    return submissionRepository.findAll().stream()
        .filter(s -> s.getUserId().equals(userId) && s.getChallengeId().equals(challengeId))
        .filter(
            s ->
                reportRepository
                    .findBySubmissionId(s.getId())
                    .map(report -> !report.isBlocked())
                    .orElse(false))
        .max(Comparator.comparing(SubmissionEntity::getCreatedAt));
  }

  private OwnedFeedback loadOwnedItem(UUID userId, UUID itemId) {
    FeedbackItemEntity item =
        feedbackItemRepository
            .findById(itemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    SubmissionReportEntity report =
        reportRepository
            .findById(item.getReportId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    SubmissionEntity submission =
        submissionRepository
            .findById(report.getSubmissionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!submission.getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    ChallengeEntity challenge =
        challengeRepository
            .findById(submission.getChallengeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return new OwnedFeedback(item, challenge, submission);
  }

  private ChallengeEntity findChallenge(String slug) {
    return challengeRepository
        .findBySlug(slug)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  private String callProvider(String prompt) {
    AiProvider provider = AiProvider.fromConfig(properties.aiProvider());
    if (provider == AiProvider.OLLAMA) {
      return callOllama(prompt);
    }
    if (properties.openrouterApiKey() == null || properties.openrouterApiKey().isBlank()) {
      return "AI provider is not configured. Set OPENROUTER_API_KEY or use AI_PROVIDER="
          + AiProvider.OLLAMA.configValue()
          + ".";
    }
    return callOpenRouter(prompt);
  }

  private String callOpenRouter(String prompt) {
    try {
      String body =
          """
          {"model":"%s","messages":[{"role":"user","content":%s}]}
          """
              .formatted(properties.openrouterModel(), jsonEscape(prompt));
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(AiProviderUrls.OPENROUTER_CHAT_COMPLETIONS))
              .timeout(Duration.ofSeconds(60))
              .header("Authorization", "Bearer " + properties.openrouterApiKey())
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(body))
              .build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 400) {
        return "AI request failed with status " + response.statusCode();
      }
      return AiResponseParser.parse(AiProvider.OPENROUTER, response.body());
    } catch (Exception e) {
      return "AI request failed: " + e.getMessage();
    }
  }

  private String callOllama(String prompt) {
    try {
      String body =
          """
          {"model":"%s","prompt":%s,"stream":false}
          """
              .formatted(properties.ollamaModel(), jsonEscape(prompt));
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(properties.ollamaBaseUrl() + "/api/generate"))
              .timeout(Duration.ofSeconds(60))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(body))
              .build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 400) {
        return "AI request failed with status " + response.statusCode();
      }
      return AiResponseParser.parse(AiProvider.OLLAMA, response.body());
    } catch (Exception e) {
      return "AI request failed: " + e.getMessage();
    }
  }

  static String coachPrompt(
      FeedbackItemEntity item, ChallengeEntity challenge, SubmissionEntity submission) {
    return """
        You are a supportive coding coach for %s practice.

        Challenge: %s
        Task summary:
        %s

        Automated check:
        - Category: %s
        - Result: %s
        - Message: %s

        %s

        Relate your answer to this learner's actual code and the automated check above.
        %s
        """
        .formatted(
            displayLanguage(challenge.getLanguage()),
            challenge.getSlug(),
            truncateForPrompt(challenge.getDescriptionMd(), TASK_SUMMARY_LIMIT),
            item.getCategory(),
            item.getStatus(),
            item.getMessage(),
            learnerSubmissionBlock(submission.getSolutionCode()),
            coachAnalysisInstructions(challenge.getLanguage()))
        .trim();
  }

  static String learnerSubmissionBlock(String solutionCode) {
    return """
        Learner's current submission:
        ```
        %s
        ```
        """
        .formatted(truncateForPrompt(solutionCode, CODE_PROMPT_LIMIT))
        .strip();
  }

  static String coachAnalysisInstructions(String language) {
    String lang = displayLanguage(language);
    return """
        Analyze the learner's submission (not a generic tutorial). Structure your reply as:
        1. **What your code does** — brief walkthrough tied to their implementation
        2. **What to improve** — specific hints linked to the feedback or gaps you see
        3. **Alternatives** — 1–2 other patterns or refactors (trade-offs, complexity) with short samples

        Rules:
        - Reference their approach (method names, data structures, control flow) before suggesting changes.
        - Give actionable hints and partial steps, not a complete solution for this challenge.
        %s
        - Prefer %s for sample snippets unless another language fits better.
        """
        .formatted(codeSampleGuidance(language), lang)
        .strip();
  }

  static String codeSampleGuidance(String language) {
    return """
        - Use Markdown fenced code blocks (``` + language tag) with 5–15 lines per pattern example.
        - Use inline `code` for identifiers and one-liners.
        - Samples must illustrate a technique (e.g. early return, map grouping, two pointers) — not the full answer.
        """
        .strip();
  }

  static String truncateForPrompt(String text, int limit) {
    if (text == null || text.isBlank()) {
      return "(not provided)";
    }
    return text.length() <= limit ? text : text.substring(0, limit) + "\n...[truncated]";
  }

  static String displayLanguage(String language) {
    if (language == null || language.isBlank()) {
      return "the target language";
    }
    return switch (language.trim().toLowerCase()) {
      case "cpp" -> "C++";
      case "csharp" -> "C#";
      case "node" -> "Node.js";
      case "typescript" -> "TypeScript";
      case "react" -> "React";
      case "vue" -> "Vue";
      case "angular" -> "Angular";
      default -> {
        String normalized = language.trim().toLowerCase();
        yield Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
      }
    };
  }

  private static String jsonEscape(String value) {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  public record ExplainResponse(String explanation) {}

  public record AlternativesResponse(String alternatives) {}

  private record OwnedFeedback(
      FeedbackItemEntity item, ChallengeEntity challenge, SubmissionEntity submission) {}
}
