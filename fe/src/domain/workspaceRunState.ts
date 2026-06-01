import type { ReportResponse, RunnerLogs } from "../api/types";
import type { SubmissionStatusValue } from "./constants";
import { SubmissionStatus } from "./constants";
import type { TrackedTest } from "../domain/runProgressTypes";

export type WorkspaceRunPhase =
  | "idle"
  | "loading"
  | "running"
  | "compilation-error"
  | "failed-test"
  | "timeout"
  | "service-unavailable"
  | "successful-submission";

export function deriveWorkspaceRunPhase(input: {
  challengeLoading: boolean;
  isRunning: boolean;
  submissionStatus: SubmissionStatusValue | null;
  submitError: string | null;
  report: ReportResponse | null;
  trackedTests: TrackedTest[];
  runnerLogs: RunnerLogs | null;
}): WorkspaceRunPhase {
  if (input.challengeLoading) {
    return "loading";
  }
  if (input.isRunning) {
    return "running";
  }

  const error = input.submitError?.toLowerCase() ?? "";
  if (
    error.includes("timeout")
    || error.includes("timed out")
    || error.includes("deadline")
  ) {
    return "timeout";
  }
  if (
    error.includes("unavailable")
    || error.includes("503")
    || error.includes("502")
    || error.includes("connection")
    || error.includes("network")
  ) {
    return "service-unavailable";
  }

  const stderr = input.runnerLogs?.stderrTruncated?.toLowerCase() ?? "";
  if (
    stderr.includes("compilation")
    || stderr.includes("compile error")
    || stderr.includes("syntax error")
    || stderr.includes("cannot find symbol")
    || stderr.includes("error:")
  ) {
    return "compilation-error";
  }

  if (input.trackedTests.some((t) => t.status === "fail")) {
    return "failed-test";
  }

  if (input.report && !input.report.blocked) {
    return "successful-submission";
  }

  if (
    input.submissionStatus === SubmissionStatus.FAILED
    || input.submissionStatus === SubmissionStatus.CANCELLED
  ) {
    if (input.trackedTests.some((t) => t.status === "fail")) {
      return "failed-test";
    }
    return "service-unavailable";
  }

  if (input.report?.blocked) {
    return "failed-test";
  }

  return "idle";
}

export const RUN_PHASE_LABELS: Record<WorkspaceRunPhase, string> = {
  idle: "Ready",
  loading: "Loading challenge…",
  running: "Running tests…",
  "compilation-error": "Compilation error",
  "failed-test": "Tests failed",
  timeout: "Run timed out",
  "service-unavailable": "Service unavailable",
  "successful-submission": "Submission passed",
};
