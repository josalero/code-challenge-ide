package com.codetraininglab.submission.api;

import java.util.List;
import java.util.UUID;

public record ReportResponse(
    UUID id,
    UUID submissionId,
    boolean blocked,
    String summary,
    List<FeedbackItemResponse> feedback,
    RunnerLogsResponse runnerLogs) {}
