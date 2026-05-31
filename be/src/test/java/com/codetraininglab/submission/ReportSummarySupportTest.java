package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.submission.api.RunnerLogsResponse;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

class ReportSummarySupportTest {

  @Test
  void parsesRunnerLogsFromSummary() {
    String summary =
        """
        {"tests":1,"blocked":false,"logs":{"stdout_truncated":"ok","stderr_truncated":"warn"}}
        """;
    RunnerLogsResponse logs =
        ReportSummarySupport.parseRunnerLogs(summary, JsonMapper.builder().build());
    assertThat(logs).isNotNull();
    assertThat(logs.stdoutTruncated()).isEqualTo("ok");
    assertThat(logs.stderrTruncated()).isEqualTo("warn");
  }

  @Test
  void returnsNullWhenNoLogs() {
    assertThat(
            ReportSummarySupport.parseRunnerLogs(
                "{\"tests\":1,\"blocked\":false}", JsonMapper.builder().build()))
        .isNull();
  }
}
