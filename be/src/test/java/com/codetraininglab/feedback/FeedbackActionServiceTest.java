package com.codetraininglab.feedback.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.coach.application.AiCoachService;
import com.codetraininglab.domain.FeedbackActionStatus;
import com.codetraininglab.domain.FeedbackActionType;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionFeedbackActionEntity;
import com.codetraininglab.platform.persistence.SubmissionFeedbackActionRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
class FeedbackActionServiceTest {

  @Mock private SubmissionRepository submissionRepository;
  @Mock private SubmissionFeedbackActionRepository actionRepository;
  @Mock private AiCoachService aiCoachService;

  private FeedbackActionService service;
  private final UUID submissionId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();
  private final Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);

  @BeforeEach
  void setUp() {
    CoachFeedbackActionHandler coach = new CoachFeedbackActionHandler(aiCoachService);
    SonarFeedbackActionHandler sonar = new SonarFeedbackActionHandler();
    ComplexityFeedbackActionHandler complexity = new ComplexityFeedbackActionHandler();
    service =
        new FeedbackActionService(
            submissionRepository,
            actionRepository,
            List.of(coach, sonar, complexity),
            clock);
  }

  @Test
  void requestCreatesQueuedAction() {
    SubmissionEntity submission = submission(userId);
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
    when(actionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    SubmissionFeedbackActionEntity result =
        service.request(userId, submissionId, FeedbackActionType.COACH);

    assertThat(result.getStatus()).isEqualTo(FeedbackActionStatus.QUEUED);
    assertThat(result.getAction()).isEqualTo(FeedbackActionType.COACH);
    assertThat(result.getSubmissionId()).isEqualTo(submissionId);
  }

  @Test
  void requestRejectsSubmissionOwnedByAnotherUser() {
    SubmissionEntity submission = submission(UUID.randomUUID());
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

    assertThatThrownBy(() -> service.request(userId, submissionId, FeedbackActionType.COACH))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("403");
    verify(actionRepository, never()).save(any());
  }

  @Test
  void runAsyncCompletesCoachActionWithResult() {
    UUID actionId = UUID.randomUUID();
    SubmissionFeedbackActionEntity entity =
        new SubmissionFeedbackActionEntity(
            actionId, submissionId, FeedbackActionType.COACH, FeedbackActionStatus.QUEUED, clock.instant());
    when(actionRepository.findById(actionId)).thenReturn(Optional.of(entity));
    when(actionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission(userId)));
    when(aiCoachService.reviewSubmission(submissionId)).thenReturn("Looks good — try edge cases.");

    service.runAsync(actionId);

    assertThat(entity.getStatus()).isEqualTo(FeedbackActionStatus.COMPLETED);
    assertThat(entity.getResult()).isEqualTo("Looks good — try edge cases.");
    assertThat(entity.getErrorMessage()).isNull();
  }

  @Test
  void runAsyncMarksFailedWhenHandlerNotAvailable() {
    UUID actionId = UUID.randomUUID();
    SubmissionFeedbackActionEntity entity =
        new SubmissionFeedbackActionEntity(
            actionId, submissionId, FeedbackActionType.SONAR, FeedbackActionStatus.QUEUED, clock.instant());
    when(actionRepository.findById(actionId)).thenReturn(Optional.of(entity));
    when(actionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission(userId)));

    service.runAsync(actionId);

    assertThat(entity.getStatus()).isEqualTo(FeedbackActionStatus.FAILED);
    assertThat(entity.getErrorMessage()).contains("not yet available");
  }

  @Test
  void runAsyncMarksFailedOnHandlerException() {
    UUID actionId = UUID.randomUUID();
    SubmissionFeedbackActionEntity entity =
        new SubmissionFeedbackActionEntity(
            actionId, submissionId, FeedbackActionType.COACH, FeedbackActionStatus.QUEUED, clock.instant());
    when(actionRepository.findById(actionId)).thenReturn(Optional.of(entity));
    when(actionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission(userId)));
    when(aiCoachService.reviewSubmission(submissionId)).thenThrow(new RuntimeException("LLM down"));

    service.runAsync(actionId);

    assertThat(entity.getStatus()).isEqualTo(FeedbackActionStatus.FAILED);
    assertThat(entity.getErrorMessage()).contains("LLM down");
  }

  private SubmissionEntity submission(UUID owner) {
    return new SubmissionEntity(
        submissionId,
        owner,
        UUID.randomUUID(),
        UUID.randomUUID(),
        SubmissionStatus.COMPLETED,
            com.codetraininglab.domain.SubmissionKind.SUBMIT,
        "code",
        null,
        null,
        Instant.EPOCH,
        Instant.EPOCH);
  }
}
