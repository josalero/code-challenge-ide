import type { RunnerLogs } from "@/api/types";
import type { BottomPanelTab } from "@/components/workspace/WorkspaceBottomPanel";
import { hasRunnerLogOutput } from "./runnerLogs";

function stderrLooksLikeCompileError(stderr: string): boolean {
  const lower = stderr.toLowerCase();
  return (
    lower.includes("compilation")
    || lower.includes("compile error")
    || lower.includes("syntax error")
    || lower.includes("cannot find symbol")
    || lower.includes("error ts")
    || lower.includes("error:")
  );
}

/** Pick the most useful Output sub-tab after a run or submit finishes. */
export function suggestOutputTab(input: {
  kind?: "RUN" | "SUBMIT";
  passed?: boolean | null;
  runnerLogs?: RunnerLogs | null;
  hasFailedTests?: boolean;
  hasReport?: boolean;
}): BottomPanelTab {
  if (input.hasReport || input.kind === "SUBMIT") {
    return "feedback";
  }

  const logs = input.runnerLogs;
  const hasLogs = hasRunnerLogOutput(logs);
  const compileLike = stderrLooksLikeCompileError(logs?.stderrTruncated ?? "");

  if (compileLike || (hasLogs && input.passed === false)) {
    return "compiler";
  }
  if (input.passed === false || input.hasFailedTests) {
    return "tests";
  }
  if (hasLogs) {
    return "compiler";
  }
  return "tests";
}
