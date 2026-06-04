package com.codetraininglab.platform.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ops_platform_state")
public class OpsPlatformStateEntity {

  public static final String DEFAULT_ID = "default";

  @Id
  private String id;

  @Column(name = "last_warm_up_at")
  private Instant lastWarmUpAt;

  protected OpsPlatformStateEntity() {}

  public OpsPlatformStateEntity(String id, Instant lastWarmUpAt) {
    this.id = id;
    this.lastWarmUpAt = lastWarmUpAt;
  }

  public String getId() {
    return id;
  }

  public Instant getLastWarmUpAt() {
    return lastWarmUpAt;
  }

  public void setLastWarmUpAt(Instant lastWarmUpAt) {
    this.lastWarmUpAt = lastWarmUpAt;
  }
}
