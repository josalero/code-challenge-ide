package com.codetraininglab.submission.application;

import com.codetraininglab.integration.runner.RunnerClient;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestRepository;
import com.codetraininglab.domain.RunnerStatus;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class SubmissionProcessor {

  private static final Logger log = LoggerFactory.getLogger(SubmissionProcessor.class);

  private final ChallengeHiddenTestRepository hiddenTestRepository;
  private final RunnerClient runnerClient;
  private final SubmissionProcessingStateWriter stateWriter;
  private final Clock clock;
  private final TaskScheduler taskScheduler;

  public SubmissionProcessor(
      ChallengeHiddenTestRepository hiddenTestRepository,
      RunnerClient runnerClient,
      SubmissionProcessingStateWriter stateWriter,
      Clock clock,
      TaskScheduler taskScheduler) {
    this.hiddenTestRepository = hiddenTestRepository;
    this.runnerClient = runnerClient;
    this.stateWriter = stateWriter;
    this.clock = clock;
    this.taskScheduler = taskScheduler;
  }

  public void process(UUID submissionId) {
    SubmissionProcessingContext context;
    try {
      context = stateWriter.markRunning(submissionId);
    } catch (Exception e) {
      log.error("Failed to mark submission {} as running", submissionId, e);
      stateWriter.markInfrastructureFailure(submissionId, "Submission processing failed", null);
      return;
    }
    if (context == null) {
      return;
    }

    Instant dockerStarted = clock.instant();
    ScheduledFuture<?> heartbeat =
        taskScheduler.scheduleAtFixedRate(
            () -> {
              long seconds = Duration.between(dockerStarted, clock.instant()).getSeconds();
              stateWriter.publishStatus(
                  submissionId,
                  SubmissionStatus.RUNNING,
                  "Docker: compiling & running tests (" + seconds + "s)");
            },
            Instant.now().plusSeconds(10),
            Duration.ofSeconds(10));

    RunnerResult result;
    try {
      result =
          runnerClient.execute(
              context.submission(),
              context.challenge().getSlug(),
              hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(
                  context.challenge().getId()),
              context.challengeDir(),
              context.runtime().getDockerImage());
      long runnerMs = Duration.between(dockerStarted, clock.instant()).toMillis();
      log.info(
          "Submission {} runner finished in {} ms ({} tests)",
          submissionId,
          runnerMs,
          result.tests().size());
    } catch (Exception e) {
      heartbeat.cancel(false);
      long runnerMs = Duration.between(dockerStarted, clock.instant()).toMillis();
      log.error("Unexpected error running submission {} after {} ms", submissionId, runnerMs, e);
      stateWriter.markInfrastructureFailure(submissionId, "Submission processing failed", null);
      return;
    } finally {
      heartbeat.cancel(false);
    }

    stateWriter.publishRunnerFinished(submissionId, result);

    if (isInfrastructureFailure(result)) {
      stateWriter.markInfrastructureFailure(
          submissionId,
          result.tests().isEmpty() ? "Runner failed" : result.tests().getFirst().message(),
          result.logs());
      return;
    }

    try {
      if (context.submission().getKind() == SubmissionKind.RUN) {
        stateWriter.finalizeRun(submissionId, result);
      } else {
        stateWriter.finalizeSuccess(
            submissionId, result, context.challenge().getGatingConfig());
      }
    } catch (Exception e) {
      log.error("Failed to finalize submission {}", submissionId, e);
      stateWriter.markInfrastructureFailure(submissionId, "Submission processing failed", null);
    }
  }

  private static boolean isInfrastructureFailure(RunnerResult result) {
    if (result == null || result.status() == null) {
      return true;
    }
    RunnerStatus status = RunnerStatus.fromString(result.status());
    return status == null || status.isInfrastructureFailure();
  }
}
