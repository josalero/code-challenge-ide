package com.codetraininglab.submission.application;

import com.codetraininglab.catalog.application.ChallengeQuotaService;
import com.codetraininglab.catalog.application.ChallengeSessionService;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.FeedbackItemEntity;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionReportRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.SubmissionKind;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.codetraininglab.platform.config.RabbitMqConfig;
import com.codetraininglab.submission.api.CreateSubmissionRequest;
import com.codetraininglab.submission.api.FeedbackItemResponse;
import com.codetraininglab.submission.api.ReportResponse;
import com.codetraininglab.submission.api.RunnerLogsResponse;
import com.codetraininglab.submission.api.SubmissionResponse;
import com.codetraininglab.submission.messaging.SsePayloadKeys;
import com.codetraininglab.submission.messaging.SubmissionEventType;
import com.codetraininglab.submission.messaging.SubmissionJobMessage;
import tools.jackson.databind.json.JsonMapper;

@Service
public class SubmissionService {

  private final SubmissionRepository submissionRepository;
  private final SubmissionReportRepository reportRepository;
  private final FeedbackItemRepository feedbackItemRepository;
  private final ChallengeRepository challengeRepository;
  private final LanguageRuntimeResolver runtimeResolver;
  private final UserProgressRepository progressRepository;
  private final ChallengeQuotaService challengeQuotaService;
  private final ChallengeSessionService sessionService;
  private final RabbitTemplate rabbitTemplate;
  private final SubmissionEventHub eventHub;
  private final CtlProperties properties;
  private final JsonMapper jsonMapper;
  private final Clock clock;

  public SubmissionService(
      SubmissionRepository submissionRepository,
      SubmissionReportRepository reportRepository,
      FeedbackItemRepository feedbackItemRepository,
      ChallengeRepository challengeRepository,
      LanguageRuntimeResolver runtimeResolver,
      UserProgressRepository progressRepository,
      ChallengeQuotaService challengeQuotaService,
      ChallengeSessionService sessionService,
      RabbitTemplate rabbitTemplate,
      SubmissionEventHub eventHub,
      CtlProperties properties,
      JsonMapper jsonMapper,
      Clock clock) {
    this.submissionRepository = submissionRepository;
    this.reportRepository = reportRepository;
    this.feedbackItemRepository = feedbackItemRepository;
    this.challengeRepository = challengeRepository;
    this.runtimeResolver = runtimeResolver;
    this.progressRepository = progressRepository;
    this.challengeQuotaService = challengeQuotaService;
    this.sessionService = sessionService;
    this.rabbitTemplate = rabbitTemplate;
    this.eventHub = eventHub;
    this.properties = properties;
    this.jsonMapper = jsonMapper;
    this.clock = clock;
  }

