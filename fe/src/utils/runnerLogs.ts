import type { RunnerLogs } from "@/api/types";

/** Parse stdout/stderr from SSE error or done payloads. */
export function runnerLogsFromSsePayload(data: {
  stdout?: string;
  stderr?: string;
}): RunnerLogs | null {
  const stdout = data.stdout;
  const stderr = data.stderr;
  if (!stdout && !stderr) {
    return null;
  }
  return {
    stdoutTruncated: stdout ?? "",
    stderrTruncated: stderr ?? "",
  };
}

export function hasRunnerLogOutput(logs: RunnerLogs | null | undefined): boolean {
  return Boolean(logs?.stdoutTruncated?.trim() || logs?.stderrTruncated?.trim());
}
