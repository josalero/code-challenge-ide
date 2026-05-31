package com.codetraininglab.submission.api;

import jakarta.validation.constraints.NotBlank;

public record CreateSubmissionRequest(
    @NotBlank String challengeSlug,
    String runtimeVersion,
    @NotBlank String solutionCode,
    String customTestsCode) {}
