package com.codetraininglab.identity.api;

import java.time.Instant;
import java.util.UUID;

public record AccessRequestSummary(
    UUID id,
    String email,
    String fullName,
    String message,
    String status,
    Instant createdAt,
    Instant reviewedAt,
    String reviewNotes) {}
