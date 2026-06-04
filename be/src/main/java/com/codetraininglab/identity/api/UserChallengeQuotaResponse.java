package com.codetraininglab.identity.api;

import java.util.UUID;

public record UserChallengeQuotaResponse(
    UUID userId,
    int platformDefault,
    Integer challengeQuotaOverride,
    Integer effectiveChallengeLimit,
    int challengesStarted,
    Integer challengesRemaining) {}
