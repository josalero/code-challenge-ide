package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.FeedbackActionStatus;
import com.codetraininglab.domain.FeedbackActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submission_feedback_actions")
public class SubmissionFeedbackActionEntity {

  @Id private UUID id;

  @Column(name = "submission_id", nullable = false)
  private UUID submissionId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FeedbackActionType action;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FeedbackActionStatus status;

  @Column(columnDefinition = "TEXT")
  private String result;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected SubmissionFeedbackActionEntity() {}

  public SubmissionFeedbackActionEntity(
      UUID id,
      UUID submissionId,
      FeedbackActionType action,
      FeedbackActionStatus status,
      Instant createdAt) {
    this.id = id;
    this.submissionId = submissionId;
    this.action = action;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getSubmissionId() {
    return submissionId;
  }

  public FeedbackActionType getAction() {
    return action;
  }

  public FeedbackActionStatus getStatus() {
    return status;
  }

  public void setStatus(FeedbackActionStatus status) {
    this.status = status;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
