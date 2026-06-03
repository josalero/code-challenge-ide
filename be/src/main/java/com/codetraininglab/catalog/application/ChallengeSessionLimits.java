package com.codetraininglab.catalog.application;

import java.util.Map;

/** Resolves per-challenge session time limits from DB or {@code challenge.yml} limits. */
public final class ChallengeSessionLimits {

  public static final int EASY_DEFAULT_MINUTES = 30;
  public static final int STANDARD_DEFAULT_MINUTES = 60;
  public static final int MIN_MINUTES = 5;
  public static final int MAX_MINUTES = 480;

  private ChallengeSessionLimits() {}

  public static int resolveMinutes(Integer configuredMinutes, String difficulty) {
    if (configuredMinutes != null && configuredMinutes > 0) {
      return configuredMinutes;
    }
    return defaultMinutesForDifficulty(difficulty);
  }

  public static int defaultMinutesForDifficulty(String difficulty) {
    return difficulty != null && "easy".equalsIgnoreCase(difficulty.trim())
        ? EASY_DEFAULT_MINUTES
        : STANDARD_DEFAULT_MINUTES;
  }

  public static Integer parseSessionDurationMinutes(Map<String, Object> challengeYaml) {
    Object limitsRaw = challengeYaml.get("limits");
    if (!(limitsRaw instanceof Map<?, ?> limits)) {
      return null;
    }
    return parsePositiveInt(limits.get("session_duration_minutes"));
  }

  private static Integer parsePositiveInt(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      int minutes = number.intValue();
      return minutes > 0 ? minutes : null;
    }
    try {
      int minutes = Integer.parseInt(value.toString().trim());
      return minutes > 0 ? minutes : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
