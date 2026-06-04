package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ChallengeProgressCalculatorTest {

  @Test
  void completionPercentUsesStartedChallengesAsDenominator() {
    assertThat(ChallengeProgressCalculator.completionPercent(1, 2)).isEqualTo(50);
    assertThat(ChallengeProgressCalculator.completionPercent(1, 1)).isEqualTo(100);
    assertThat(ChallengeProgressCalculator.completionPercent(0, 0)).isZero();
  }
}
