package com.codetraininglab.identity.application;

public final class ChallengeProgressCalculator {

  private ChallengeProgressCalculator() {}

  /** Pass rate among challenges the learner has started (denominator excludes not-started catalog items). */
  public static int completionPercent(int passed, int started) {
    return started == 0 ? 0 : Math.round((passed * 100f) / started);
  }
}
