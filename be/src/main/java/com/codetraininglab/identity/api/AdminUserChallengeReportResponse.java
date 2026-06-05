package com.codetraininglab.identity.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminUserChallengeReportResponse(
    UserHeader user, Summary summary, List<ChallengeRow> challenges) {

  public record UserHeader(
      UUID id, String email, String fullName, String role, boolean active) {}

  public record Summary(
      int catalogTotal,
      int started,
      int passed,
      int attempted,
      int failed,
      int notStarted,
      int completionPercent,
      int likelyAbandoned,
      long submissionsTotal,
      long practiceRuns,
      long gradedSubmits,
      Integer gradedPassRatePercent) {}

  public record ChallengeRow(
      String challengeSlug,
      String title,
      String language,
      String difficulty,
      Integer sessionLimitMinutes,
      String progressState,
      String engagementStatus,
      boolean submitted,
      Instant submittedAt,
      Instant firstActivityAt,
      Instant lastActivityAt,
      long practiceRuns,
      long gradedSubmits,
      long gradedPasses,
      long gradedFails,
      Integer passRatePercent,
      Long timeToPassMs,
      Long avgProcessingMs,
      long enhancementRequests,
      long feedbackItems,
      long feedbackWarnings,
      long cancelledSubmissions,
      boolean likelyAbandoned,
      long clipboardCopyAttempts,
      long clipboardPasteAttempts,
      long clipboardCutAttempts,
      long integrityTabHiddenCount,
      long integrityWindowBlurCount,
      long integrityLargeEditCount,
      long integrityTotalAwayMs) {}
}
