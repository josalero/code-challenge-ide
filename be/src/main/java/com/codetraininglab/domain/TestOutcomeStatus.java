package com.codetraininglab.domain;

public enum TestOutcomeStatus {
  PASS,
  FAIL,
  SKIP;

  public boolean matches(String value) {
    return value != null && name().equalsIgnoreCase(value.trim());
  }
}
