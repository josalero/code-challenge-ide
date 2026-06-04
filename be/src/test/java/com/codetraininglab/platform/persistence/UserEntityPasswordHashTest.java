package com.codetraininglab.platform.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codetraininglab.domain.UserRole;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserEntityPasswordHashTest {

  private static final String BCRYPT_HASH =
      new BCryptPasswordEncoder(12).encode("Password1");

  @Test
  void rejectsPlaintextPasswordHash() {
    assertThatThrownBy(
            () ->
                new UserEntity(
                    UUID.randomUUID(),
                    "a@b.com",
                    "Password1",
                    UserRole.USER,
                    Instant.EPOCH,
                    Instant.EPOCH))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("BCrypt hash");
  }

  @Test
  void acceptsBcryptPasswordHash() {
    new UserEntity(
        UUID.randomUUID(),
        "a@b.com",
        BCRYPT_HASH,
        UserRole.USER,
        Instant.EPOCH,
        Instant.EPOCH);
  }
}
