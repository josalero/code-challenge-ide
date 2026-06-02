package com.codetraininglab.feedback.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.domain.FeedbackCategory;
import com.codetraininglab.domain.FeedbackStatus;
import com.codetraininglab.domain.GatingConfigKeys;
import com.codetraininglab.domain.RunnerStatus;
import com.codetraininglab.domain.TestOutcomeStatus;
import com.codetraininglab.integration.runner.RunnerResult;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class FeedbackAggregatorTest {

  private final JsonMapper mapper = JsonMapper.builder().build();
  private final Clock clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

  @Test
  void aggregatesPassingResult() {
    RunnerResult result = result(TestOutcomeStatus.PASS.name(), 90, 0);

    var aggregated =
        FeedbackAggregator.aggregate(
            UUID.randomUUID(),
            UUID.randomUUID(),
            result,
            "{\"" + GatingConfigKeys.LINE_COVERAGE_PERCENT + "\":80}",
            mapper,
            clock);

    assertThat(aggregated.blocked()).isFalse();
    assertThat(aggregated.items()).hasSize(3);
    assertThat(aggregated.items())
        .extracting(item -> item.getCategory())
        .containsExactly(
            FeedbackCategory.CORRECTNESS,
            FeedbackCategory.COVERAGE,
            FeedbackCategory.READABILITY);
    assertThat(aggregated.items())
        .allMatch(item -> item.getStatus() == FeedbackStatus.pass);
  }

  @Test
  void summaryIncludesRunnerLogsWhenPresent() {
    RunnerResult result =
        new RunnerResult(
            RunnerStatus.COMPLETED.name(),
            List.of(new RunnerResult.TestOutcome("t", TestOutcomeStatus.PASS.name(), null, 1)),
            new RunnerResult.CoverageOutcome(90, 80),
            new RunnerResult.CompileOutcome(0, List.of()),
            null,
            new RunnerResult.LogsOutcome("build ok", "warning line"));

    var aggregated =
        FeedbackAggregator.aggregate(
            UUID.randomUUID(), UUID.randomUUID(), result, "{}", mapper, clock);

    assertThat(aggregated.report().getSummary()).contains("stdout_truncated");
    assertThat(aggregated.report().getSummary()).contains("build ok");
  }

  @Test
  void blocksOnFailingTestsOnly() {
    RunnerResult result = result(TestOutcomeStatus.FAIL.name(), 10, 5);

    var aggregated =
        FeedbackAggregator.aggregate(
            UUID.randomUUID(), UUID.randomUUID(), result, "{}", mapper, clock);

    assertThat(aggregated.blocked()).isTrue();
    FeedbackStatus correctness =
        aggregated.items().stream()
            .filter(i -> i.getCategory() == FeedbackCategory.CORRECTNESS)
            .findFirst()
            .orElseThrow()
            .getStatus();
    assertThat(correctness).isEqualTo(FeedbackStatus.fail);
  }

  @Test
  void coverageBelowThresholdWarnsButDoesNotBlock() {
    RunnerResult result = result(TestOutcomeStatus.PASS.name(), 10, 0);

    var aggregated =
        FeedbackAggregator.aggregate(
            UUID.randomUUID(),
            UUID.randomUUID(),
            result,
            "{\"" + GatingConfigKeys.LINE_COVERAGE_PERCENT + "\":80}",
            mapper,
            clock);

    assertThat(aggregated.blocked()).isFalse();
    FeedbackStatus coverage =
        aggregated.items().stream()
            .filter(i -> i.getCategory() == FeedbackCategory.COVERAGE)
            .findFirst()
            .orElseThrow()
            .getStatus();
    assertThat(coverage).isEqualTo(FeedbackStatus.warn);
  }

  @Test
  void readabilityWarnsWhenCompileWarningsExceedConfig() {
    RunnerResult result = result(TestOutcomeStatus.PASS.name(), 90, 4);

    var aggregated =
        FeedbackAggregator.aggregate(
            UUID.randomUUID(),
            UUID.randomUUID(),
            result,
            "{\"" + GatingConfigKeys.MAX_COMPILE_WARNINGS + "\":2}",
            mapper,
            clock);

    assertThat(aggregated.blocked()).isFalse();
    FeedbackStatus readability =
        aggregated.items().stream()
            .filter(i -> i.getCategory() == FeedbackCategory.READABILITY)
            .findFirst()
            .orElseThrow()
            .getStatus();
    assertThat(readability).isEqualTo(FeedbackStatus.warn);
  }

  private static RunnerResult result(String testStatus, double linePercent, int compileWarnings) {
    return new RunnerResult(
        RunnerStatus.COMPLETED.name(),
        List.of(new RunnerResult.TestOutcome("t", testStatus, "msg", 1)),
        new RunnerResult.CoverageOutcome(linePercent, linePercent),
        new RunnerResult.CompileOutcome(compileWarnings, List.of()),
        null,
        null);
  }
}
