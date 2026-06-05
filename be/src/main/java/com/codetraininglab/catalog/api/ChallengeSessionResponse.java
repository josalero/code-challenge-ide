package com.codetraininglab.catalog.api;

import java.time.Instant;
import java.util.UUID;

public record ChallengeSessionResponse(
    UUID sessionId,
    Instant startedAt,
    Instant expiresAt,
    long remainingSeconds,
    boolean expired) {}
