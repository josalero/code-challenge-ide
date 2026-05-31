package com.codetraininglab.integration.runner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class RunnerResultDeserializationTest {

  private final JsonMapper jsonMapper = JsonMapper.builder().build();

  @Test
  void deserializesRunnerStdoutJson() throws Exception {
    String line =
        """
        {"status":"COMPLETED","tests":[{"name":"t","status":"PASS","message":null,\
        "duration_ms":1}],"coverage":{"line_percent":50.0,"branch_percent":0.0,\
        "raw_path":"/tmp/x"},"checkstyle":{"errors":0,"warnings":0,"findings":[]}}\
        """;

    RunnerResult result = jsonMapper.readValue(line, RunnerResult.class);

    assertThat(result.status()).isEqualTo("COMPLETED");
    assertThat(result.tests().getFirst().durationMs()).isEqualTo(1);
    assertThat(result.coverage().linePercent()).isEqualTo(50.0);
  }
}
