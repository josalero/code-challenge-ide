package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RunnerSmokeChallengesTest {

  @Test
  void mapsLanguagesToSmokeSlugs() {
    assertThat(RunnerSmokeChallenges.slugFor("java")).isEqualTo("reverse-string");
    assertThat(RunnerSmokeChallenges.slugFor("go")).isEqualTo("anagram-check-go");
    assertThat(RunnerSmokeChallenges.slugFor("react")).isEqualTo("accordion-react");
  }
}
