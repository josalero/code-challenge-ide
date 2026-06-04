package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codetraininglab.catalog.application.ChallengeQuotaService;
import com.codetraininglab.submission.api.ReportResponse;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import com.codetraininglab.domain.FeedbackCategory;
import com.codetraininglab.domain.FeedbackStatus;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.platform.persistence.FeedbackItemEntity;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionReportEntity;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.SubmissionReportRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceAdditionalTest {

  @Mock private SubmissionRepository submissionRepository;
  @Mock private SubmissionReportRepository reportRepository;
  @Mock private FeedbackItemRepository feedbackItemRepository;
  @Mock private ChallengeRepository challengeRepository;
  @Mock private LanguageRuntimeResolver runtimeResolver;
  @Mock private UserProgressRepository progressRepository;
  @Mock private RabbitTemplate rabbitTemplate;
  @Mock private SubmissionEventHub eventHub;
  @Mock private ChallengeQuotaService challengeQuotaService;

  private SubmissionService service;
  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    var properties = CtlPropertiesTestFixtures.defaults();
    service =
        new SubmissionService(
            submissionRepository,
            reportRepository,
            feedbackItemRepository,
            challengeRepository,
            runtimeResolver,
            progressRepository,
            challengeQuotaService,
            rabbitTemplate,
            eventHub,
            properties,
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
  }

  @Test
  void getReport() {
    UUID reportId = UUID.randomUUID();
    UUID submissionId = UUID.randomUUID();
    when(reportRepository.findById(reportId))
        .thenReturn(
            Optional.of(
                new SubmissionReportEntity(reportId, submissionId, 1, "{}", false, Instant.EPOCH)));
    when(submissionRepository.findById(submissionId))
        .thenReturn(
            Optional.of(
                new SubmissionEntity(
                    submissionId,
                    userId,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    SubmissionStatus.COMPLETED,
                    SubmissionKind.SUBMIT,
                    "c",
                    null,
                    null,
                    Instant.EPOCH,
                    Instant.EPOCH)));
    when(feedbackItemRepository.findByReportIdOrderByCategoryAsc(reportId)).thenReturn(List.of());
    ReportResponse report = service.getReport(userId, reportId);
    assertThat(report.blocked()).isFalse();
    assertThat(report.feedback()).isEmpty();
  }

  @Test
  void getReportIncludesFeedbackForCoach() {
    UUID reportId = UUID.randomUUID();
    UUID submissionId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
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
                    SubmissionKind.SUBMIT,
                    "c",
                    null,
                    null,
                    Instant.EPOCH,
                    Instant.EPOCH)));
    when(feedbackItemRepository.findByReportIdOrderByCategoryAsc(reportId))
        .thenReturn(
            List.of(
                new FeedbackItemEntity(
                    itemId,
                    reportId,
                    FeedbackCategory.CORRECTNESS,
                    FeedbackStatus.fail,
                    "info",
                    "One or more tests failed",
                    "correctness",
                    Instant.EPOCH)));
    ReportResponse report = service.getReport(userId, reportId);
    assertThat(report.feedback()).hasSize(1);
    assertThat(report.feedback().getFirst().category()).isEqualTo("CORRECTNESS");
    assertThat(report.feedback().getFirst().message()).contains("tests failed");
  }

  @Test
  void cancelPending() {
    UUID submissionId = UUID.randomUUID();
    SubmissionEntity entity =
        new SubmissionEntity(
            submissionId,
            userId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            SubmissionStatus.PENDING,
            SubmissionKind.SUBMIT,
            "c",
            null,
            null,
            Instant.EPOCH,
            Instant.EPOCH);
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(entity));
    when(submissionRepository.save(entity)).thenReturn(entity);
    service.cancel(userId, submissionId);
    assertThat(entity.getStatus()).isEqualTo(SubmissionStatus.CANCELLED);
  }
}
