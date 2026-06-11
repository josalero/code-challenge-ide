import {
  CheckCircle2,
  Loader2,
  MinusCircle,
  Radio,
  Wifi,
  WifiOff,
  XCircle,
} from "lucide-react";
import { useEffect, useState } from "react";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { SubmissionStatusValue } from "@/domain/constants";
import { SubmissionStatus } from "@/domain/constants";
import type { TrackedTest } from "@/domain/runProgressTypes";
import {
  formatRuntimeLabel,
  runnerPipelineLabel,
  runnerWarmupHint,
} from "@/utils/languageRuntimes";

export type { ActivityEntry, TrackedTest } from "@/domain/runProgressTypes";

type Props = {
  submissionStatus: SubmissionStatusValue | null;
  isSubmitting: boolean;
  streamConnected: boolean;
  streamReconnecting: boolean;
  trackedTests: TrackedTest[];
  hiddenTestCount: number;
  runtimeVersion: string;
  challengeLanguage: string;
  runStartedAt: number | null;
};

const STEPS = [
  { key: "queue", label: "Queued" },
  { key: "sandbox", label: "Sandbox" },
  { key: "tests", label: "Tests" },
  { key: "report", label: "Report" },
] as const;

function stepIndex(
  status: SubmissionStatusValue | null,
  isSubmitting: boolean,
  hasTestResults: boolean,
): number {
  if (isSubmitting || status === SubmissionStatus.PENDING) {
    return 0;
  }
  if (status === SubmissionStatus.RUNNING && !hasTestResults) {
    return 1;
  }
  if (status === SubmissionStatus.RUNNING && hasTestResults) {
    return 2;
  }
  if (
    status === SubmissionStatus.COMPLETED
    || status === SubmissionStatus.FAILED
    || status === SubmissionStatus.CANCELLED
  ) {
    return 3;
  }
  return 0;
}

function formatTestName(name: string, hiddenTestCount: number): string {
  if (name === "hidden:pending") {
    return `${hiddenTestCount} hidden test${hiddenTestCount === 1 ? "" : "s"}`;
  }
  if (name.startsWith("hidden:")) {
    return name.slice(7);
  }
  return name;
}

function TestStatusIcon({
  status,
  runComplete,
}: {
  status: TrackedTest["status"];
  runComplete: boolean;
}) {
  if (status === "pending" && runComplete) {
    return <XCircle className="size-4 shrink-0 text-red-400" aria-hidden />;
  }
  switch (status) {
    case "pass":
      return <CheckCircle2 className="size-4 shrink-0 text-emerald-400" aria-hidden />;
    case "fail":
      return <XCircle className="size-4 shrink-0 text-red-400" aria-hidden />;
    case "skip":
      return <MinusCircle className="size-4 shrink-0 text-slate-500" aria-hidden />;
    default:
      return <Loader2 className="size-4 shrink-0 animate-spin text-sky-400" aria-hidden />;
  }
}

