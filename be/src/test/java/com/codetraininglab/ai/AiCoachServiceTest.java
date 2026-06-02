package com.codetraininglab.coach.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import com.codetraininglab.domain.FeedbackCategory;
import com.codetraininglab.domain.FeedbackStatus;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.FeedbackItemEntity;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionReportEntity;
import com.codetraininglab.platform.persistence.SubmissionReportRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.domain.SubmissionKind;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AiCoachServiceTest {

  @Mock private FeedbackItemRepository feedbackItemRepository;
  @Mock private SubmissionReportRepository reportRepository;
  @Mock private SubmissionRepository submissionRepository;
  @Mock private ChallengeRepository challengeRepository;

  private AiCoachService service;

  @BeforeEach
  void setUp() {
    var properties = CtlPropertiesTestFixtures.defaults();
    service =
        new AiCoachService(
            feedbackItemRepository,
            reportRepository,
            submissionRepository,
            challengeRepository,
            properties);
  }

  @Test
  void explainReturnsMessageWhenProviderNotConfigured() {
    UUID userId = UUID.randomUUID();
    UUID reportId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    UUID submissionId = UUID.randomUUID();
    UUID challengeId = UUID.randomUUID();
    FeedbackItemEntity item =
        new FeedbackItemEntity(
            itemId,
            reportId,
            FeedbackCategory.CORRECTNESS,
            FeedbackStatus.fail,
            "error",
            "failed",
            "id",
            Instant.EPOCH);
    when(feedbackItemRepository.findById(itemId)).thenReturn(Optional.of(item));
    when(reportRepository.findById(reportId))
        .thenReturn(
            Optional.of(
                new SubmissionReportEntity(reportId, submissionId, 1, "{}", true, Instant.EPOCH)));
    when(submissionRepository.findById(submissionId))
        .thenReturn(
            Optional.of(
                new SubmissionEntity(
                    submissionId,
                    userId,
                    challengeId,
                    UUID.randomUUID(),
                    SubmissionStatus.COMPLETED,
            com.codetraininglab.domain.SubmissionKind.SUBMIT,
                    "c",
                    null,
                    null,
                    Instant.EPOCH,
                    Instant.EPOCH)));
    when(challengeRepository.findById(challengeId))
        .thenReturn(
            Optional.of(
                new com.codetraininglab.platform.persistence.ChallengeEntity(
                    challengeId,
                    "slug",
                    "Title",
                    "desc",
                    "starter",
                    "{}",
                    "git",
                    "easy",
                    "java",
                    Instant.EPOCH,
                    Instant.EPOCH)));

    var response = service.explain(userId, itemId);
    assertThat(response.explanation()).contains("not configured");
  }

  @Test
  void explainReturnsCachedExplanationWithoutCallingProvider() {
    UUID userId = UUID.randomUUID();
    UUID reportId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    UUID submissionId = UUID.randomUUID();
    UUID challengeId = UUID.randomUUID();
    FeedbackItemEntity item =
        new FeedbackItemEntity(
            itemId,
            reportId,
            FeedbackCategory.CORRECTNESS,
            FeedbackStatus.fail,
            "error",
            "failed",
            "id",
            Instant.EPOCH);
    item.setAiExplanation("Cached explanation");
    when(feedbackItemRepository.findById(itemId)).thenReturn(Optional.of(item));
    when(reportRepository.findById(reportId))
        .thenReturn(
            Optional.of(
                new SubmissionReportEntity(reportId, submissionId, 1, "{}", true, Instant.EPOCH)));
    when(submissionRepository.findById(submissionId))
        .thenReturn(
            Optional.of(
                new SubmissionEntity(
                    submissionId,
                    userId,
                    challengeId,
                    UUID.randomUUID(),
                    SubmissionStatus.COMPLETED,
            com.codetraininglab.domain.SubmissionKind.SUBMIT,
                    "c",
                    null,
                    null,
                    Instant.EPOCH,
                    Instant.EPOCH)));
    when(challengeRepository.findById(challengeId))
        .thenReturn(
            Optional.of(
                new com.codetraininglab.platform.persistence.ChallengeEntity(
                    challengeId,
                    "slug",
                    "Title",
                    "desc",
                    "starter",
                    "{}",
                    "git",
                    "easy",
                    "java",
                    Instant.EPOCH,
                    Instant.EPOCH)));

    var response = service.explain(userId, itemId);

    assertThat(response.explanation()).isEqualTo("Cached explanation");
    verify(feedbackItemRepository).findById(itemId);
  }

  @Test
  void explainRejectsForeignSubmission() {
    UUID itemId = UUID.randomUUID();
    UUID reportId = UUID.randomUUID();
    UUID submissionId = UUID.randomUUID();
    FeedbackItemEntity item =
        new FeedbackItemEntity(
            itemId,
            reportId,
            FeedbackCategory.CORRECTNESS,
            FeedbackStatus.fail,
            "error",
            "failed",
            "id",
            Instant.EPOCH);
    when(feedbackItemRepository.findById(itemId)).thenReturn(Optional.of(item));
    when(reportRepository.findById(reportId))
        .thenReturn(
            Optional.of(
                new SubmissionReportEntity(reportId, submissionId, 1, "{}", true, Instant.EPOCH)));
    when(submissionRepository.findById(submissionId))
        .thenReturn(
            Optional.of(
                new SubmissionEntity(
                    submissionId,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    SubmissionStatus.COMPLETED,
            com.codetraininglab.domain.SubmissionKind.SUBMIT,
                    "c",
                    null,
                    null,
                    Instant.EPOCH,
                    Instant.EPOCH)));

    assertThatThrownBy(() -> service.explain(UUID.randomUUID(), itemId))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void alternativesUsesOllamaProviderWhenConfigured() {
    CtlProperties ollamaProperties =
        new CtlProperties(
            true,
            CtlPropertiesTestFixtures.defaults().jwtSecret(),
            24,
            "http://localhost:5173",
            "challenges",
            "runner",
            "",
            true,
            60,
            CtlPropertiesTestFixtures.TEST_LSP_IMAGES,
            5,
            24,
            "ollama",
            "",
            "model",
            "http://127.0.0.1:1",
            "qwen",
            false,
            false,
            false);
    AiCoachService ollamaService =
        new AiCoachService(
            feedbackItemRepository,
            reportRepository,
            submissionRepository,
            challengeRepository,
            ollamaProperties);
    UUID userId = UUID.randomUUID();
    UUID challengeId = UUID.randomUUID();
    UUID submissionId = UUID.randomUUID();
    when(challengeRepository.findBySlug("slug"))
        .thenReturn(
            Optional.of(
                new com.codetraininglab.platform.persistence.ChallengeEntity(
                    challengeId,
                    "slug",
                    "Title",
                    "desc",
                    "starter",
                    "{}",
                    "git",
                    "easy",
                    "python",
                    Instant.EPOCH,
                    Instant.EPOCH)));
    when(submissionRepository.findAll())
        .thenReturn(
            List.of(
                new SubmissionEntity(
                    submissionId,
                    userId,
                    challengeId,
                    UUID.randomUUID(),
                    SubmissionStatus.COMPLETED,
            com.codetraininglab.domain.SubmissionKind.SUBMIT,
                    "code",
                    null,
                    null,
                    Instant.EPOCH,
                    Instant.EPOCH)));
    when(reportRepository.findBySubmissionId(submissionId))
        .thenReturn(
            Optional.of(
                new SubmissionReportEntity(
                    UUID.randomUUID(), submissionId, 1, "{}", false, Instant.EPOCH)));

    var response = ollamaService.alternatives(userId, "slug");

    assertThat(response.alternatives()).contains("AI request failed");
  }

  @Test
  void reviewSubmissionReturnsCoachTextEvenWithoutPassingSubmission() {
    UUID submissionId = UUID.randomUUID();
    UUID challengeId = UUID.randomUUID();
    when(submissionRepository.findById(submissionId))
        .thenReturn(
            Optional.of(
                new SubmissionEntity(
                    submissionId,
                    UUID.randomUUID(),
                    challengeId,
                    UUID.randomUUID(),
                    SubmissionStatus.COMPLETED,
            com.codetraininglab.domain.SubmissionKind.SUBMIT,
                    "class Solution {}",
                    null,
                    null,
                    Instant.EPOCH,
                    Instant.EPOCH)));
    when(challengeRepository.findById(challengeId))
        .thenReturn(
            Optional.of(
                new com.codetraininglab.platform.persistence.ChallengeEntity(
                    challengeId,
                    "reverse-string",
                    "Reverse String",
                    "desc",
                    "starter",
                    "{}",
                    "git",
                    "easy",
                    "java",
                    Instant.EPOCH,
                    Instant.EPOCH)));

    String review = service.reviewSubmission(submissionId);

    assertThat(review).contains("not configured");
  }

  @Test
  void reviewSubmissionThrowsWhenSubmissionMissing() {
    UUID submissionId = UUID.randomUUID();
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.reviewSubmission(submissionId))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void coachPromptFormatsLanguageLabels() {
    var challenge =
        new com.codetraininglab.platform.persistence.ChallengeEntity(
            UUID.randomUUID(),
            "slug",
            "Title",
            "desc",
            "starter",
            "{}",
            "git",
            "easy",
            "cpp",
            Instant.EPOCH,
            Instant.EPOCH);
    var item =
        new FeedbackItemEntity(
            UUID.randomUUID(),
            UUID.randomUUID(),
            FeedbackCategory.CORRECTNESS,
            FeedbackStatus.fail,
            "error",
            "failed",
            "id",
            Instant.EPOCH);

    var submission =
        new SubmissionEntity(
            UUID.randomUUID(),
            UUID.randomUUID(),
            challenge.getId(),
            UUID.randomUUID(),
            SubmissionStatus.COMPLETED,
            com.codetraininglab.domain.SubmissionKind.SUBMIT,
            "int[] twoSum(int[] nums, int target) { return null; }",
            null,
            null,
            Instant.EPOCH,
            Instant.EPOCH);

    String prompt = AiCoachService.coachPrompt(item, challenge, submission);

    assertThat(prompt).contains("C++").contains("slug");
    assertThat(prompt).contains("Learner's current submission");
    assertThat(prompt).contains("twoSum");
    assertThat(prompt).contains("Alternatives");
    assertThat(prompt).contains("fenced code blocks");
  }

  @Test
  void codeSampleGuidanceMentionsFencedBlocks() {
    assertThat(AiCoachService.codeSampleGuidance("java")).contains("fenced code blocks");
  }

  @Test
  void coachAnalysisInstructionsRequireSubmissionAnalysis() {
    assertThat(AiCoachService.coachAnalysisInstructions("java"))
        .contains("learner's submission")
        .contains("Alternatives");
  }
}
