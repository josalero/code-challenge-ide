package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.FeedbackCategory;
import com.codetraininglab.domain.FeedbackStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feedback_items")
public class FeedbackItemEntity {

  @Id
  private UUID id;

  @Column(name = "report_id", nullable = false)
  private UUID reportId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FeedbackCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FeedbackStatus status;

  private String severity;

  private String message;

  @Column(name = "stable_id")
  private String stableId;

  @Column(name = "ai_explanation", columnDefinition = "TEXT")
  private String aiExplanation;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected FeedbackItemEntity() {}

  public FeedbackItemEntity(
      UUID id,
      UUID reportId,
      FeedbackCategory category,
      FeedbackStatus status,
      String severity,
      String message,
      String stableId,
      Instant createdAt) {
    this.id = id;
    this.reportId = reportId;
    this.category = category;
    this.status = status;
    this.severity = severity;
    this.message = message;
    this.stableId = stableId;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getReportId() {
    return reportId;
  }

  public FeedbackCategory getCategory() {
    return category;
  }

  public FeedbackStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public String getStableId() {
    return stableId;
  }

  public String getAiExplanation() {
    return aiExplanation;
  }

  public void setAiExplanation(String aiExplanation) {
    this.aiExplanation = aiExplanation;
  }
}
