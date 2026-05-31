package com.codetraininglab.platform.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "challenge_hidden_tests")
public class ChallengeHiddenTestEntity {

  @Id
  private UUID id;

  @Column(name = "challenge_id", nullable = false)
  private UUID challengeId;

  @Column(nullable = false)
  private String name;

  @Column(name = "test_source", nullable = false, columnDefinition = "TEXT")
  private String testSource;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  protected ChallengeHiddenTestEntity() {}

  public ChallengeHiddenTestEntity(
      UUID id, UUID challengeId, String name, String testSource, int sortOrder) {
    this.id = id;
    this.challengeId = challengeId;
    this.name = name;
    this.testSource = testSource;
    this.sortOrder = sortOrder;
  }

  public UUID getChallengeId() {
    return challengeId;
  }

  public String getName() {
    return name;
  }

  public String getTestSource() {
    return testSource;
  }
}
