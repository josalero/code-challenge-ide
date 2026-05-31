package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.ProgressState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_progress")
public class UserProgressEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "challenge_id", nullable = false)
  private UUID challengeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProgressState state;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected UserProgressEntity() {}

  public UserProgressEntity(UUID id, UUID userId, UUID challengeId, ProgressState state, Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.challengeId = challengeId;
    this.state = state;
    this.updatedAt = updatedAt;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getChallengeId() {
    return challengeId;
  }

  public ProgressState getState() {
    return state;
  }

  public void setState(ProgressState state) {
    this.state = state;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
