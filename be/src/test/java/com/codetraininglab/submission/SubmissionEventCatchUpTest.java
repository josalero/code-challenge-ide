package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.submission.messaging.SsePayloadKeys;
import org.junit.jupiter.api.Test;

class SubmissionEventCatchUpTest {

  @Test
  void statusPayloadIncludesMessageForPending() {
    var payload = SubmissionEventCatchUp.statusPayload(SubmissionStatus.PENDING.name());
    assertThat(payload.get(SsePayloadKeys.STATUS)).isEqualTo("PENDING");
    assertThat(payload.get(SsePayloadKeys.MESSAGE)).isEqualTo("Queued — waiting for worker");
  }

  @Test
  void statusPayloadIncludesMessageForRunning() {
    var payload = SubmissionEventCatchUp.statusPayload(SubmissionStatus.RUNNING.name());
    assertThat(payload.get(SsePayloadKeys.MESSAGE)).asString().contains("Run in progress");
  }

  @Test
  void statusPayloadIncludesMessageForCompleted() {
    var payload = SubmissionEventCatchUp.statusPayload(SubmissionStatus.COMPLETED.name());
    assertThat(payload.get(SsePayloadKeys.MESSAGE)).isEqualTo("Run completed");
  }

  @Test
  void statusPayloadIncludesMessageForFailed() {
    var payload = SubmissionEventCatchUp.statusPayload(SubmissionStatus.FAILED.name());
    assertThat(payload.get(SsePayloadKeys.MESSAGE)).isEqualTo("Run failed");
  }

  @Test
  void statusPayloadIncludesMessageForCancelled() {
    var payload = SubmissionEventCatchUp.statusPayload(SubmissionStatus.CANCELLED.name());
    assertThat(payload.get(SsePayloadKeys.MESSAGE)).isEqualTo("Run cancelled");
  }

  @Test
  void statusPayloadFallsBackForUnknownStatus() {
    var payload = SubmissionEventCatchUp.statusPayload("CUSTOM");
    assertThat(payload.get(SsePayloadKeys.MESSAGE)).isEqualTo("Current status: CUSTOM");
  }
}
