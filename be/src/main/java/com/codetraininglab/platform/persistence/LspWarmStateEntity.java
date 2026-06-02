package com.codetraininglab.platform.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "lsp_warm_state")
public class LspWarmStateEntity {

  @EmbeddedId
  private LspWarmStateId id;

  @Column(name = "image_id", length = 256)
  private String imageId;

  @Column(name = "warmed", nullable = false)
  private boolean warmed;

  @Column(name = "warmed_at", nullable = false)
  private Instant warmedAt;

  protected LspWarmStateEntity() {}

  public LspWarmStateEntity(
      String label, String dockerImage, String imageId, boolean warmed, Instant warmedAt) {
    this.id = new LspWarmStateId(label, dockerImage);
    this.imageId = imageId;
    this.warmed = warmed;
    this.warmedAt = warmedAt;
  }

  public LspWarmStateId getId() {
    return id;
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

  @Embeddable
  public static class LspWarmStateId implements Serializable {

    @Column(name = "label", nullable = false, length = 64)
    private String label;

    @Column(name = "docker_image", nullable = false, length = 512)
    private String dockerImage;

    protected LspWarmStateId() {}

    public LspWarmStateId(String label, String dockerImage) {
      this.label = label;
      this.dockerImage = dockerImage;
    }

    public String getLabel() {
      return label;
    }

    public String getDockerImage() {
      return dockerImage;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (other == null || getClass() != other.getClass()) {
        return false;
      }
      LspWarmStateId that = (LspWarmStateId) other;
      return Objects.equals(label, that.label) && Objects.equals(dockerImage, that.dockerImage);
    }

    @Override
    public int hashCode() {
      return Objects.hash(label, dockerImage);
    }
  }
}
