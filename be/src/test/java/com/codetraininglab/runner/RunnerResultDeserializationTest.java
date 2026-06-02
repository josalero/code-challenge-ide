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
        "duration_ms":1}],"coverage":{"line_percent":50.0,"branch_percent":0.0},\
        "compile":{"warnings":2,"messages":[{"file":"/x.java","line":3,"message":"raw type"}]}}\
        """;

    RunnerResult result = jsonMapper.readValue(line, RunnerResult.class);

    assertThat(result.status()).isEqualTo("COMPLETED");
    assertThat(result.tests().getFirst().durationMs()).isEqualTo(1);
    assertThat(result.coverage().linePercent()).isEqualTo(50.0);
    assertThat(result.compile().warnings()).isEqualTo(2);
    assertThat(result.compile().messages()).hasSize(1);
    assertThat(result.checkstyle().errors()).isZero();
  }

  @Test
  void deserializesLegacyResultWithoutCompileField() throws Exception {
    String line =
        """
        {"status":"COMPLETED","tests":[],"coverage":{"line_percent":0.0,"branch_percent":0.0},\
        "checkstyle":{"errors":0,"warnings":0}}\
        """;

    RunnerResult result = jsonMapper.readValue(line, RunnerResult.class);

    assertThat(result.compile().warnings()).isZero();
    assertThat(result.compile().messages()).isEmpty();
  }
}
