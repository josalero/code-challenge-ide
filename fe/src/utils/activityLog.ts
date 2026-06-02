import type { TestResultEvent } from "@/api/types";
import type { ActivityEntry, ActivityKind } from "@/domain/runProgressTypes";
import { normalizeRunnerTestName } from "@/utils/submissionProgress";

export function createActivityEntry(message: string, kind: ActivityKind = "info"): ActivityEntry {
  return {
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`,
    at: Date.now(),
    message,
    kind,
  };
}

/** Short label for activity log (full runner name in `title` for hover). */
export function formatTestActivityLabel(runnerName: string): string {
  const short = normalizeRunnerTestName(runnerName);
  if (runnerName.includes(".hidden.") || runnerName.toLowerCase().includes("hidden")) {
    return `Hidden · ${short}`;
  }
  return short;
}

export function activityEntryFromTestResult(test: TestResultEvent): ActivityEntry {
  const status = test.status.toUpperCase();
  const kind: ActivityKind =
    status === "PASS" ? "test-pass" : status === "SKIP" ? "test-skip" : "test-fail";
  return {
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`,
    at: Date.now(),
    message: formatTestActivityLabel(test.name),
    kind,
    title: test.name,
  };
}

export function inferActivityKind(message: string): ActivityKind {
  const lower = message.toLowerCase();
  if (lower.includes("all tests passed") || lower.includes("practice run: all")) {
    return "success";
  }
  if (
    lower.includes("failed")
    || lower.includes("some tests failed")
    || lower.startsWith("status: failed")
  ) {
    return "warning";
  }
  if (lower.startsWith("test ") && /: (pass|fail|skip)$/i.test(message)) {
    const status = message.slice(message.lastIndexOf(":") + 2).toUpperCase();
    if (status === "PASS") {
      return "test-pass";
    }
    if (status === "SKIP") {
      return "test-skip";
    }
    return "test-fail";
  }
  return "info";
}
