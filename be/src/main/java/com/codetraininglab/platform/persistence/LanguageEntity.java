package com.codetraininglab.platform.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "languages")
public class LanguageEntity {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  protected LanguageEntity() {}

  public LanguageEntity(UUID id, String name, String displayName) {
    this.id = id;
    this.name = name;
    this.displayName = displayName;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDisplayName() {
    return displayName;
  }
}
