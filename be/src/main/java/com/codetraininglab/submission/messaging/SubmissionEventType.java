package com.codetraininglab.submission.messaging;

public enum SubmissionEventType {
  STATUS("status"),
  TEST_RESULT("test_result"),
  DONE("done"),
  ERROR("error");

  private final String eventName;

  SubmissionEventType(String eventName) {
    this.eventName = eventName;
  }

  public String eventName() {
    return eventName;
  }
}
