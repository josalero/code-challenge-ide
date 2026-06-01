package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionReportRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.submission.messaging.SsePayloadKeys;
import com.codetraininglab.submission.messaging.SubmissionEventType;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class SubmissionProcessingStateWriterTest {

  @Mock private SubmissionRepository submissionRepository;
  @Mock private SubmissionReportRepository reportRepository;
  @Mock private FeedbackItemRepository feedbackItemRepository;
  @Mock private ChallengeRepository challengeRepository;
  @Mock private LanguageRuntimeRepository runtimeRepository;
  @Mock private UserProgressRepository progressRepository;
  @Mock private SubmissionEventHub eventHub;

  private SubmissionProcessingStateWriter writer;
  private final Clock clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
  private final UUID submissionId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();
  private final UUID challengeId = UUID.randomUUID();
  private final UUID runtimeId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    writer =
        new SubmissionProcessingStateWriter(
            submissionRepository,
            reportRepository,
            feedbackItemRepository,
            challengeRepository,
            runtimeRepository,
            progressRepository,
            CtlPropertiesTestFixtures.defaults(),
            eventHub,
            JsonMapper.builder().build(),
            clock);
  }

  @Test
  void markRunningReturnsNullWhenSubmissionMissing() {
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.empty());

    assertThat(writer.markRunning(submissionId)).isNull();
    verify(eventHub, never()).publish(any(), any(), any());
  }

  @Test
  void markRunningReturnsNullWhenCancelled() {
    when(submissionRepository.findById(submissionId))
        .thenReturn(Optional.of(submission(SubmissionStatus.CANCELLED)));

    assertThat(writer.markRunning(submissionId)).isNull();
    verify(submissionRepository, never()).save(any());
  }

  @Test
  void markRunningUpdatesStatusAndReturnsContext() {
    SubmissionEntity submission = submission(SubmissionStatus.PENDING);
    ChallengeEntity challenge = challenge();
    LanguageRuntimeEntity runtime = runtime();

    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
    when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
    when(runtimeRepository.findById(runtimeId)).thenReturn(Optional.of(runtime));

    SubmissionProcessingContext context = writer.markRunning(submissionId);

    assertThat(context).isNotNull();
    assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.RUNNING);
    assertThat(context.challengeDir()).isEqualTo(Path.of("challenges/reverse-string"));
    verify(eventHub, org.mockito.Mockito.atLeast(2))
        .publish(eq(submissionId), eq(SubmissionEventType.STATUS.eventName()), any());
  }

  @Test
  void markRunningThrowsWhenChallengeMissing() {
    SubmissionEntity submission = submission(SubmissionStatus.PENDING);
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
    when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(challengeRepository.findById(challengeId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> writer.markRunning(submissionId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void finalizeSuccessSkipsWhenSubmissionMissing() {
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.empty());

    writer.finalizeSuccess(submissionId, runnerResult("PASS"), "{}");

    verify(reportRepository, never()).save(any());
    verify(eventHub, never()).publish(any(), eq(SubmissionEventType.DONE.eventName()), any());
  }

  @Test
  void finalizeSuccessPersistsReportAndProgress() {
    SubmissionEntity submission = submission(SubmissionStatus.RUNNING);
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
    when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(progressRepository.findByUserIdAndChallengeId(userId, challengeId)).thenReturn(Optional.empty());
    when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    writer.finalizeSuccess(
        submissionId,
        new RunnerResult(
            "COMPLETED",
            List.of(new RunnerResult.TestOutcome("t1", "PASS", "ok", 1)),
            new RunnerResult.CoverageOutcome(90, 80),
            new RunnerResult.CompileOutcome(0, List.of()),
            null,
            null),
        "{\"line_coverage_percent\":80}");

    assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.COMPLETED);
    verify(reportRepository).save(any());
    verify(feedbackItemRepository).saveAll(any());
    verify(eventHub).publish(eq(submissionId), eq(SubmissionEventType.DONE.eventName()), any());

    ArgumentCaptor<UserProgressEntity> progressCaptor = ArgumentCaptor.forClass(UserProgressEntity.class);
    verify(progressRepository).save(progressCaptor.capture());
    assertThat(progressCaptor.getValue().getState()).isEqualTo(ProgressState.PASSED);
  }

  @Test
  void finalizeSuccessMarksProgressFailedWhenBlocked() {
    SubmissionEntity submission = submission(SubmissionStatus.RUNNING);
    UserProgressEntity existing =
        new UserProgressEntity(
            UUID.randomUUID(), userId, challengeId, ProgressState.NOT_STARTED, Instant.EPOCH);

    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
    when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(progressRepository.findByUserIdAndChallengeId(userId, challengeId))
        .thenReturn(Optional.of(existing));
    when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    writer.finalizeSuccess(
        submissionId,
        new RunnerResult(
            "COMPLETED",
            List.of(new RunnerResult.TestOutcome("t1", "FAIL", "assertion", 1)),
            new RunnerResult.CoverageOutcome(90, 80),
            new RunnerResult.CompileOutcome(0, List.of()),
            null,
            null),
        "{}");

    assertThat(existing.getState()).isEqualTo(ProgressState.FAILED);
  }

  @Test
  void markInfrastructureFailureUsesDefaultMessageAndPublishesLogs() {
    SubmissionEntity submission = submission(SubmissionStatus.RUNNING);
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
    when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    writer.markInfrastructureFailure(
        submissionId,
        "  ",
        new RunnerResult.LogsOutcome("stdout", "stderr"));

    assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.FAILED);

    ArgumentCaptor<java.util.Map<String, Object>> errorCaptor = ArgumentCaptor.forClass(java.util.Map.class);
    verify(eventHub).publish(eq(submissionId), eq(SubmissionEventType.ERROR.eventName()), errorCaptor.capture());
    assertThat(errorCaptor.getValue())
        .containsEntry(SsePayloadKeys.MESSAGE, "Runner or infrastructure error")
        .containsEntry(SsePayloadKeys.STDOUT, "stdout")
        .containsEntry(SsePayloadKeys.STDERR, "stderr");
    verify(eventHub).publish(eq(submissionId), eq(SubmissionEventType.DONE.eventName()), any());
  }

  @Test
  void markInfrastructureFailureSkipsCancelledSubmission() {
    when(submissionRepository.findById(submissionId))
        .thenReturn(Optional.of(submission(SubmissionStatus.CANCELLED)));

    writer.markInfrastructureFailure(submissionId, "boom", null);

    verify(submissionRepository, never()).save(any());
    verify(eventHub, never()).publish(any(), any(), any());
  }

  @Test
  void publishRunnerFinishedEmitsStatusWithTestCount() {
    writer.publishRunnerFinished(
        submissionId,
        new RunnerResult(
            "COMPLETED",
            List.of(
                new RunnerResult.TestOutcome("a", "PASS", null, 1),
                new RunnerResult.TestOutcome("b", "PASS", null, 1)),
            new RunnerResult.CoverageOutcome(90, 80),
            new RunnerResult.CompileOutcome(0, List.of()),
            null,
            null));

    ArgumentCaptor<java.util.Map<String, Object>> statusCaptor = ArgumentCaptor.forClass(java.util.Map.class);
    verify(eventHub)
        .publish(eq(submissionId), eq(SubmissionEventType.STATUS.eventName()), statusCaptor.capture());
    assertThat(statusCaptor.getValue().get(SsePayloadKeys.MESSAGE).toString())
        .contains("2 test result(s)");
  }

  private SubmissionEntity submission(SubmissionStatus status) {
    return new SubmissionEntity(
        submissionId,
        userId,
        challengeId,
        runtimeId,
        status,
        "code",
        null,
        null,
        Instant.EPOCH,
        Instant.EPOCH);
  }

  private ChallengeEntity challenge() {
    return new ChallengeEntity(
        challengeId,
        "reverse-string",
        "title",
        "desc",
        "starter",
        "{}",
        "git",
        "easy",
        "java",
        Instant.EPOCH,
        Instant.EPOCH);
  }

  private LanguageRuntimeEntity runtime() {
    return new LanguageRuntimeEntity(runtimeId, UUID.randomUUID(), "26", "runner:local", true);
  }

  private RunnerResult runnerResult(String testStatus) {
    return new RunnerResult(
        "COMPLETED",
        List.of(new RunnerResult.TestOutcome("t", testStatus, null, 1)),
        new RunnerResult.CoverageOutcome(90, 80),
        new RunnerResult.CompileOutcome(0, List.of()),
        null,
        null);
  }
}
