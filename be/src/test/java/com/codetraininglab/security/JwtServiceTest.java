package com.codetraininglab.platform.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.platform.config.CtlProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  @Test
  void roundTripsUserId() {
    CtlProperties properties =
        new CtlProperties(
            true,
            "test-jwt-secret-must-be-at-least-32-characters-long",
            24,
            "http://localhost:5173",
            "challenges",
            "runner",
            "",
            "lsp",
            5,
            24,
            "openrouter",
            "",
            "model",
            "http://localhost:11434",
            "ollama", false, false);
    JwtService jwtService = new JwtService(properties, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    UUID userId = UUID.randomUUID();
    String token = jwtService.createToken(userId, "u@example.com");
    assertThat(jwtService.parseUserId(token)).isEqualTo(userId);
  }
}
