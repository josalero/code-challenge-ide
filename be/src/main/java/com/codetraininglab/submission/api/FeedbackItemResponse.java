package com.codetraininglab.submission.api;

import java.util.UUID;

public record FeedbackItemResponse(
    UUID id,
    String category,
    String status,
    String message,
    String aiExplanation) {}
