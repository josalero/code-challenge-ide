package com.codetraininglab.catalog.api;

import jakarta.validation.constraints.NotBlank;

public record ChallengeTestPayload(
    @NotBlank String name, @NotBlank String source, String description) {

  public ChallengeTestPayload(String name, String source) {
    this(name, source, "");
  }
}
