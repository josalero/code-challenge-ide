package com.codetraininglab.identity.api;

public record AdminDashboardStatsResponse(
    AdminUserStats users,
    AdminAccessRequestStats accessRequests,
    AdminChallengeStats challenges,
    AdminSubmissionStats submissions) {

  public record AdminUserStats(long total, long admins, long learners) {}

  public record AdminAccessRequestStats(long pending, long approved, long rejected) {}

  public record AdminChallengeStats(long total) {}

  public record AdminSubmissionStats(
      long total, long completed, long failed, long running, long pending) {}
}
