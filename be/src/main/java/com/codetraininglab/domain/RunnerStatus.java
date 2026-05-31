package com.codetraininglab.domain;

public enum RunnerStatus {
  COMPLETED,
  FAILED,
  TIMED_OUT;

  public static RunnerStatus fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return RunnerStatus.valueOf(value.trim().toUpperCase());
  }

  public boolean isInfrastructureFailure() {
    return this == FAILED || this == TIMED_OUT;
  }
}
