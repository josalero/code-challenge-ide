package com.codetraininglab.integration.runner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RunnerResult(
    String status,
    List<TestOutcome> tests,
    CoverageOutcome coverage,
    CompileOutcome compile,
    CheckstyleOutcome checkstyle,
    LogsOutcome logs) {

  public RunnerResult {
    if (logs == null) {
      logs = new LogsOutcome("", "");
    }
    if (compile == null) {
      compile = new CompileOutcome(0, List.of());
    }
    // Legacy field kept for serialized history; default to zero so older consumers continue
    // to work even when runners no longer emit it.
    if (checkstyle == null) {
      checkstyle = new CheckstyleOutcome(0, 0);
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

  /** Compile/parse warnings surfaced by the runner (e.g. javac warnings, ts diagnostics). */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record CompileOutcome(int warnings, List<CompileMessage> messages) {

    public CompileOutcome {
      messages = messages == null ? List.of() : List.copyOf(messages);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompileMessage(String file, int line, String message) {}
  }

  /**
   * Legacy static-analysis carrier. Default submission flow no longer populates it; kept so we can
   * deserialize historical runner output and surface on-demand analyzer results in the future.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record CheckstyleOutcome(int errors, int warnings) {}
}
