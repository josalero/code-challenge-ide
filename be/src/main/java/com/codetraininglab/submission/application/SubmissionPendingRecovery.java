package com.codetraininglab.submission.application;

import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.platform.config.RabbitMqConfig;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.submission.messaging.SubmissionJobMessage;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Re-queues submissions stuck in PENDING after a worker crash or lost Rabbit message. */
@Component
public class SubmissionPendingRecovery {

  private static final Logger log = LoggerFactory.getLogger(SubmissionPendingRecovery.class);
  private static final long STALE_MINUTES = 2;

  private final SubmissionRepository submissionRepository;
  private final RabbitTemplate rabbitTemplate;
  private final Clock clock;

  public SubmissionPendingRecovery(
      SubmissionRepository submissionRepository,
      RabbitTemplate rabbitTemplate,
      Clock clock) {
    this.submissionRepository = submissionRepository;
    this.rabbitTemplate = rabbitTemplate;
    this.clock = clock;
  }

  @Scheduled(fixedRate = 60_000)
  public void recoverStalePendingSubmissions() {
    Instant cutoff = clock.instant().minus(STALE_MINUTES, ChronoUnit.MINUTES);
    List<SubmissionEntity> stale =
        submissionRepository.findByStatusAndUpdatedAtBefore(SubmissionStatus.PENDING, cutoff);
    for (SubmissionEntity submission : stale) {
      log.warn("Re-queuing stale PENDING submission {}", submission.getId());
      rabbitTemplate.convertAndSend(
          RabbitMqConfig.SUBMISSION_QUEUE, new SubmissionJobMessage(submission.getId()));
      submission.setUpdatedAt(clock.instant());
      submissionRepository.save(submission);
    }
  }
}
