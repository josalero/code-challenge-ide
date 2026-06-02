package com.codetraininglab.submission.api;

import java.time.Instant;
import java.util.UUID;

public record SubmissionResponse(
    UUID id, String status, String kind, UUID reportId, Instant createdAt) {}
