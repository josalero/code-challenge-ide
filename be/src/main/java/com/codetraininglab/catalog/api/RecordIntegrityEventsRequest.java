package com.codetraininglab.catalog.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record RecordIntegrityEventsRequest(
    @NotEmpty @Size(max = 50) List<@Valid IntegrityEventRequest> events) {

  public record IntegrityEventRequest(
      String eventType,
      String editorSurface,
      Integer charCount,
      Long awayMs,
      Instant occurredAt) {}
}
