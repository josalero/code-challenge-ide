package com.codetraininglab.feedback.application;

import com.codetraininglab.domain.FeedbackCategory;
import com.codetraininglab.domain.FeedbackStatus;
import com.codetraininglab.domain.GatingConfigKeys;
import com.codetraininglab.domain.GatingDefaults;
import com.codetraininglab.domain.TestOutcomeStatus;
import com.codetraininglab.platform.persistence.FeedbackItemEntity;
import com.codetraininglab.platform.persistence.SubmissionReportEntity;
import com.codetraininglab.integration.runner.RunnerResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    int maxCheckstyleErrors = GatingDefaults.MAX_CHECKSTYLE_ERRORS;
    try {
      JsonNode gating = mapper.readTree(gatingConfigJson);
      if (gating.has(GatingConfigKeys.LINE_COVERAGE_PERCENT)) {
        coverageThreshold = gating.get(GatingConfigKeys.LINE_COVERAGE_PERCENT).asDouble();
      }
      if (gating.has(GatingConfigKeys.CHECKSTYLE_MAX_ERRORS)) {
        maxCheckstyleErrors = gating.get(GatingConfigKeys.CHECKSTYLE_MAX_ERRORS).asInt();
      }
    } catch (Exception ignored) {
      // use defaults
    }

    List<FeedbackItemEntity> items = new ArrayList<>();
    Instant now = clock.instant();

    boolean testsPass =
        result.tests().stream().allMatch(t -> TestOutcomeStatus.PASS.matches(t.status()));
    items.add(
        item(
            reportId,
            FeedbackCategory.CORRECTNESS,
            testsPass ? FeedbackStatus.pass : FeedbackStatus.fail,
            testsPass ? "All tests passed" : "One or more tests failed",
            "correctness",
            now));

    boolean coveragePass = result.coverage().linePercent() >= coverageThreshold;
    items.add(
        item(
            reportId,
            FeedbackCategory.COVERAGE,
            coveragePass ? FeedbackStatus.pass : FeedbackStatus.fail,
            "Line coverage "
                + result.coverage().linePercent()
                + "% (required "
                + coverageThreshold
                + "%)",
            "coverage",
            now));

    boolean stylePass = result.checkstyle().errors() <= maxCheckstyleErrors;
    items.add(
        item(
            reportId,
            FeedbackCategory.STYLE,
            stylePass ? FeedbackStatus.pass : FeedbackStatus.fail,
            "Checkstyle errors: " + result.checkstyle().errors(),
            "style",
            now));

    int checkstyleWarnings = result.checkstyle().warnings();
    items.add(
        item(
            reportId,
            FeedbackCategory.SECURITY,
            checkstyleWarnings == 0 ? FeedbackStatus.pass : FeedbackStatus.warn,
            checkstyleWarnings == 0
                ? "No static-analysis security warnings"
                : "Checkstyle reported " + checkstyleWarnings + " warning(s) — review imports and APIs",
            "security",
            now));

    boolean readable =
        checkstyleWarnings <= GatingDefaults.MAX_READABILITY_WARNINGS
            && result.checkstyle().errors() == 0;
    items.add(
        item(
            reportId,
            FeedbackCategory.READABILITY,
            readable ? FeedbackStatus.pass : FeedbackStatus.warn,
            readable
                ? "Code structure looks readable"
                : "Style issues may affect readability — simplify naming and formatting",
            "readability",
            now));

    boolean blocked = items.stream().anyMatch(i -> i.getStatus() == FeedbackStatus.fail);
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
