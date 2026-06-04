package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.AccessRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_requests")
public class AccessRequestEntity {

  @Id
  private UUID id;

  @Column(nullable = false)
  private String email;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(columnDefinition = "TEXT")
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccessRequestStatus status;

  @Column(name = "review_notes", columnDefinition = "TEXT")
  private String reviewNotes;

  @Column(name = "reviewed_by_user_id")
  private UUID reviewedByUserId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  protected AccessRequestEntity() {}

  public AccessRequestEntity(
      UUID id,
      String email,
      String fullName,
      String message,
      AccessRequestStatus status,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.email = email;
    this.fullName = fullName;
    this.message = message;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getFullName() {
    return fullName;
  }

  public String getMessage() {
    return message;
  }

  public AccessRequestStatus getStatus() {
    return status;
  }

  public String getReviewNotes() {
    return reviewNotes;
  }

  public UUID getReviewedByUserId() {
    return reviewedByUserId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getReviewedAt() {
    return reviewedAt;
  }

  public void setStatus(AccessRequestStatus status) {
    this.status = status;
  }

  public void setReviewNotes(String reviewNotes) {
    this.reviewNotes = reviewNotes;
  }

  public void setReviewedByUserId(UUID reviewedByUserId) {
    this.reviewedByUserId = reviewedByUserId;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setReviewedAt(Instant reviewedAt) {
    this.reviewedAt = reviewedAt;
  }
}
