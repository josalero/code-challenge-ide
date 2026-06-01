package com.codetraininglab.platform.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "challenge_public_tests")
public class ChallengePublicTestEntity {

  @Id
  private UUID id;

  @Column(name = "challenge_id", nullable = false)
  private UUID challengeId;

  @Column(nullable = false)
  private String name;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Column(nullable = false)
  private String description;

  protected ChallengePublicTestEntity() {}

  public ChallengePublicTestEntity(
      UUID id, UUID challengeId, String name, String description, int sortOrder) {
    this.id = id;
    this.challengeId = challengeId;
    this.name = name;
    this.description = description == null ? "" : description;
    this.sortOrder = sortOrder;
  }

  public UUID getChallengeId() {
    return challengeId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description == null ? "" : description;
  }
}
