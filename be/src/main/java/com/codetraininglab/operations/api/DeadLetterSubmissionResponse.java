package com.codetraininglab.operations.api;

import java.time.Instant;
import java.util.UUID;

public record DeadLetterSubmissionResponse(
    UUID submissionId, String status, Instant enqueuedAt, String errorHint) {}
