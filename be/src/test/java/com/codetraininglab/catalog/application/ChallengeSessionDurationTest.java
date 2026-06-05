package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.platform.persistence.ChallengeEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChallengeSessionDurationTest {

  private static final Instant NOW = Instant.parse("2026-06-04T12:00:00Z");

  @Test
  void usesConfiguredMinutesWhenPresent() {
    ChallengeEntity challenge = challenge("medium", 25);

    assertThat(ChallengeSessionDuration.durationSeconds(challenge)).isEqualTo(25 * 60);
  }

  @Test
  void fallsBackToEasyDuration() {
    ChallengeEntity challenge = challenge("easy", null);

    assertThat(ChallengeSessionDuration.durationSeconds(challenge))
        .isEqualTo(ChallengeSessionDuration.EASY_FALLBACK_MINUTES * 60);
  }

  @Test
  void fallsBackToStandardDurationForNonEasy() {
    ChallengeEntity challenge = challenge("hard", 0);

    assertThat(ChallengeSessionDuration.durationSeconds(challenge))
        .isEqualTo(ChallengeSessionDuration.STANDARD_FALLBACK_MINUTES * 60);
  }

  private static ChallengeEntity challenge(String difficulty, Integer sessionMinutes) {
    return new ChallengeEntity(
        UUID.randomUUID(),
        "slug",
        "Title",
        "desc",
        "code",
        "{}",
        "seed",
        difficulty,
        "java",
        sessionMinutes,
        NOW,
        NOW);
  }
}
