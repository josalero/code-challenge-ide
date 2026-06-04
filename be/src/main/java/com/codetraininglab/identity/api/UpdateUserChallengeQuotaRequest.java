package com.codetraininglab.identity.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateUserChallengeQuotaRequest(
    /** Null resets to platform default; 0 = unlimited; positive = explicit learner cap. */
    @Min(0) @Max(100) Integer maxStartedChallenges) {}
