package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RunnerWarmImageIdsTest {

  @Test
  void matchesIgnoresSha256Prefix() {
    assertThat(
            RunnerWarmImageIds.matches(
                "sha256:fc84662de353",
                "fc84662de353d19be90802a8bc5ee886b12efde182207de094f6073e70bafe62"))
        .isFalse();
    assertThat(
            RunnerWarmImageIds.matches(
                "sha256:fc84662de353d19be90802a8bc5ee886b12efde182207de094f6073e70bafe62",
                "sha256:fc84662de353d19be90802a8bc5ee886b12efde182207de094f6073e70bafe62"))
        .isTrue();
  }
}
