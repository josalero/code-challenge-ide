package com.codetraininglab.feedback.application;

import com.codetraininglab.domain.FeedbackCategory;
import com.codetraininglab.domain.FeedbackStatus;
import com.codetraininglab.domain.GatingConfigKeys;
import com.codetraininglab.domain.GatingDefaults;
import com.codetraininglab.domain.TestOutcomeStatus;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.platform.persistence.FeedbackItemEntity;
import com.codetraininglab.platform.persistence.SubmissionReportEntity;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the report + feedback items emitted on every submission.
 *
 * <p>The default submission flow only runs tests and coverage. Style/security analysis is requested
 * explicitly via the feedback-actions API and aggregated separately. The aggregator therefore
 * emits three categories ({@link FeedbackCategory#CORRECTNESS}, {@link FeedbackCategory#COVERAGE},
 * {@link FeedbackCategory#READABILITY}) and only marks the submission as blocked when tests fail.
 */
public final class FeedbackAggregator {

  private FeedbackAggregator() {}

  public static AggregatedFeedback aggregate(
      UUID reportId,
      UUID submissionId,
      RunnerResult result,
      String gatingConfigJson,
      JsonMapper mapper,
      Clock clock) {
    double coverageThreshold = GatingDefaults.LINE_COVERAGE_PERCENT;
    int maxCompileWarnings = GatingDefaults.MAX_COMPILE_WARNINGS;
    try {
      JsonNode gating = mapper.readTree(gatingConfigJson);
      if (gating.has(GatingConfigKeys.LINE_COVERAGE_PERCENT)) {
        coverageThreshold = gating.get(GatingConfigKeys.LINE_COVERAGE_PERCENT).asDouble();
      }
      if (gating.has(GatingConfigKeys.MAX_COMPILE_WARNINGS)) {
        maxCompileWarnings = gating.get(GatingConfigKeys.MAX_COMPILE_WARNINGS).asInt();
      }
    } catch (Exception ignored) {
      // use defaults
    }

    List<FeedbackItemEntity> items = new ArrayList<>();
    Instant now = clock.instant();

    boolean testsPass =
        !result.tests().isEmpty()
            && result.tests().stream().allMatch(t -> TestOutcomeStatus.PASS.matches(t.status()));
    items.add(
        item(
            reportId,
            FeedbackCategory.CORRECTNESS,
            testsPass ? FeedbackStatus.pass : FeedbackStatus.fail,
            testsPass ? "All tests passed" : "One or more tests failed",
            "correctness",
            now));

    double linePercent = result.coverage().linePercent();
    boolean coverageOk = linePercent >= coverageThreshold;
    items.add(
        item(
            reportId,
            FeedbackCategory.COVERAGE,
            coverageOk ? FeedbackStatus.pass : FeedbackStatus.warn,
            "Line coverage " + linePercent + "% (target " + coverageThreshold + "%)",
            "coverage",
            now));

    int compileWarnings = result.compile().warnings();
    boolean readable = compileWarnings <= maxCompileWarnings;
    items.add(
        item(
            reportId,
            FeedbackCategory.READABILITY,
            readable ? FeedbackStatus.pass : FeedbackStatus.warn,
            readable
                ? "No compiler warnings"
                : "Compiler reported " + compileWarnings + " warning(s)",
            "readability",
            now));

    // Only tests gate the submission. Coverage/readability are informational.
    boolean blocked = !testsPass;
    String summary = buildSummary(mapper, result, blocked);
    SubmissionReportEntity report =
        new SubmissionReportEntity(reportId, submissionId, 1, summary, blocked, now);
    return new AggregatedFeedback(report, items, blocked);
  }

  private static String buildSummary(JsonMapper mapper, RunnerResult result, boolean blocked) {
    var root = mapper.createObjectNode();
    root.put("tests", result.tests().size());
    root.put("blocked", blocked);
    RunnerResult.LogsOutcome logs = result.logs();
    if (logs != null
        && (!logs.stdoutTruncated().isBlank() || !logs.stderrTruncated().isBlank())) {
      var logsNode = root.putObject("logs");
      logsNode.put("stdout_truncated", logs.stdoutTruncated());
      logsNode.put("stderr_truncated", logs.stderrTruncated());
    }
    return root.toString();
  }

  private static FeedbackItemEntity item(
      UUID reportId,
      FeedbackCategory category,
      FeedbackStatus status,
      String message,
      String stableId,
      Instant now) {
    return new FeedbackItemEntity(
        UUID.randomUUID(), reportId, category, status, "info", message, stableId, now);
  }

  public record AggregatedFeedback(
      SubmissionReportEntity report, List<FeedbackItemEntity> items, boolean blocked) {}
}
