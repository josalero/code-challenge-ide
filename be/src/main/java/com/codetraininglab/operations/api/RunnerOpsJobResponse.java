package com.codetraininglab.operations.api;

import java.time.Instant;
import java.util.UUID;

public record RunnerOpsJobResponse(
    UUID id,
    String type,
    String status,
    Instant startedAt,
    Instant finishedAt,
    String message,
    String logTail) {}
