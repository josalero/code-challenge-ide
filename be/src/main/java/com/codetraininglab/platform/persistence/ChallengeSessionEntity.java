package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.ChallengeSessionEndReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "challenge_sessions")
public class ChallengeSessionEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "challenge_id", nullable = false)
  private UUID challengeId;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "ended_at")
  private Instant endedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "end_reason")
  private ChallengeSessionEndReason endReason;

  protected ChallengeSessionEntity() {}

  public ChallengeSessionEntity(
      UUID id,
      UUID userId,
      UUID challengeId,
      Instant startedAt,
      Instant expiresAt) {
    this.id = id;
    this.userId = userId;
    this.challengeId = challengeId;
    this.startedAt = startedAt;
    this.expiresAt = expiresAt;
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

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getEndedAt() {
    return endedAt;
  }

  public ChallengeSessionEndReason getEndReason() {
    return endReason;
  }

  public boolean isActive(Instant now) {
    return endedAt == null && !expiresAt.isBefore(now);
  }

  public boolean isExpired(Instant now) {
    return endedAt == null && expiresAt.isBefore(now);
  }

  public void end(Instant endedAt, ChallengeSessionEndReason reason) {
    this.endedAt = endedAt;
    this.endReason = reason;
  }
}
