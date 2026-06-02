package com.codetraininglab.platform.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "runner_pool_warm_state")
public class RunnerPoolWarmStateEntity {

  @Id
  @Column(name = "docker_image", nullable = false, length = 512)
  private String dockerImage;

  @Column(name = "image_id", length = 256)
  private String imageId;

  @Column(name = "warmed", nullable = false)
  private boolean warmed;

  @Column(name = "warmed_at", nullable = false)
  private Instant warmedAt;

  protected RunnerPoolWarmStateEntity() {}

  public RunnerPoolWarmStateEntity(
      String dockerImage, String imageId, boolean warmed, Instant warmedAt) {
    this.dockerImage = dockerImage;
    this.imageId = imageId;
    this.warmed = warmed;
    this.warmedAt = warmedAt;
  }

  public String getDockerImage() {
    return dockerImage;
  }

  public String getImageId() {
    return imageId;
  }

  public Instant getWarmedAt() {
    return warmedAt;
  }

  public boolean isWarmed() {
    return warmed;
  }

  public void setImageId(String imageId) {
    this.imageId = imageId;
  }

  public void setWarmed(boolean warmed) {
    this.warmed = warmed;
  }

  public void setWarmedAt(Instant warmedAt) {
    this.warmedAt = warmedAt;
  }
}
