package com.codetraininglab.feedback.api;

import jakarta.validation.constraints.NotBlank;

public record FeedbackActionRequest(@NotBlank String action) {}
