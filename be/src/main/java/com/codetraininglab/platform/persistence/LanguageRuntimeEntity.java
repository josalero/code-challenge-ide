package com.codetraininglab.platform.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "language_runtimes")
public class LanguageRuntimeEntity {

  @Id
  private UUID id;

  @Column(name = "language_id", nullable = false)
  private UUID languageId;

  @Column(nullable = false)
  private String version;

  @Column(name = "docker_image", nullable = false)
  private String dockerImage;

  @Column(nullable = false)
  private boolean active;

  protected LanguageRuntimeEntity() {}

  public LanguageRuntimeEntity(UUID id, UUID languageId, String version, String dockerImage, boolean active) {
    this.id = id;
    this.languageId = languageId;
    this.version = version;
    this.dockerImage = dockerImage;
    this.active = active;
  }

  public UUID getId() {
    return id;
  }

  public UUID getLanguageId() {
    return languageId;
  }

  public String getVersion() {
    return version;
  }

  public String getDockerImage() {
    return dockerImage;
  }

  public boolean isActive() {
    return active;
  }
}
