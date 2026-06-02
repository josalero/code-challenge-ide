package com.codetraininglab.domain;

public enum SubmissionKind {
  RUN,
  SUBMIT;

  public static SubmissionKind fromRequest(String raw) {
    if (raw == null || raw.isBlank()) {
      return SUBMIT;
    }
    try {
      return SubmissionKind.valueOf(raw.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Unsupported submission kind: " + raw);
    }
  }
}
