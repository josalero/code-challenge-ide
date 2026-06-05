package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.IntegrityEditorSurface;
import com.codetraininglab.domain.IntegrityEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "challenge_integrity_events")
public class ChallengeIntegrityEventEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "challenge_id", nullable = false)
  private UUID challengeId;

  @Column(name = "session_id")
  private UUID sessionId;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  private IntegrityEventType eventType;

  @Enumerated(EnumType.STRING)
  @Column(name = "editor_surface")
  private IntegrityEditorSurface editorSurface;

  @Column(name = "char_count")
  private Integer charCount;

  @Column(name = "away_ms")
  private Long awayMs;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  protected ChallengeIntegrityEventEntity() {}

  public ChallengeIntegrityEventEntity(
      UUID id,
      UUID userId,
      UUID challengeId,
      UUID sessionId,
      IntegrityEventType eventType,
      IntegrityEditorSurface editorSurface,
      Integer charCount,
      Long awayMs,
      Instant occurredAt) {
    this.id = id;
    this.userId = userId;
    this.challengeId = challengeId;
    this.sessionId = sessionId;
    this.eventType = eventType;
    this.editorSurface = editorSurface;
    this.charCount = charCount;
    this.awayMs = awayMs;
    this.occurredAt = occurredAt;
  }

  public UUID getId() {
    return id;
  }

  public IntegrityEventType getEventType() {
    return eventType;
  }

  public IntegrityEditorSurface getEditorSurface() {
    return editorSurface;
  }

  public Integer getCharCount() {
    return charCount;
  }

  public Long getAwayMs() {
    return awayMs;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }
}
