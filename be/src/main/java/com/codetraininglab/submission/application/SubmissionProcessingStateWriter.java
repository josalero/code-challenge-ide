package com.codetraininglab.submission.application;

import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.domain.TestOutcomeStatus;
import com.codetraininglab.feedback.application.FeedbackAggregator;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.platform.config.CtlProperties;
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
import java.nio.file.Path;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class SubmissionProcessingStateWriter {

  private final SubmissionRepository submissionRepository;
  private final SubmissionReportRepository reportRepository;
  private final FeedbackItemRepository feedbackItemRepository;
  private final ChallengeRepository challengeRepository;
  private final LanguageRuntimeRepository runtimeRepository;
  private final UserProgressRepository progressRepository;
  private final CtlProperties properties;
  private final SubmissionEventHub eventHub;
  private final JsonMapper jsonMapper;
  private final Clock clock;

  public SubmissionProcessingStateWriter(
      SubmissionRepository submissionRepository,
      SubmissionReportRepository reportRepository,
      FeedbackItemRepository feedbackItemRepository,
      ChallengeRepository challengeRepository,
      LanguageRuntimeRepository runtimeRepository,
      UserProgressRepository progressRepository,
      CtlProperties properties,
      SubmissionEventHub eventHub,
      JsonMapper jsonMapper,
      Clock clock) {
    this.submissionRepository = submissionRepository;
    this.reportRepository = reportRepository;
    this.feedbackItemRepository = feedbackItemRepository;
    this.challengeRepository = challengeRepository;
    this.runtimeRepository = runtimeRepository;
    this.progressRepository = progressRepository;
    this.properties = properties;
    this.eventHub = eventHub;
    this.jsonMapper = jsonMapper;
    this.clock = clock;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public SubmissionProcessingContext markRunning(UUID submissionId) {
    SubmissionEntity submission =
        submissionRepository.findById(submissionId).orElse(null);
    if (submission == null || submission.getStatus() == SubmissionStatus.CANCELLED) {
      return null;
    }
    submission.setStatus(SubmissionStatus.RUNNING);
    submission.setUpdatedAt(clock.instant());
    submissionRepository.save(submission);

    ChallengeEntity challenge =
        challengeRepository
            .findById(submission.getChallengeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    LanguageRuntimeEntity runtime =
        runtimeRepository
            .findById(submission.getRuntimeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    String runnerImage =
        properties.runnerImageFor(
            challenge.getLanguage(), runtime.getVersion(), runtime.getDockerImage());
    Path challengeDir = Path.of(properties.challengesPath(), challenge.getSlug());

    publishStatus(
        submissionId,
        SubmissionStatus.RUNNING,
        "Worker started — preparing "
            + challenge.getLanguage()
            + " "
            + runtime.getVersion()
            + " sandbox");
    publishStatus(
        submissionId,
        SubmissionStatus.RUNNING,
        "Launching Docker runner (" + runnerImage + ")");

    return new SubmissionProcessingContext(submission, challenge, runtime, challengeDir);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void finalizeSuccess(UUID submissionId, RunnerResult result, String gatingConfig) {
    SubmissionEntity submission =
        submissionRepository.findById(submissionId).orElse(null);
    if (submission == null || submission.getStatus() == SubmissionStatus.CANCELLED) {
      return;
    }

    publishTestResults(submissionId, result);
    publishStatus(
        submissionId, SubmissionStatus.RUNNING, "Aggregating coverage, style, and coach report");

    UUID reportId = UUID.randomUUID();
    var aggregated =
        FeedbackAggregator.aggregate(
            reportId, submissionId, result, gatingConfig, jsonMapper, clock);
    reportRepository.save(aggregated.report());
    feedbackItemRepository.saveAll(aggregated.items());

    submission.setStatus(SubmissionStatus.COMPLETED);
    submission.setUpdatedAt(clock.instant());
    submissionRepository.save(submission);

    updateProgress(submission, aggregated.blocked(), SubmissionKind.SUBMIT);
    eventHub.publish(
        submissionId,
        SubmissionEventType.DONE.eventName(),
        Map.of(
            SsePayloadKeys.SUBMISSION_ID,
            submissionId,
            SsePayloadKeys.KIND,
            SubmissionKind.SUBMIT.name(),
            SsePayloadKeys.REPORT_ID,
            reportId));
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void finalizeRun(UUID submissionId, RunnerResult result) {
    SubmissionEntity submission =
        submissionRepository.findById(submissionId).orElse(null);
    if (submission == null || submission.getStatus() == SubmissionStatus.CANCELLED) {
      return;
    }

    publishTestResults(submissionId, result);
    boolean allTestsPassed =
        result.tests() != null
            && !result.tests().isEmpty()
            && result.tests().stream()
                .allMatch(test -> TestOutcomeStatus.PASS.name().equals(test.status()));

    submission.setStatus(SubmissionStatus.COMPLETED);
    submission.setUpdatedAt(clock.instant());
    submissionRepository.save(submission);

    HashMap<String, Object> done = new HashMap<>();
    done.put(SsePayloadKeys.SUBMISSION_ID, submissionId);
    done.put(SsePayloadKeys.KIND, SubmissionKind.RUN.name());
    done.put(SsePayloadKeys.PASSED, allTestsPassed);
    done.put(
        SsePayloadKeys.MESSAGE,
        allTestsPassed
            ? "All tests passed — you can keep editing or submit when ready."
            : "Some tests failed — fix your solution and run again, or submit for full feedback.");
    eventHub.publish(submissionId, SubmissionEventType.DONE.eventName(), done);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markInfrastructureFailure(
      UUID submissionId, String message, RunnerResult.LogsOutcome logs) {
    SubmissionEntity submission =
        submissionRepository.findById(submissionId).orElse(null);
    if (submission == null || submission.getStatus() == SubmissionStatus.CANCELLED) {
      return;
    }
    String safeMessage =
        message == null || message.isBlank() ? "Runner or infrastructure error" : message;
    submission.setStatus(SubmissionStatus.FAILED);
    submission.setUpdatedAt(clock.instant());
    submissionRepository.save(submission);

    HashMap<String, Object> errorPayload = new HashMap<>();
    errorPayload.put(SsePayloadKeys.MESSAGE, safeMessage);
    if (logs != null) {
      if (!logs.stdoutTruncated().isBlank()) {
        errorPayload.put(SsePayloadKeys.STDOUT, logs.stdoutTruncated());
      }
      if (!logs.stderrTruncated().isBlank()) {
        errorPayload.put(SsePayloadKeys.STDERR, logs.stderrTruncated());
      }
    }
    eventHub.publish(submissionId, SubmissionEventType.ERROR.eventName(), errorPayload);
    eventHub.publish(
        submissionId,
        SubmissionEventType.DONE.eventName(),
        Map.of(SsePayloadKeys.SUBMISSION_ID, submissionId));
  }

  void publishStatus(UUID submissionId, SubmissionStatus status, String message) {
    HashMap<String, Object> payload = new HashMap<>();
    payload.put(SsePayloadKeys.STATUS, status.name());
    payload.put(SsePayloadKeys.MESSAGE, message);
    eventHub.publish(submissionId, SubmissionEventType.STATUS.eventName(), payload);
  }

  void publishRunnerFinished(UUID submissionId, RunnerResult result) {
    publishStatus(
        submissionId,
        SubmissionStatus.RUNNING,
        "Runner finished — publishing " + result.tests().size() + " test result(s)");
  }

  private void publishTestResults(UUID submissionId, RunnerResult result) {
    for (RunnerResult.TestOutcome test : result.tests()) {
      HashMap<String, Object> payload = new HashMap<>();
      payload.put(SsePayloadKeys.NAME, test.name());
      payload.put(SsePayloadKeys.STATUS, test.status());
      if (test.message() != null) {
        payload.put(SsePayloadKeys.MESSAGE, test.message());
      }
      eventHub.publish(submissionId, SubmissionEventType.TEST_RESULT.eventName(), payload);
    }
  }

  private void updateProgress(
      SubmissionEntity submission, boolean blocked, SubmissionKind kind) {
    if (kind != SubmissionKind.SUBMIT) {
      return;
    }
    UUID userId = submission.getUserId();
    UUID challengeId = submission.getChallengeId();
    UserProgressEntity progress =
        progressRepository
            .findByUserIdAndChallengeId(userId, challengeId)
            .orElse(
                new UserProgressEntity(
                    UUID.randomUUID(),
                    userId,
                    challengeId,
                    ProgressState.NOT_STARTED,
                    clock.instant()));
    progress.setState(blocked ? ProgressState.FAILED : ProgressState.PASSED);
    progress.setSubmittedAt(clock.instant());
    progress.setUpdatedAt(clock.instant());
    progressRepository.save(progress);
  }
}
