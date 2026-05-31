package com.codetraininglab.platform.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "custom_tests")
public class CustomTestsEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "challenge_id", nullable = false)
  private UUID challengeId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String code;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected CustomTestsEntity() {}

  public CustomTestsEntity(UUID id, UUID userId, UUID challengeId, String code, Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.challengeId = challengeId;
    this.code = code;
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

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
