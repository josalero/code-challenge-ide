package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "users")
public class UserEntity {

  private static final Pattern BCRYPT_HASH =
      Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

  @Id
  private UUID id;

  @Column(nullable = false)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "password_must_change", nullable = false)
  private boolean passwordMustChange;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  /** Null = platform default; 0 = unlimited; positive = explicit cap for learners. */
  @Column(name = "max_started_challenges")
  private Integer maxStartedChallenges;

  protected UserEntity() {}

  public UserEntity(
      UUID id,
      String email,
      String passwordHash,
      UserRole role,
      Instant createdAt,
      Instant updatedAt) {
    this(id, email, passwordHash, role, createdAt, updatedAt, null, false);
  }

  public UserEntity(
      UUID id,
      String email,
      String passwordHash,
      UserRole role,
      Instant createdAt,
      Instant updatedAt,
      String fullName,
      boolean passwordMustChange) {
    this.id = id;
    this.email = email;
    assignPasswordHash(passwordHash);
    this.role = role;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.fullName = fullName;
    this.passwordMustChange = passwordMustChange;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getFullName() {
    return fullName;
  }

  public boolean isPasswordMustChange() {
    return passwordMustChange;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public UserRole getRole() {
    return role;
  }

  public Integer getMaxStartedChallenges() {
    return maxStartedChallenges;
  }

  public void setMaxStartedChallenges(Integer maxStartedChallenges) {
    this.maxStartedChallenges = maxStartedChallenges;
  }

  public void setPasswordHash(String passwordHash) {
    assignPasswordHash(passwordHash);
  }

  private void assignPasswordHash(String passwordHash) {
    if (passwordHash == null || !BCRYPT_HASH.matcher(passwordHash).matches()) {
      throw new IllegalArgumentException(
          "password_hash must be a BCrypt hash, never a plaintext password");
    }
    this.passwordHash = passwordHash;
  }

  public void setPasswordMustChange(boolean passwordMustChange) {
    this.passwordMustChange = passwordMustChange;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void markDeleted(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }
}
