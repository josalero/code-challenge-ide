package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submissions")
public class SubmissionEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "challenge_id", nullable = false)
  private UUID challengeId;

  @Column(name = "runtime_id", nullable = false)
  private UUID runtimeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SubmissionStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SubmissionKind kind;

  @Column(name = "solution_code", nullable = false, columnDefinition = "TEXT")
  private String solutionCode;

  @Column(name = "custom_tests_code", columnDefinition = "TEXT")
  private String customTestsCode;

  @Column(name = "idempotency_key")
  private String idempotencyKey;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected SubmissionEntity() {}

  public SubmissionEntity(
      UUID id,
      UUID userId,
      UUID challengeId,
      UUID runtimeId,
      SubmissionStatus status,
      SubmissionKind kind,
      String solutionCode,
      String customTestsCode,
      String idempotencyKey,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.challengeId = challengeId;
    this.runtimeId = runtimeId;
    this.status = status;
    this.kind = kind;
    this.solutionCode = solutionCode;
    this.customTestsCode = customTestsCode;
    this.idempotencyKey = idempotencyKey;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getChallengeId() {
    return challengeId;
  }

  public UUID getRuntimeId() {
    return runtimeId;
  }

  public SubmissionStatus getStatus() {
    return status;
  }

  public void setStatus(SubmissionStatus status) {
    this.status = status;
  }

  public SubmissionKind getKind() {
    return kind;
  }

  public String getSolutionCode() {
    return solutionCode;
  }

  public String getCustomTestsCode() {
    return customTestsCode;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
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
