package com.codetraininglab.feedback.api;

import com.codetraininglab.platform.persistence.SubmissionFeedbackActionEntity;
import java.time.Instant;
import java.util.UUID;

public record FeedbackActionResponse(
    UUID id,
    UUID submissionId,
    String action,
    String status,
    String result,
    String errorMessage,
    Instant createdAt,
    Instant updatedAt) {

  public static FeedbackActionResponse from(SubmissionFeedbackActionEntity entity) {
    return new FeedbackActionResponse(
        entity.getId(),
        entity.getSubmissionId(),
        entity.getAction().name(),
        entity.getStatus().name(),
        entity.getResult(),
        entity.getErrorMessage(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
