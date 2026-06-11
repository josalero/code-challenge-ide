import type { RunnerLogs } from "@/api/types";
import { cn } from "@/lib/utils";

type Props = {
  logs: RunnerLogs;
  className?: string;
};

/** Runner stdout (info) and stderr (errors) with distinct terminal styling. */
export default function RunnerLogOutput({ logs, className }: Props) {
  const hasStdout = Boolean(logs.stdoutTruncated?.trim());
  const hasStderr = Boolean(logs.stderrTruncated?.trim());

  if (!hasStdout && !hasStderr) {
    return null;
  }

  return (
    <div className={cn("space-y-3", className)}>
      {hasStdout && (
        <section aria-label="Program output">
          <p className="mb-1 text-[10px] font-semibold uppercase tracking-wide text-sky-400/90">
            Output
          </p>
          <pre className="whitespace-pre-wrap rounded-md border border-sky-500/25 bg-sky-500/5 p-2.5 font-mono text-xs leading-relaxed text-sky-100">
            {logs.stdoutTruncated}
          </pre>
        </section>
      )}
      {hasStderr && (
        <section aria-label="Runner errors">
          <p className="mb-1 text-[10px] font-semibold uppercase tracking-wide text-red-400/90">
            Errors
          </p>
          <pre className="whitespace-pre-wrap rounded-md border border-red-500/30 bg-red-500/5 p-2.5 font-mono text-xs leading-relaxed text-red-200">
            {logs.stderrTruncated}
          </pre>
        </section>
      )}
    </div>
  );
}
