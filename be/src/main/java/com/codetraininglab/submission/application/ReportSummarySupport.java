package com.codetraininglab.submission.application;

import com.codetraininglab.submission.api.RunnerLogsResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public final class ReportSummarySupport {

  private ReportSummarySupport() {}

  public static RunnerLogsResponse parseRunnerLogs(String summary, JsonMapper mapper) {
    if (summary == null || summary.isBlank()) {
      return null;
    }
    try {
      JsonNode logs = mapper.readTree(summary).path("logs");
      if (logs.isMissingNode()) {
        return null;
      }
      String stdout = logs.path("stdout_truncated").asText("");
      String stderr = logs.path("stderr_truncated").asText("");
      if (stdout.isBlank() && stderr.isBlank()) {
        return null;
      }
      return new RunnerLogsResponse(stdout, stderr);
    } catch (RuntimeException e) {
      return null;
    }
  }
}
