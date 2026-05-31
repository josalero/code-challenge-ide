package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SubmissionEventHubTest {

  @Test
  void subscribesAndPublishes() {
    SubmissionEventHub hub = new SubmissionEventHub();
    UUID id = UUID.randomUUID();
    SseEmitter emitter = hub.subscribe(id);
    assertThat(emitter).isNotNull();
    hub.publish(id, "status", Map.of("status", "RUNNING"));
  }
}
