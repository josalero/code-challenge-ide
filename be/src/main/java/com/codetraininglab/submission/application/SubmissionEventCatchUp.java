package com.codetraininglab.submission.application;

import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.submission.messaging.SsePayloadKeys;
import java.util.HashMap;
import java.util.Map;

public final class SubmissionEventCatchUp {

  private SubmissionEventCatchUp() {}

  public static Map<String, Object> statusPayload(String status) {
    HashMap<String, Object> payload = new HashMap<>();
    payload.put(SsePayloadKeys.STATUS, status);
    payload.put(SsePayloadKeys.MESSAGE, messageFor(status));
    return payload;
  }

  static String messageFor(String status) {
    if (SubmissionStatus.PENDING.name().equals(status)) {
      return "Queued — waiting for worker";
    }
    if (SubmissionStatus.RUNNING.name().equals(status)) {
      return "Run in progress — Docker is compiling and testing (often 1–3 min)";
    }
    if (SubmissionStatus.COMPLETED.name().equals(status)) {
      return "Run completed";
    }
    if (SubmissionStatus.FAILED.name().equals(status)) {
      return "Run failed";
    }
    if (SubmissionStatus.CANCELLED.name().equals(status)) {
      return "Run cancelled";
    }
    return "Current status: " + status;
  }
}
