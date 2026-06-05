package com.codetraininglab.catalog.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/** Client session keepalive — optional compact marks for server-side reconciliation. */
public record ChallengeSessionSyncRequest(
    Long clientMs, @Size(max = 50) List<@Valid SessionMarkRequest> marks) {

  public record SessionMarkRequest(
      @Min(1) @Max(8) int k,
      Instant at,
      @Min(1) @Max(2) Integer s,
      @Min(0) @Max(100_000) Integer n,
      @Min(0) @Max(86_400_000) Long d) {}
}
