package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.integration.runner.RunnerClient;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.submission.messaging.SubmissionEventType;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.scheduling.TaskScheduler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubmissionProcessorTest {

  @Mock private ChallengeHiddenTestRepository hiddenTestRepository;
  @Mock private RunnerClient runnerClient;
  @Mock private SubmissionProcessingStateWriter stateWriter;
  @Mock private TaskScheduler taskScheduler;

  private SubmissionProcessor processor;
  private final UUID submissionId = UUID.randomUUID();
  private final UUID challengeId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    when(taskScheduler.scheduleAtFixedRate(any(), any(), any()))
        .thenReturn(org.mockito.Mockito.mock(ScheduledFuture.class));
    processor =
        new SubmissionProcessor(
            hiddenTestRepository,
            runnerClient,
            stateWriter,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
            taskScheduler);
  }

  @Test
  void processesSubmission() {
    SubmissionEntity submission =
        new SubmissionEntity(
            submissionId,
            UUID.randomUUID(),
            challengeId,
            UUID.randomUUID(),
            SubmissionStatus.PENDING,
            com.codetraininglab.domain.SubmissionKind.SUBMIT,
            "code",
            null,
            null,
            Instant.EPOCH,
            Instant.EPOCH);
    ChallengeEntity challenge =
        new ChallengeEntity(
            challengeId,
            "reverse-string",
            "title",
            "desc",
            "starter",
            "{\"line_coverage_percent\":80}",
            "git",
            "easy",
            "java",
            null,
            Instant.EPOCH,
            Instant.EPOCH);
    LanguageRuntimeEntity runtime =
        new LanguageRuntimeEntity(
            submission.getRuntimeId(), UUID.randomUUID(), "26", "runner:local", true);
    SubmissionProcessingContext context =
        new SubmissionProcessingContext(
            submission, challenge, runtime, Path.of("challenges/reverse-string"));

    when(stateWriter.markRunning(submissionId)).thenReturn(context);
    when(hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(challengeId)).thenReturn(List.of());
    when(runnerClient.execute(any(), any(), any(), any(), any()))
        .thenReturn(
            new RunnerResult(
                "COMPLETED",
                List.of(new RunnerResult.TestOutcome("t", "PASS", null, 1)),
                new RunnerResult.CoverageOutcome(90, 80),
                new RunnerResult.CompileOutcome(0, List.of()),
                null,
                null));

    processor.process(submissionId);

    verify(stateWriter)
        .finalizeSuccess(
            eq(submissionId), org.mockito.ArgumentMatchers.any(RunnerResult.class), eq(challenge.getGatingConfig()));
  }

  @Test
  void marksInfrastructureFailure() {
    SubmissionEntity submission =
        new SubmissionEntity(
            submissionId,
            UUID.randomUUID(),
            challengeId,
            UUID.randomUUID(),
            SubmissionStatus.PENDING,
            com.codetraininglab.domain.SubmissionKind.SUBMIT,
            "code",
            null,
            null,
            Instant.EPOCH,
            Instant.EPOCH);
    ChallengeEntity challenge =
        new ChallengeEntity(
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
    LanguageRuntimeEntity runtime =
        new LanguageRuntimeEntity(
            submission.getRuntimeId(), UUID.randomUUID(), "26", "runner:local", true);
    SubmissionProcessingContext context =
        new SubmissionProcessingContext(
            submission, challenge, runtime, Path.of("challenges/reverse-string"));
    RunnerResult failed =
        new RunnerResult(
            com.codetraininglab.domain.RunnerStatus.FAILED.name(),
            List.of(
                new RunnerResult.TestOutcome(
                    "runner",
                    com.codetraininglab.domain.TestOutcomeStatus.FAIL.name(),
                    "Docker error",
                    0)),
            new RunnerResult.CoverageOutcome(0, 0),
            new RunnerResult.CompileOutcome(0, List.of()),
            null,
            new RunnerResult.LogsOutcome("out", "err"));

    when(stateWriter.markRunning(submissionId)).thenReturn(context);
    when(hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(challengeId)).thenReturn(List.of());
    when(runnerClient.execute(any(), any(), any(), any(), any())).thenReturn(failed);

    processor.process(submissionId);

    verify(stateWriter).markInfrastructureFailure(submissionId, "Docker error", failed.logs());
    verify(stateWriter, never()).finalizeSuccess(any(), any(), any());
  }

  @Test
  void skipsCancelledSubmission() {
    when(stateWriter.markRunning(submissionId)).thenReturn(null);
    processor.process(submissionId);
    verify(runnerClient, never()).execute(any(), any(), any(), any(), any());
  }

  @Test
  void marksInfrastructureFailureWhenMarkRunningThrows() {
    when(stateWriter.markRunning(submissionId)).thenThrow(new RuntimeException("db down"));

    processor.process(submissionId);

    verify(stateWriter).markInfrastructureFailure(submissionId, "Submission processing failed", null);
    verify(runnerClient, never()).execute(any(), any(), any(), any(), any());
  }

  @Test
  void marksInfrastructureFailureWhenRunnerThrows() {
    SubmissionProcessingContext context = processingContext();

    when(stateWriter.markRunning(submissionId)).thenReturn(context);
    when(hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(challengeId)).thenReturn(List.of());
    when(runnerClient.execute(any(), any(), any(), any(), any()))
        .thenThrow(new RuntimeException("docker unavailable"));

    processor.process(submissionId);

    verify(stateWriter).markInfrastructureFailure(submissionId, "Submission processing failed", null);
  }

  @Test
  void marksInfrastructureFailureWhenRunnerReturnsNullStatus() {
    SubmissionProcessingContext context = processingContext();

    when(stateWriter.markRunning(submissionId)).thenReturn(context);
    when(hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(challengeId)).thenReturn(List.of());
    when(runnerClient.execute(any(), any(), any(), any(), any()))
        .thenReturn(
            new RunnerResult(
                null,
                List.of(),
                new RunnerResult.CoverageOutcome(0, 0),
                new RunnerResult.CompileOutcome(0, List.of()),
                null,
                null));

    processor.process(submissionId);

    verify(stateWriter)
        .markInfrastructureFailure(
            submissionId, "Runner failed", new RunnerResult.LogsOutcome("", ""));
    verify(stateWriter, never()).finalizeSuccess(any(), any(), any());
  }

  @Test
  void publishesHeartbeatWhileDockerRunIsInProgress() {
    SubmissionProcessingContext context = processingContext();
    AtomicReference<Runnable> heartbeat = new AtomicReference<>();
    RunnerResult result =
        new RunnerResult(
            "COMPLETED",
            List.of(new RunnerResult.TestOutcome("t", "PASS", null, 1)),
            new RunnerResult.CoverageOutcome(90, 80),
            new RunnerResult.CompileOutcome(0, List.of()),
            null,
            null);

    when(stateWriter.markRunning(submissionId)).thenReturn(context);
    when(hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(challengeId)).thenReturn(List.of());
    when(taskScheduler.scheduleAtFixedRate(any(), any(), any()))
        .thenAnswer(
            inv -> {
              heartbeat.set(inv.getArgument(0));
              return org.mockito.Mockito.mock(ScheduledFuture.class);
            });
    when(runnerClient.execute(any(), any(), any(), any(), any()))
        .thenAnswer(
            inv -> {
              if (heartbeat.get() != null) {
                heartbeat.get().run();
              }
              return result;
            });

    processor.process(submissionId);

    verify(stateWriter)
        .publishStatus(
            eq(submissionId),
            eq(SubmissionStatus.RUNNING),
            org.mockito.ArgumentMatchers.contains("Docker: compiling & running tests"));
    verify(stateWriter).finalizeSuccess(eq(submissionId), eq(result), eq(context.challenge().getGatingConfig()));
  }

  @Test
  void marksInfrastructureFailureWhenFinalizeThrows() {
    SubmissionProcessingContext context = processingContext();
    RunnerResult result =
        new RunnerResult(
            "COMPLETED",
            List.of(new RunnerResult.TestOutcome("t", "PASS", null, 1)),
            new RunnerResult.CoverageOutcome(90, 80),
            new RunnerResult.CompileOutcome(0, List.of()),
            null,
            null);

    when(stateWriter.markRunning(submissionId)).thenReturn(context);
    when(hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(challengeId)).thenReturn(List.of());
    when(runnerClient.execute(any(), any(), any(), any(), any())).thenReturn(result);
    org.mockito.Mockito.doThrow(new RuntimeException("save failed"))
        .when(stateWriter)
        .finalizeSuccess(eq(submissionId), eq(result), eq(context.challenge().getGatingConfig()));

    processor.process(submissionId);

    verify(stateWriter).markInfrastructureFailure(submissionId, "Submission processing failed", null);
  }

  private SubmissionProcessingContext processingContext() {
    SubmissionEntity submission =
        new SubmissionEntity(
            submissionId,
            UUID.randomUUID(),
            challengeId,
            UUID.randomUUID(),
            SubmissionStatus.PENDING,
            com.codetraininglab.domain.SubmissionKind.SUBMIT,
            "code",
            null,
            null,
            Instant.EPOCH,
            Instant.EPOCH);
    ChallengeEntity challenge =
        new ChallengeEntity(
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
    LanguageRuntimeEntity runtime =
        new LanguageRuntimeEntity(
            submission.getRuntimeId(), UUID.randomUUID(), "26", "runner:local", true);
    return new SubmissionProcessingContext(
        submission, challenge, runtime, Path.of("challenges/reverse-string"));
  }
}
