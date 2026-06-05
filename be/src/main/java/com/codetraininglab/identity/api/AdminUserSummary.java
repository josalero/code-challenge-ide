package com.codetraininglab.identity.api;

import java.time.Instant;
import java.util.UUID;

public record AdminUserSummary(
    UUID id,
    String email,
    String fullName,
    String role,
    Instant createdAt,
    boolean active,
    Instant lastActivityAt,
    long submissionsTotal,
    long practiceRuns,
    long gradedSubmits,
    int challengesPassed,
    int challengesStarted,
    int completionPercent,
    int platformDefaultChallengeLimit,
    Integer challengeQuotaOverride,
    Integer effectiveChallengeLimit,
    boolean integrityMonitoringDisabled) {}
