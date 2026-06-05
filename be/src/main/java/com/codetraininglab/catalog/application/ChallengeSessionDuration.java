package com.codetraininglab.catalog.application;

import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.domain.UserRole;

public final class ChallengeSessionDuration {

  public static final int EASY_FALLBACK_MINUTES = 30;
  public static final int STANDARD_FALLBACK_MINUTES = 60;

  private ChallengeSessionDuration() {}

  public static int durationSeconds(ChallengeEntity challenge) {
    Integer configured = challenge.getSessionDurationMinutes();
    if (configured != null && configured > 0) {
      return configured * 60;
    }
    String difficulty = challenge.getDifficulty();
    int fallbackMinutes =
        difficulty != null && "easy".equalsIgnoreCase(difficulty.trim())
            ? EASY_FALLBACK_MINUTES
            : STANDARD_FALLBACK_MINUTES;
    return fallbackMinutes * 60;
  }
}
