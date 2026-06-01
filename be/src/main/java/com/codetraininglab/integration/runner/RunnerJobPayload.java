package com.codetraininglab.integration.runner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RunnerJobPayload(
    @JsonProperty("submission_id") String submissionId,
    @JsonProperty("challenge_slug") String challengeSlug,
    @JsonProperty("workspace_layout") String workspaceLayout,
    @JsonProperty("solution_code") String solutionCode,
    @JsonProperty("custom_tests_code") String customTestsCode,
    @JsonProperty("hidden_tests") List<HiddenTest> hiddenTests,
    RunnerLimits limits) {

  public record HiddenTest(String name, String source) {}

  public record RunnerLimits(
      @JsonProperty("memory_mb") int memoryMb,
      int cpus,
      @JsonProperty("wall_seconds") int wallSeconds,
      @JsonProperty("cpu_seconds") int cpuSeconds,
      int pids,
      @JsonProperty("stdout_bytes") int stdoutBytes,
      @JsonProperty("per_test_seconds") int perTestSeconds) {

    public static RunnerLimits defaults() {
      return new RunnerLimits(1024, 2, 120, 60, 512, 2_097_152, 10);
    }
  }
}
