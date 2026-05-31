package com.codetraininglab.integration.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class RunnerJobPayloadTest {

  private final JsonMapper jsonMapper = JsonMapper.builder().build();

  @Test
  void serializesSnakeCaseFieldsForRunnerContract() throws Exception {
    RunnerJobPayload payload =
        new RunnerJobPayload(
            "sub-1",
            "maven",
            "class Solution {}",
            null,
            List.of(new RunnerJobPayload.HiddenTest("Hidden", "class H {}")),
            RunnerJobPayload.RunnerLimits.defaults());

    String json = jsonMapper.writeValueAsString(payload);

    assertThat(json).contains("\"submission_id\":\"sub-1\"");
    assertThat(json).contains("\"solution_code\"");
    assertThat(json).contains("\"hidden_tests\"");
    assertThat(json).doesNotContain("custom_tests_code");
  }
}
