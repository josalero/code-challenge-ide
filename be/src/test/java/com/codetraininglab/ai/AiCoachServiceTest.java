package com.codetraininglab.coach.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.config.CtlProperties;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiCoachServiceTest {

  @Mock private FeedbackItemRepository feedbackItemRepository;
  @Mock private SubmissionReportRepository reportRepository;
  @Mock private SubmissionRepository submissionRepository;
  @Mock private ChallengeRepository challengeRepository;

  private AiCoachService service;

  @BeforeEach
  void setUp() {
    CtlProperties properties =
        new CtlProperties(
            true,
            "test-jwt-secret-must-be-at-least-32-characters-long",
            24,
            "http://localhost:5173",
            "challenges",
            "runner",
            "",
            "lsp",
            5,
            24,
            "openrouter",
            "",
            "model",
            "http://localhost:11434",
            "ollama", false, false);
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
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    SubmissionStatus.COMPLETED,
                    "c",
                    null,
                    null,
                    Instant.EPOCH,
                    Instant.EPOCH)));

    var response = service.explain(userId, itemId);
    assertThat(response.explanation()).contains("not configured");
  }
}
