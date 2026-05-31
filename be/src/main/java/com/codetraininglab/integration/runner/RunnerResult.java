package com.codetraininglab.integration.runner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RunnerResult(
    String status,
    List<TestOutcome> tests,
    CoverageOutcome coverage,
    CheckstyleOutcome checkstyle,
    LogsOutcome logs) {

  public RunnerResult {
    if (logs == null) {
      logs = new LogsOutcome("", "");
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record LogsOutcome(
      @JsonProperty("stdout_truncated") String stdoutTruncated,
      @JsonProperty("stderr_truncated") String stderrTruncated) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TestOutcome(
      String name,
      String status,
      String message,
      @JsonProperty("duration_ms") long durationMs) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record CoverageOutcome(
      @JsonProperty("line_percent") double linePercent,
      @JsonProperty("branch_percent") double branchPercent) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record CheckstyleOutcome(int errors, int warnings) {}
}
