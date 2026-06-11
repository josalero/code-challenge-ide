package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.FeedbackCategory;
import com.codetraininglab.domain.FeedbackStatus;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.FeedbackItemEntity;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionReportEntity;
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
import java.util.stream.StreamSupport;
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

    ArgumentCaptor<SubmissionReportEntity> reportCaptor =
        ArgumentCaptor.forClass(SubmissionReportEntity.class);
    verify(reportRepository).save(reportCaptor.capture());
    assertThat(reportCaptor.getValue().isBlocked()).isFalse();
    assertThat(reportCaptor.getValue().getSummary()).contains("\"blocked\":false");

    verify(feedbackItemRepository)
        .saveAll(argThat(feedbackHasCorrectness(FeedbackStatus.pass, "All tests passed")));

    verify(eventHub)
        .publish(
            eq(submissionId),
            eq(SubmissionEventType.DONE.eventName()),
            argThat(
                payload ->
                    payload instanceof java.util.Map<?, ?> map
                        && SubmissionKind.SUBMIT.name().equals(map.get(SsePayloadKeys.KIND))
                        && map.containsKey(SsePayloadKeys.REPORT_ID)));

    ArgumentCaptor<UserProgressEntity> progressCaptor = ArgumentCaptor.forClass(UserProgressEntity.class);
    verify(progressRepository).save(progressCaptor.capture());
    assertThat(progressCaptor.getValue().getState()).isEqualTo(ProgressState.PASSED);
    assertThat(progressCaptor.getValue().getSubmittedAt()).isEqualTo(Instant.EPOCH);
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
    assertThat(existing.getSubmittedAt()).isEqualTo(Instant.EPOCH);

    ArgumentCaptor<SubmissionReportEntity> reportCaptor =
        ArgumentCaptor.forClass(SubmissionReportEntity.class);
    verify(reportRepository).save(reportCaptor.capture());
    assertThat(reportCaptor.getValue().isBlocked()).isTrue();

    verify(feedbackItemRepository)
        .saveAll(
            argThat(feedbackHasCorrectness(FeedbackStatus.fail, "One or more tests failed")));
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

    verify(eventHub)
        .publish(
            eq(submissionId),
            eq(SubmissionEventType.ERROR.eventName()),
            argThat(
                payload ->
                    payload instanceof java.util.Map<?, ?> map
                        && "Runner or infrastructure error"
                            .equals(map.get(SsePayloadKeys.MESSAGE))
                        && "stdout".equals(map.get(SsePayloadKeys.STDOUT))
                        && "stderr".equals(map.get(SsePayloadKeys.STDERR))));
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

    verify(eventHub)
        .publish(
            eq(submissionId),
            eq(SubmissionEventType.STATUS.eventName()),
            argThat(
                payload ->
                    payload instanceof java.util.Map<?, ?> map
                        && map.get(SsePayloadKeys.MESSAGE).toString().contains("2 test result(s)")));
  }

  @Test
  void finalizeRunPublishesRunnerLogsOnDone() {
    SubmissionEntity submission =
        new SubmissionEntity(
            submissionId,
            userId,
            challengeId,
            runtimeId,
            SubmissionStatus.RUNNING,
            SubmissionKind.RUN,
            "code",
            null,
            null,
            Instant.EPOCH,
            Instant.EPOCH);
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
    when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    writer.finalizeRun(
        submissionId,
        new RunnerResult(
            "COMPLETED",
            List.of(new RunnerResult.TestOutcome("t1", "FAIL", "AssertionError", 1)),
            new RunnerResult.CoverageOutcome(0, 0),
            new RunnerResult.CompileOutcome(0, List.of()),
            null,
            new RunnerResult.LogsOutcome("printed debug\n", "warning line")));

    verify(eventHub)
        .publish(
            eq(submissionId),
            eq(SubmissionEventType.DONE.eventName()),
            argThat(
                payload ->
                    payload instanceof java.util.Map<?, ?> map
                        && SubmissionKind.RUN.name().equals(map.get(SsePayloadKeys.KIND))
                        && Boolean.FALSE.equals(map.get(SsePayloadKeys.PASSED))
                        && "printed debug\n".equals(map.get(SsePayloadKeys.STDOUT))
                        && "warning line".equals(map.get(SsePayloadKeys.STDERR))));
  }

  private SubmissionEntity submission(SubmissionStatus status) {
    return new SubmissionEntity(
        submissionId,
        userId,
        challengeId,
        runtimeId,
        status,
        SubmissionKind.SUBMIT,
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
        null,
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

  private static org.mockito.ArgumentMatcher<Iterable<FeedbackItemEntity>>
      feedbackHasCorrectness(FeedbackStatus status, String message) {
    return items ->
        items != null
            && StreamSupport.stream(items.spliterator(), false)
                .anyMatch(
                    item ->
                        item.getCategory() == FeedbackCategory.CORRECTNESS
                            && item.getStatus() == status
                            && message.equals(item.getMessage()));
  }
}
