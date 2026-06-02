package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.integration.runner.RunnerJobPayload;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class RunnerPoolWarmJobSerializationTest {

  @Test
  void warmJobSerializesPassingSolutionForReverseString() throws Exception {
    String solution = RunnerWarmSolutions.solutionFor("reverse-string").orElseThrow();
    RunnerJobPayload job =
        new RunnerJobPayload(
            "warm-test",
            "reverse-string",
            "maven",
            solution,
            null,
            List.of(),
            RunnerJobPayload.RunnerLimits.defaults());

    JsonMapper mapper = JsonMapper.builder().build();
    String json = mapper.writeValueAsString(job);
    JsonNode root = mapper.readTree(json);

    assertThat(root.get("solution_code").asString()).contains("StringBuilder");
    assertThat(root.get("solution_code").asString()).doesNotContain("TODO");
    assertThat(root.get("hidden_tests")).isEmpty();
  }

  @Test
  void warmJobSerializesSolutionCodeForArmstrong() throws Exception {
    String solution =
        RunnerWarmSolutions.solutionFor("armstrong-number").orElseThrow();
    RunnerJobPayload job =
        new RunnerJobPayload(
            "warm-test",
            "armstrong-number",
            "pytest",
            solution,
            null,
            List.of(),
            RunnerJobPayload.RunnerLimits.defaults());

    JsonMapper mapper = JsonMapper.builder().build();
    String json = mapper.writeValueAsString(job);
    JsonNode root = mapper.readTree(json);

    assertThat(root.get("solution_code").asString()).contains("is_armstrong");
    assertThat(root.get("challenge_slug").asString()).isEqualTo("armstrong-number");
    assertThat(root.get("workspace_layout").asString()).isEqualTo("pytest");
    assertThat(root.get("hidden_tests")).isEmpty();
  }
}
