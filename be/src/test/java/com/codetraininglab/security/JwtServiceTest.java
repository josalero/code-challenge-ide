package com.codetraininglab.platform.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  @Test
  void roundTripsUserId() {
    JwtService jwtService =
        new JwtService(
            CtlPropertiesTestFixtures.defaults(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    UUID userId = UUID.randomUUID();
    String token = jwtService.createToken(userId, "u@example.com", UserRole.USER);
    assertThat(jwtService.parseUserId(token)).isEqualTo(userId);
  }

  @Test
  void roundTripsRole() {
    JwtService jwtService =
        new JwtService(
            CtlPropertiesTestFixtures.defaults(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    UUID userId = UUID.randomUUID();
    String token = jwtService.createToken(userId, "u@example.com", UserRole.ADMIN);
    assertThat(jwtService.parseRole(token)).isEqualTo(UserRole.ADMIN);
  }
}
