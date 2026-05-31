package com.codetraininglab.submission.application;

import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.RunnerStatus;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.feedback.application.FeedbackAggregator;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionReportRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.integration.runner.RunnerClient;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.submission.messaging.SsePayloadKeys;
import com.codetraininglab.submission.messaging.SubmissionEventType;
import tools.jackson.databind.json.JsonMapper;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionProcessor {

  private static final Logger log = LoggerFactory.getLogger(SubmissionProcessor.class);

  private final SubmissionRepository submissionRepository;
  private final SubmissionReportRepository reportRepository;
  private final FeedbackItemRepository feedbackItemRepository;
  private final ChallengeRepository challengeRepository;
  private final ChallengeHiddenTestRepository hiddenTestRepository;
  private final LanguageRuntimeRepository runtimeRepository;
  private final UserProgressRepository progressRepository;
  private final RunnerClient runnerClient;
  private final CtlProperties properties;
  private final SubmissionEventHub eventHub;
  private final JsonMapper jsonMapper;
  private final Clock clock;
  private final TaskScheduler taskScheduler;

  public SubmissionProcessor(
      SubmissionRepository submissionRepository,
      SubmissionReportRepository reportRepository,
      FeedbackItemRepository feedbackItemRepository,
      ChallengeRepository challengeRepository,
      ChallengeHiddenTestRepository hiddenTestRepository,
      LanguageRuntimeRepository runtimeRepository,
      UserProgressRepository progressRepository,
      RunnerClient runnerClient,
      CtlProperties properties,
      SubmissionEventHub eventHub,
      JsonMapper jsonMapper,
      Clock clock,
      TaskScheduler taskScheduler) {
    this.submissionRepository = submissionRepository;
    this.reportRepository = reportRepository;
    this.feedbackItemRepository = feedbackItemRepository;
    this.challengeRepository = challengeRepository;
    this.hiddenTestRepository = hiddenTestRepository;
    this.runtimeRepository = runtimeRepository;
    this.progressRepository = progressRepository;
    this.runnerClient = runnerClient;
    this.properties = properties;
    this.eventHub = eventHub;
    this.jsonMapper = jsonMapper;
    this.clock = clock;
    this.taskScheduler = taskScheduler;
  }

  @Transactional
  public void process(UUID submissionId) {
    try {
      processInternal(submissionId);
    } catch (Exception e) {
      log.error("Unexpected error processing submission {}", submissionId, e);
      markInfrastructureFailure(submissionId, "Submission processing failed", null);
      throw e;
    }
  }

  private void processInternal(UUID submissionId) {
    SubmissionEntity submission =
        submissionRepository.findById(submissionId).orElse(null);
    if (submission == null || submission.getStatus() == SubmissionStatus.CANCELLED) {
      return;
    }
    submission.setStatus(SubmissionStatus.RUNNING);
    submission.setUpdatedAt(clock.instant());
    submissionRepository.save(submission);

    ChallengeEntity challenge =
        challengeRepository.findById(submission.getChallengeId()).orElseThrow();
    Path challengeDir = Path.of(properties.challengesPath(), challenge.getSlug());
    LanguageRuntimeEntity runtime =
        runtimeRepository.findById(submission.getRuntimeId()).orElseThrow();

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
        "Launching Docker runner (" + runtime.getDockerImage() + ")");

    Instant dockerStarted = clock.instant();
    ScheduledFuture<?> heartbeat =
        taskScheduler.scheduleAtFixedRate(
            () -> {
              long seconds = Duration.between(dockerStarted, clock.instant()).getSeconds();
              publishStatus(
                  submissionId,
                  SubmissionStatus.RUNNING,
                  "Docker: compiling & running tests ("
                      + seconds
                      + "s) — first run can take 2–3 minutes");
            },
            Instant.now().plusSeconds(10),
            Duration.ofSeconds(10));

    RunnerResult result;
    try {
      result =
          runnerClient.execute(
              submission,
              challenge.getSlug(),
              hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(challenge.getId()),
              challengeDir,
              runtime.getDockerImage());
    } finally {
      heartbeat.cancel(false);
    }

    publishStatus(
        submissionId,
        SubmissionStatus.RUNNING,
        "Runner finished — publishing "
            + result.tests().size()
            + " test result(s)");

    if (isInfrastructureFailure(result)) {
      markInfrastructureFailure(
          submissionId,
          result.tests().isEmpty()
              ? "Runner failed"
              : result.tests().getFirst().message(),
          result.logs());
      return;
    }

    publishStatus(
        submissionId, SubmissionStatus.RUNNING, "Aggregating coverage, style, and coach report");

    UUID reportId = UUID.randomUUID();
    var aggregated =
        FeedbackAggregator.aggregate(
            reportId, submissionId, result, challenge.getGatingConfig(), jsonMapper, clock);
    reportRepository.save(aggregated.report());
    feedbackItemRepository.saveAll(aggregated.items());

    submission.setStatus(SubmissionStatus.COMPLETED);
    submission.setUpdatedAt(clock.instant());
    submissionRepository.save(submission);

    updateProgress(submission, aggregated.blocked());
    for (RunnerResult.TestOutcome test : result.tests()) {
      HashMap<String, Object> payload = new HashMap<>();
      payload.put(SsePayloadKeys.NAME, test.name());
      payload.put(SsePayloadKeys.STATUS, test.status());
      if (test.message() != null) {
        payload.put(SsePayloadKeys.MESSAGE, test.message());
      }
      eventHub.publish(submissionId, SubmissionEventType.TEST_RESULT.eventName(), payload);
    }
    eventHub.publish(
        submissionId,
        SubmissionEventType.DONE.eventName(),
        Map.of(SsePayloadKeys.SUBMISSION_ID, submissionId, SsePayloadKeys.REPORT_ID, reportId));
  }

  private void publishStatus(UUID submissionId, SubmissionStatus status, String message) {
    HashMap<String, Object> payload = new HashMap<>();
    payload.put(SsePayloadKeys.STATUS, status.name());
    payload.put(SsePayloadKeys.MESSAGE, message);
    eventHub.publish(submissionId, SubmissionEventType.STATUS.eventName(), payload);
  }

  private static boolean isInfrastructureFailure(RunnerResult result) {
    if (result == null || result.status() == null) {
      return true;
    }
    RunnerStatus status = RunnerStatus.fromString(result.status());
    return status == null || status.isInfrastructureFailure();
  }

  private void markInfrastructureFailure(
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

  private void updateProgress(SubmissionEntity submission, boolean blocked) {
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
    progress.setUpdatedAt(clock.instant());
    progressRepository.save(progress);
  }
}
