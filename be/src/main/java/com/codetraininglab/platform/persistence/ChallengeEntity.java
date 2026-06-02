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
@Table(name = "challenges")
public class ChallengeEntity {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private String title;

  @Column(name = "description_md", nullable = false, columnDefinition = "TEXT")
  private String descriptionMd;

  @Column(name = "starter_code", nullable = false, columnDefinition = "TEXT")
  private String starterCode;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "gating_config", nullable = false, columnDefinition = "jsonb")
  private String gatingConfig;

  @Column(nullable = false)
  private String source;

  private String difficulty;

  @Column(nullable = false)
  private String language;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected ChallengeEntity() {}

  public ChallengeEntity(
      UUID id,
      String slug,
      String title,
      String descriptionMd,
      String starterCode,
      String gatingConfig,
      String source,
      String difficulty,
      String language,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.slug = slug;
    this.title = title;
    this.descriptionMd = descriptionMd;
    this.starterCode = starterCode;
    this.gatingConfig = gatingConfig;
    this.source = source;
    this.difficulty = difficulty;
    this.language = language;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public String getSlug() {
    return slug;
  }

  public String getTitle() {
    return title;
  }

  public String getDescriptionMd() {
    return descriptionMd;
  }

  public void setDescriptionMd(String descriptionMd) {
    this.descriptionMd = descriptionMd;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getStarterCode() {
    return starterCode;
  }

  public String getGatingConfig() {
    return gatingConfig;
  }

  public String getSource() {
    return source;
  }

  public String getDifficulty() {
    return difficulty;
  }

  public String getLanguage() {
    return language;
  }
}
