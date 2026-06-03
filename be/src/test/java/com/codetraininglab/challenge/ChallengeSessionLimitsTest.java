package com.codetraininglab.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.catalog.application.ChallengeSessionLimits;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ChallengeSessionLimitsTest {

  @Test
  void resolvesConfiguredMinutesBeforeDifficultyDefault() {
    assertThat(ChallengeSessionLimits.resolveMinutes(45, "easy")).isEqualTo(45);
    assertThat(ChallengeSessionLimits.resolveMinutes(null, "easy")).isEqualTo(30);
    assertThat(ChallengeSessionLimits.resolveMinutes(null, "hard")).isEqualTo(60);
  }

  @Test
  void parsesSessionDurationFromLimitsBlock() {
    Map<String, Object> yaml =
        Map.of("limits", Map.of("session_duration_minutes", 25, "per_test_timeout_seconds", 10));
    assertThat(ChallengeSessionLimits.parseSessionDurationMinutes(yaml)).isEqualTo(25);
  }
}