export default function RunProgressPanel({
  submissionStatus,
  isSubmitting,
  streamConnected,
  streamReconnecting,
  trackedTests,
  hiddenTestCount,
  runtimeVersion,
  challengeLanguage,
  runStartedAt,
}: Props) {
  const runtimeLabel = formatRuntimeLabel(challengeLanguage, runtimeVersion);
  const pipeline = runnerPipelineLabel(challengeLanguage);
  const [elapsedSec, setElapsedSec] = useState(0);

  const isActive =
    isSubmitting
    || submissionStatus === SubmissionStatus.PENDING
    || submissionStatus === SubmissionStatus.RUNNING;

  const runComplete =
    submissionStatus === SubmissionStatus.COMPLETED
    || submissionStatus === SubmissionStatus.FAILED
    || submissionStatus === SubmissionStatus.CANCELLED;

  useEffect(() => {
    if (!runStartedAt || !isActive) {
      setElapsedSec(0);
      return;
    }
    const tick = () =>
      setElapsedSec(Math.floor((Date.now() - runStartedAt) / 1000));
    tick();
    const id = window.setInterval(tick, 1000);
    return () => window.clearInterval(id);
  }, [runStartedAt, isActive]);

  const hasTestResults = trackedTests.some((t) => t.status !== "pending");
  const currentStep = stepIndex(submissionStatus, isSubmitting, hasTestResults);
  const failed = submissionStatus === SubmissionStatus.FAILED;

  const finishedTests = trackedTests.filter((t) => t.status !== "pending");
  const passedCount = finishedTests.filter((t) => t.status === "pass").length;
  const failedCount = finishedTests.filter((t) => t.status === "fail").length;
  const pendingCount = trackedTests.filter((t) => t.status === "pending").length;

  return (
    <div className="flex h-full min-h-0 flex-col gap-3">
      {/* Status strip */}
      <div className="ctl-run-status-strip flex flex-wrap items-center gap-2 rounded-lg border border-slate-700/50 bg-slate-900/60 px-3 py-2">
        {isActive ? (
          <span className="flex items-center gap-1.5 text-sm font-medium text-sky-200">
            <Loader2 className="size-3.5 animate-spin text-sky-400" aria-hidden />
            Running
          </span>
        ) : submissionStatus === SubmissionStatus.COMPLETED ? (
          <span className="flex items-center gap-1.5 text-sm font-medium text-emerald-300">
            <CheckCircle2 className="size-3.5" aria-hidden />
            Complete
          </span>
        ) : submissionStatus === SubmissionStatus.FAILED ? (
          <span className="flex items-center gap-1.5 text-sm font-medium text-red-300">
            <XCircle className="size-3.5" aria-hidden />
            Failed
          </span>
        ) : submissionStatus === SubmissionStatus.CANCELLED ? (
          <span className="text-sm font-medium text-slate-400">Cancelled</span>
        ) : null}

        <Badge variant="outline" className="border-slate-600/60 font-normal text-slate-300">
          {runtimeLabel}
        </Badge>

        {isActive && elapsedSec > 0 && (
          <span className="font-mono text-xs tabular-nums text-slate-400">
            {elapsedSec}s
          </span>
        )}

        {isActive && (
          <span className="ml-auto flex items-center gap-1 text-xs text-slate-500">
            {streamConnected ? (
              <>
                <Wifi className="size-3 text-emerald-500" aria-hidden />
                <span className="text-emerald-400/90">Live</span>
              </>
            ) : streamReconnecting ? (
              <>
                <WifiOff className="size-3 text-amber-500" aria-hidden />
                <span className="text-amber-400/90">Reconnecting…</span>
              </>
            ) : (
              <>
                <Radio className="size-3 animate-pulse text-sky-500" aria-hidden />
                <span>Connecting…</span>
              </>
            )}
          </span>
        )}

        {!isActive && finishedTests.length > 0 && (
          <span className="ml-auto text-xs text-slate-400">
            <span className="text-emerald-400">{passedCount} passed</span>
            {failedCount > 0 && (
              <>
                {" · "}
                <span className="text-red-400">{failedCount} failed</span>
              </>
            )}
          </span>
        )}
      </div>

      {/* Step progress */}
      <div className="px-0.5" role="group" aria-label="Run progress">
        <div className="mb-1.5 flex justify-between gap-1">
          {STEPS.map((step, i) => {
            const done = i < currentStep;
            const active = i === currentStep && isActive;
            return (
              <span
                key={step.key}
                className={`flex-1 truncate text-center text-[10px] font-medium uppercase tracking-wide sm:text-[11px] ${
                  done
                    ? "text-emerald-400/90"
                    : active
                      ? failed
                        ? "text-red-300"
                        : "text-sky-300"
                      : "text-slate-600"
                }`}
              >
                {step.label}
              </span>
            );
          })}
        </div>
        <div className="flex gap-1">
          {STEPS.map((step, i) => {
            const done = i < currentStep;
            const active = i === currentStep;
            return (
              <div
                key={step.key}
                className={`h-1 flex-1 overflow-hidden rounded-full bg-slate-800 ${
                  active && isActive ? "ctl-run-step-active" : ""
                }`}
              >
                <div
                  className={`h-full rounded-full transition-all duration-500 ${
                    done
                      ? "w-full bg-emerald-500/80"
                      : active
                        ? failed
                          ? "w-2/3 bg-red-500/70"
                          : "w-2/3 bg-sky-500/70"
                        : "w-0"
                  }`}
                />
              </div>
            );
          })}
        </div>
        <p className="mt-1.5 text-[11px] text-slate-500">{pipeline}</p>
      </div>

      {/* Test list */}
      {trackedTests.length > 0 && (
        <ScrollArea className="min-h-0 flex-1">
          <ul className="space-y-1.5 pr-2" aria-label="Test results">
            {trackedTests.map((test) => (
              <li
                key={test.name}
                className={`ctl-workspace-test-row flex items-start gap-2.5 rounded-md border px-2.5 py-2 text-sm ${
                  test.status === "fail"
                    ? "border-red-500/30 bg-red-500/5"
                    : test.status === "pass"
                      ? "border-emerald-500/20 bg-emerald-500/5"
                      : test.status === "pending"
                        ? "border-slate-700/40 bg-slate-900/40"
                        : "border-slate-700/40 bg-slate-900/30"
                }`}
              >
                <TestStatusIcon status={test.status} runComplete={runComplete} />
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span
                      className={`truncate font-mono text-xs sm:text-sm ${
                        test.status === "pending" ? "text-slate-400" : "text-slate-100"
                      }`}
                    >
                      {formatTestName(test.name, hiddenTestCount)}
                    </span>
                    {test.name.startsWith("hidden:") && test.name !== "hidden:pending" && (
                      <Badge
                        variant="outline"
                        className="h-4 border-slate-600/50 px-1 text-[9px] font-normal text-slate-500"
                      >
                        hidden
                      </Badge>
                    )}
                  </div>
                  {test.message && test.status !== "pending" && (
                    <pre className="mt-0.5 whitespace-pre-wrap text-xs leading-relaxed text-slate-500">
                      {test.message}
                    </pre>
                  )}
                  {test.message && test.status === "pending" && !test.name.startsWith("hidden:") && (
                    <p className="mt-0.5 text-xs text-slate-600">{test.message}</p>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </ScrollArea>
      )}

      {isActive && pendingCount > 0 && trackedTests.length === 0 && (
        <div className="flex items-center justify-center gap-2 py-6 text-sm text-slate-400">
          <Loader2 className="size-4 animate-spin text-sky-400" aria-hidden />
          Preparing test suite…
        </div>
      )}

      {isActive && (
        <p className="shrink-0 text-[11px] leading-relaxed text-slate-600">
          {runnerWarmupHint(challengeLanguage)}
        </p>
      )}
    </div>
  );
}
