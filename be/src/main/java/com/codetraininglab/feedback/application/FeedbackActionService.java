package com.codetraininglab.feedback.application;

import com.codetraininglab.domain.FeedbackActionStatus;
import com.codetraininglab.domain.FeedbackActionType;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionFeedbackActionEntity;
import com.codetraininglab.platform.persistence.SubmissionFeedbackActionRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import java.time.Clock;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Coordinates user-requested feedback actions (coach review, Sonar scan, complexity, …). Each
 * action runs asynchronously so the HTTP request returns immediately with a queued record the UI
 * can poll.
 */
@Service
public class FeedbackActionService {

  private static final Logger log = LoggerFactory.getLogger(FeedbackActionService.class);

  private final SubmissionRepository submissionRepository;
  private final SubmissionFeedbackActionRepository actionRepository;
  private final Map<FeedbackActionType, FeedbackActionHandler> handlers;
  private final Clock clock;

  public FeedbackActionService(
      SubmissionRepository submissionRepository,
      SubmissionFeedbackActionRepository actionRepository,
      List<FeedbackActionHandler> handlerBeans,
      Clock clock) {
    this.submissionRepository = submissionRepository;
    this.actionRepository = actionRepository;
    this.clock = clock;
    Map<FeedbackActionType, FeedbackActionHandler> map = new EnumMap<>(FeedbackActionType.class);
    for (FeedbackActionHandler handler : handlerBeans) {
      map.put(handler.type(), handler);
    }
    this.handlers = Map.copyOf(map);
  }

  @Transactional
  public SubmissionFeedbackActionEntity request(UUID userId, UUID submissionId, FeedbackActionType action) {
    SubmissionEntity submission = ownedSubmission(userId, submissionId);
    SubmissionFeedbackActionEntity entity =
        new SubmissionFeedbackActionEntity(
            UUID.randomUUID(),
            submission.getId(),
            action,
            FeedbackActionStatus.QUEUED,
            clock.instant());
    actionRepository.save(entity);
    runAsync(entity.getId());
    return entity;
  }

  @Transactional(readOnly = true)
  public SubmissionFeedbackActionEntity find(UUID userId, UUID actionId) {
    SubmissionFeedbackActionEntity entity =
        actionRepository
            .findById(actionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    ownedSubmission(userId, entity.getSubmissionId());
    return entity;
  }

  @Transactional(readOnly = true)
  public List<SubmissionFeedbackActionEntity> list(UUID userId, UUID submissionId) {
    ownedSubmission(userId, submissionId);
    return actionRepository.findBySubmissionIdOrderByCreatedAtDesc(submissionId);
  }

  private SubmissionEntity ownedSubmission(UUID userId, UUID submissionId) {
    SubmissionEntity submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!submission.getUserId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    return submission;
  }

  @Async
  void runAsync(UUID actionId) {
    SubmissionFeedbackActionEntity entity = markRunning(actionId);
    if (entity == null) {
      return;
    }
    FeedbackActionHandler handler = handlers.get(entity.getAction());
    if (handler == null) {
      finalizeFailure(actionId, "No handler registered for action " + entity.getAction());
      return;
    }
    SubmissionEntity submission =
        submissionRepository.findById(entity.getSubmissionId()).orElse(null);
    if (submission == null) {
      finalizeFailure(actionId, "Submission no longer exists");
      return;
    }
    try {
      String result = handler.execute(submission);
      finalizeSuccess(actionId, result);
    } catch (UnsupportedOperationException ex) {
      finalizeFailure(actionId, ex.getMessage());
    } catch (Exception ex) {
      log.warn("Feedback action {} failed", actionId, ex);
      finalizeFailure(actionId, "Action failed: " + ex.getMessage());
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  SubmissionFeedbackActionEntity markRunning(UUID actionId) {
    SubmissionFeedbackActionEntity entity = actionRepository.findById(actionId).orElse(null);
    if (entity == null) {
      return null;
    }
    entity.setStatus(FeedbackActionStatus.RUNNING);
    entity.setUpdatedAt(clock.instant());
    return actionRepository.save(entity);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void finalizeSuccess(UUID actionId, String result) {
    actionRepository
        .findById(actionId)
        .ifPresent(
            entity -> {
              entity.setStatus(FeedbackActionStatus.COMPLETED);
              entity.setResult(result);
              entity.setUpdatedAt(clock.instant());
              actionRepository.save(entity);
            });
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void finalizeFailure(UUID actionId, String message) {
    actionRepository
        .findById(actionId)
        .ifPresent(
            entity -> {
              entity.setStatus(FeedbackActionStatus.FAILED);
              entity.setErrorMessage(message);
              entity.setUpdatedAt(clock.instant());
              actionRepository.save(entity);
            });
  }
}
