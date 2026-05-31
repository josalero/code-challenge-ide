package com.codetraininglab.platform.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "submission_reports")
public class SubmissionReportEntity {

  @Id
  private UUID id;

  @Column(name = "submission_id", nullable = false, unique = true)
  private UUID submissionId;

  @Column(name = "schema_version", nullable = false)
  private int schemaVersion;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private String summary;

  @Column(nullable = false)
  private boolean blocked;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected SubmissionReportEntity() {}

  public SubmissionReportEntity(
      UUID id, UUID submissionId, int schemaVersion, String summary, boolean blocked, Instant createdAt) {
    this.id = id;
    this.submissionId = submissionId;
    this.schemaVersion = schemaVersion;
    this.summary = summary;
    this.blocked = blocked;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getSubmissionId() {
    return submissionId;
  }

  public String getSummary() {
    return summary;
  }

  public boolean isBlocked() {
    return blocked;
  }
}
