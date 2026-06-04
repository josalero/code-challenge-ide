package com.codetraininglab.identity.api;

import com.codetraininglab.feedback.api.FeedbackActionResponse;
import com.codetraininglab.submission.api.FeedbackItemResponse;
import com.codetraininglab.submission.api.RunnerLogsResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminUserChallengeDetailResponse(
    AdminUserChallengeReportResponse.UserHeader user,
    AdminUserChallengeReportResponse.ChallengeRow stats,
    List<SubmissionDetail> submissions) {

  public record SubmissionDetail(
      UUID id,
      String kind,
      String status,
      String runtimeVersion,
      Instant createdAt,
      Instant updatedAt,
      Long processingMs,
      String solutionCode,
      String customTestsCode,
      ReportDetail report,
      List<FeedbackActionResponse> feedbackActions) {}

  public record ReportDetail(
      UUID id,
      boolean blocked,
      String summary,
      RunnerLogsResponse runnerLogs,
      List<FeedbackItemResponse> feedback) {}
}
