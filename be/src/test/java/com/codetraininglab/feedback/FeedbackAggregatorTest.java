package com.codetraininglab.feedback.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.integration.runner.RunnerResult;
import tools.jackson.databind.json.JsonMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FeedbackAggregatorTest {

  @Test
  void aggregatesPassingResult() {
    RunnerResult result =
        new RunnerResult(
            com.codetraininglab.domain.RunnerStatus.COMPLETED.name(),
            List.of(
                new RunnerResult.TestOutcome(
                    "t", com.codetraininglab.domain.TestOutcomeStatus.PASS.name(), null, 1)),
            new RunnerResult.CoverageOutcome(90, 80),
            new RunnerResult.CheckstyleOutcome(0, 0),
            null);
    var aggregated =
        FeedbackAggregator.aggregate(
            UUID.randomUUID(),
            UUID.randomUUID(),
            result,
            "{\"" + com.codetraininglab.domain.GatingConfigKeys.LINE_COVERAGE_PERCENT + "\":80}",
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    assertThat(aggregated.blocked()).isFalse();
    assertThat(aggregated.items()).hasSize(5);
  }

  @Test
  void summaryIncludesRunnerLogsWhenPresent() {
    RunnerResult result =
        new RunnerResult(
            com.codetraininglab.domain.RunnerStatus.COMPLETED.name(),
            List.of(
                new RunnerResult.TestOutcome(
                    "t", com.codetraininglab.domain.TestOutcomeStatus.PASS.name(), null, 1)),
            new RunnerResult.CoverageOutcome(90, 80),
            new RunnerResult.CheckstyleOutcome(0, 0),
            new RunnerResult.LogsOutcome("build ok", "warning line"));
    var aggregated =
        FeedbackAggregator.aggregate(
            UUID.randomUUID(),
            UUID.randomUUID(),
            result,
            "{}",
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    assertThat(aggregated.report().getSummary()).contains("stdout_truncated");
    assertThat(aggregated.report().getSummary()).contains("build ok");
  }

  @Test
  void aggregatesFailingTests() {
    RunnerResult result =
        new RunnerResult(
            com.codetraininglab.domain.RunnerStatus.COMPLETED.name(),
            List.of(
                new RunnerResult.TestOutcome(
                    "t", com.codetraininglab.domain.TestOutcomeStatus.FAIL.name(), "boom", 1)),
            new RunnerResult.CoverageOutcome(10, 5),
            new RunnerResult.CheckstyleOutcome(2, 0),
            null);
    var aggregated =
        FeedbackAggregator.aggregate(
            UUID.randomUUID(),
            UUID.randomUUID(),
            result,
            "{}",
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    assertThat(aggregated.blocked()).isTrue();
  }
}
