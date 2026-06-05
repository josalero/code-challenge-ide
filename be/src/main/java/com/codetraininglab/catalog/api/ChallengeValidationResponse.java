package com.codetraininglab.catalog.api;

import java.util.List;

public record ChallengeValidationResponse(
    String status,
    boolean compiled,
    boolean passed,
    String message,
    CompileSummary compile,
    List<TestResult> tests,
    Logs logs) {

  public record CompileSummary(int warnings, List<CompileMessage> messages) {}

  public record CompileMessage(String file, int line, String message) {}

  public record TestResult(String name, String status, String message, long durationMs) {}

  public record Logs(String stdoutTruncated, String stderrTruncated) {}
}
