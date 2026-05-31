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
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiCoachService {

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
    FeedbackItemEntity item = loadOwnedItem(userId, itemId);
    if (item.getAiExplanation() != null) {
      return new ExplainResponse(item.getAiExplanation());
    }
    String explanation = callProvider(coachPrompt(item));
    item.setAiExplanation(explanation);
    feedbackItemRepository.save(item);
    return new ExplainResponse(explanation);
  }

  public AlternativesResponse alternatives(UUID userId, String slug) {
    ChallengeEntity challenge = findChallenge(slug);
    boolean passed =
        submissionRepository.findAll().stream()
            .anyMatch(
                s ->
                    s.getUserId().equals(userId)
                        && s.getChallengeId().equals(challenge.getId())
                        && reportRepository
                            .findBySubmissionId(s.getId())
                            .map(r -> !r.isBlocked())
                            .orElse(false));
    if (!passed) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Alternatives require a passing submission");
    }
    String text =
        callProvider(
            """
            You are a coding coach. The learner passed challenge '%s' in Java.
            Suggest 2–3 alternative approaches or refinements (trade-offs, complexity, readability).
            Do not provide a full replacement solution or complete code.
            """
                .formatted(slug)
                .trim());
    return new AlternativesResponse(text);
  }

  private FeedbackItemEntity loadOwnedItem(UUID userId, UUID itemId) {
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
    return item;
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

  private static String coachPrompt(FeedbackItemEntity item) {
    return """
        You are a supportive coding coach helping someone practice Java challenges.
        They are learning — analyze this automated feedback and help them improve.

        Category: %s
        Result: %s
        Feedback: %s

        Explain what this means in plain language, why it matters for practice, and give
        specific next steps. Do not paste a full solution; hints and small examples only.
        """
        .formatted(item.getCategory(), item.getStatus(), item.getMessage())
        .trim();
  }

  private static String jsonEscape(String value) {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  public record ExplainResponse(String explanation) {}

  public record AlternativesResponse(String alternatives) {}
}
