package com.codetraininglab.domain;

/** Kinds of on-demand feedback users can request on top of a submission's default test+coverage run. */
public enum FeedbackActionType {
  COACH,
  SONAR,
  COMPLEXITY;

  public static FeedbackActionType fromString(String value) {
    if (value == null) {
      throw new IllegalArgumentException("action is required");
    }
    try {
      return FeedbackActionType.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Unknown feedback action: " + value);
    }
  }
}