  @Transactional
  public SubmissionResponse create(
      UUID userId, CreateSubmissionRequest request, String idempotencyKey) {
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      Instant since = clock.instant().minus(properties.idempotencyTtlHours(), ChronoUnit.HOURS);
      var existing = submissionRepository.findIdempotent(userId, idempotencyKey, since);
      if (existing.isPresent()) {
        return toResponse(existing.get());
      }
    }
    ChallengeEntity challenge =
        challengeRepository
            .findBySlug(request.challengeSlug())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
    LanguageRuntimeEntity runtime = runtimeResolver.resolve(challenge, request.runtimeVersion());
    SubmissionKind kind = resolveKind(request.kind());
    challengeQuotaService.ensureMayStartChallenge(userId, challenge.getId());
    sessionService.ensureMayRunOrSubmit(userId, challenge.getId());
    if (kind == SubmissionKind.SUBMIT) {
      ensureExerciseNotLocked(userId, challenge.getId());
    }
    Instant now = clock.instant();
    SubmissionEntity entity =
        new SubmissionEntity(
            UUID.randomUUID(),
            userId,
            challenge.getId(),
            runtime.getId(),
            SubmissionStatus.PENDING,
            kind,
            request.solutionCode(),
            request.customTestsCode(),
            idempotencyKey,
            now,
            now);
    submissionRepository.save(entity);
    if (kind == SubmissionKind.SUBMIT) {
      sessionService.endOnGradedSubmit(userId, challenge.getId());
    }
    touchProgress(userId, challenge.getId(), now);
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.SUBMISSION_QUEUE, new SubmissionJobMessage(entity.getId()));
    Map<String, Object> queued = new HashMap<>();
    queued.put(SsePayloadKeys.STATUS, SubmissionStatus.PENDING.name());
    queued.put(SsePayloadKeys.KIND, kind.name());
    queued.put(
        SsePayloadKeys.MESSAGE,
        (kind == SubmissionKind.RUN ? "Practice run" : "Final submit")
            + " queued — "
            + challenge.getLanguage()
            + " "
            + runtime.getVersion());
    eventHub.publish(entity.getId(), SubmissionEventType.STATUS.eventName(), queued);
    return toResponse(entity);
  }

  public SubmissionResponse get(UUID userId, UUID submissionId) {
    SubmissionEntity entity =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!entity.getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    return toResponse(entity);
  }

  @Transactional
  public void cancel(UUID userId, UUID submissionId) {
    SubmissionEntity entity =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!entity.getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    if (entity.getStatus() != SubmissionStatus.PENDING
        && entity.getStatus() != SubmissionStatus.RUNNING) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Submission cannot be cancelled");
    }
    entity.setStatus(SubmissionStatus.CANCELLED);
    entity.setUpdatedAt(clock.instant());
    submissionRepository.save(entity);
  }

  public ReportResponse getReport(UUID userId, UUID reportId) {
    var report =
        reportRepository
            .findById(reportId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    SubmissionEntity submission =
        submissionRepository
            .findById(report.getSubmissionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!submission.getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    List<FeedbackItemResponse> feedback =
        feedbackItemRepository.findByReportIdOrderByCategoryAsc(report.getId()).stream()
            .map(SubmissionService::toFeedbackResponse)
            .toList();
    RunnerLogsResponse runnerLogs =
        ReportSummarySupport.parseRunnerLogs(report.getSummary(), jsonMapper);
    return new ReportResponse(
        report.getId(),
        report.getSubmissionId(),
        report.isBlocked(),
        report.getSummary(),
        feedback,
        runnerLogs);
  }

  private static FeedbackItemResponse toFeedbackResponse(FeedbackItemEntity item) {
    return new FeedbackItemResponse(
        item.getId(),
        item.getCategory().name(),
        item.getStatus().name(),
        item.getMessage(),
        item.getAiExplanation());
  }

  private void touchProgress(UUID userId, UUID challengeId, Instant now) {
    progressRepository
        .findByUserIdAndChallengeId(userId, challengeId)
        .ifPresentOrElse(
            p -> {
              if (p.getState() == ProgressState.NOT_STARTED) {
                p.setState(ProgressState.ATTEMPTED);
                p.setUpdatedAt(now);
                progressRepository.save(p);
              }
            },
            () ->
                progressRepository.save(
                    new UserProgressEntity(
                        UUID.randomUUID(), userId, challengeId, ProgressState.ATTEMPTED, now)));
  }

  private static SubmissionKind resolveKind(String raw) {
    try {
      return SubmissionKind.fromRequest(raw);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  private void ensureExerciseNotLocked(UUID userId, UUID challengeId) {
    progressRepository
        .findByUserIdAndChallengeId(userId, challengeId)
        .filter(UserProgressEntity::isSubmitted)
        .ifPresent(
            ignored -> {
              throw new ResponseStatusException(
                  HttpStatus.CONFLICT,
                  "This exercise was already submitted. Use Redo to start a new attempt.");
            });
  }

  private SubmissionResponse toResponse(SubmissionEntity entity) {
    UUID reportId =
        reportRepository
            .findBySubmissionId(entity.getId())
            .map(r -> r.getId())
            .orElse(null);
    return new SubmissionResponse(
        entity.getId(),
        entity.getStatus().name(),
        entity.getKind().name(),
        reportId,
        entity.getCreatedAt());
  }
}
