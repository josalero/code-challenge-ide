package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.submission.application.LanguageRuntimeResolver;
import com.codetraininglab.submission.api.CreateSubmissionRequest;
import com.codetraininglab.submission.api.SubmissionResponse;
import com.codetraininglab.submission.messaging.SubmissionJobMessage;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import com.codetraininglab.platform.config.RabbitMqConfig;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.SubmissionReportRepository;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

  @Mock private SubmissionRepository submissionRepository;
  @Mock private SubmissionReportRepository reportRepository;
  @Mock private FeedbackItemRepository feedbackItemRepository;
  @Mock private ChallengeRepository challengeRepository;
  @Mock private LanguageRuntimeResolver runtimeResolver;
  @Mock private UserProgressRepository progressRepository;
  @Mock private RabbitTemplate rabbitTemplate;
  @Mock private SubmissionEventHub eventHub;

  private SubmissionService service;
  private final UUID userId = UUID.randomUUID();
  private final UUID challengeId = UUID.randomUUID();
  private final UUID runtimeId = UUID.randomUUID();
  private final UUID langId = UUID.randomUUID();

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
            rabbitTemplate,
            eventHub,
            properties,
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
  }

  @Test
  void createsSubmission() {
    when(submissionRepository.findIdempotent(any(), any(), any())).thenReturn(Optional.empty());
    when(challengeRepository.findBySlug("slug"))
        .thenReturn(
            Optional.of(
                new ChallengeEntity(
                    challengeId,
                    "slug",
                    "t",
                    "d",
                    "s",
                    "{}",
                    "git",
                    "easy",
                    "java",
                    Instant.EPOCH,
                    Instant.EPOCH)));
    ChallengeEntity challenge =
        challengeRepository.findBySlug("slug").orElseThrow();
    when(runtimeResolver.resolve(challenge, "26"))
        .thenReturn(new LanguageRuntimeEntity(runtimeId, langId, "26", "img", true));
    when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(progressRepository.findByUserIdAndChallengeId(any(), any())).thenReturn(Optional.empty());

    SubmissionResponse response =
        service.create(
            userId, new CreateSubmissionRequest("slug", "26", "code", null, null), "key-1");

    assertThat(response.status()).isEqualTo("PENDING");
    verify(rabbitTemplate)
        .convertAndSend(eq(RabbitMqConfig.SUBMISSION_QUEUE), any(SubmissionJobMessage.class));
  }

  @Test
  void cancelRejectsCompleted() {
    UUID submissionId = UUID.randomUUID();
    SubmissionEntity entity =
        new SubmissionEntity(
            submissionId,
            userId,
            challengeId,
            runtimeId,
            SubmissionStatus.COMPLETED,
            SubmissionKind.SUBMIT,
            "c",
            null,
            null,
            Instant.EPOCH,
            Instant.EPOCH);
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service.cancel(userId, submissionId))
        .isInstanceOf(ResponseStatusException.class);
  }
}
