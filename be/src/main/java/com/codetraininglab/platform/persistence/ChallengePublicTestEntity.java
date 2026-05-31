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

  protected ChallengePublicTestEntity() {}

  public ChallengePublicTestEntity(UUID id, UUID challengeId, String name, int sortOrder) {
    this.id = id;
    this.challengeId = challengeId;
    this.name = name;
    this.sortOrder = sortOrder;
  }

  public UUID getChallengeId() {
    return challengeId;
  }

  public String getName() {
    return name;
  }
}
