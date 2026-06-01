package com.codetraininglab.submission.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.platform.config.RabbitMqConfig;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.submission.messaging.SubmissionJobMessage;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class SubmissionPendingRecoveryTest {

  @Mock private SubmissionRepository submissionRepository;
  @Mock private RabbitTemplate rabbitTemplate;

  private SubmissionPendingRecovery recovery;
  private final Instant now = Instant.parse("2026-06-01T12:00:00Z");
  private final UUID submissionId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    recovery =
        new SubmissionPendingRecovery(
            submissionRepository, rabbitTemplate, Clock.fixed(now, ZoneOffset.UTC));
  }

  @Test
  void recoverStalePendingSubmissionsRequeuesAndTouchesUpdatedAt() {
    SubmissionEntity stale =
        new SubmissionEntity(
            submissionId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            SubmissionStatus.PENDING,
            "code",
            null,
            null,
            now.minus(2, ChronoUnit.MINUTES),
            now.minus(2, ChronoUnit.MINUTES));

    when(submissionRepository.findByStatusAndUpdatedAtBefore(
            SubmissionStatus.PENDING, now.minus(45, ChronoUnit.SECONDS)))
        .thenReturn(List.of(stale));
    when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    recovery.recoverStalePendingSubmissions();

    verify(rabbitTemplate)
        .convertAndSend(
            eq(RabbitMqConfig.SUBMISSION_QUEUE), eq(new SubmissionJobMessage(submissionId)));
    verify(submissionRepository).save(stale);
  }

  @Test
  void recoverStalePendingSubmissionsDoesNothingWhenNoneFound() {
    when(submissionRepository.findByStatusAndUpdatedAtBefore(any(), any())).thenReturn(List.of());

    recovery.recoverStalePendingSubmissions();

    verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    verify(submissionRepository, never()).save(any());
  }
}
