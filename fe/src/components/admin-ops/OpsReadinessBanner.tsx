import { Alert, Progress } from "antd";
import { CheckCircle2, CircleAlert, Flame } from "lucide-react";
import type { RunnerOpsStatus } from "@/api/types";
import { cn } from "@/lib/utils";
import { WARM_LANGUAGES, type LanguageWarmGroup } from "./opsWarmUtils";

type Props = {
  status: RunnerOpsStatus;
  groups: LanguageWarmGroup[];
  dockerReady: boolean;
  jobRunning: boolean;
};

export default function OpsReadinessBanner({
  status,
  groups,
  dockerReady,
  jobRunning,
}: Props) {
  const readyLanguages = groups.filter((g) => g.state === "ready").length;
  const total = WARM_LANGUAGES.length;
  const percent = total > 0 ? Math.round((readyLanguages / total) * 100) : 0;
  const partialCount = groups.filter((g) => g.state === "partial").length;
  const coldCount = groups.filter(
    (g) => g.state === "cold" || g.state === "missing",
  ).length;

  if (!dockerReady) {
    return (
      <Alert
        type="error"
        showIcon
        className="mb-6"
        message="Docker is not available — warm-up cannot run"
        description={
          status.dockerEnabled
            ? "The API cannot reach Docker. Fix docker.sock access on the API host, then refresh this page."
            : "Docker integration is disabled (CTL_DOCKER_ENABLED=false). Enable it and restart the API."
        }
      />
    );
  }

  if (readyLanguages === total && total > 0 && !jobRunning) {
    return (
      <div
        className="mb-6 flex gap-3 rounded-lg border border-emerald-500/30 bg-emerald-500/10 px-4 py-3"
        role="status"
      >
        <CheckCircle2 className="mt-0.5 size-5 shrink-0 text-emerald-400" aria-hidden />
        <div>
          <p className="text-sm font-medium text-emerald-100">
            Platform is warmed up
          </p>
          <p className="mt-1 text-sm text-emerald-200/80">
            All {total} languages are ready. Learners should get fast{" "}
            <strong className="font-medium">Run tests</strong> and{" "}
            <strong className="font-medium">IntelliSense</strong>. Re-warm only after{" "}
            <code className="text-xs">make runners</code> or image tag changes.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div
      className={cn(
        "mb-6 rounded-lg border px-4 py-4",
        jobRunning
          ? "border-sky-500/30 bg-sky-500/10"
          : "border-amber-500/25 bg-amber-500/5",
      )}
      role="status"
    >
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="flex min-w-0 gap-3">
          {jobRunning ? (
            <Flame className="mt-0.5 size-5 shrink-0 text-sky-400" aria-hidden />
          ) : (
            <CircleAlert className="mt-0.5 size-5 shrink-0 text-amber-400" aria-hidden />
          )}
          <div>
            <p className="text-sm font-medium text-slate-100">
              {jobRunning
                ? "Warm-up in progress…"
                : `${readyLanguages} of ${total} languages fully ready`}
            </p>
            <p className="mt-1 text-sm text-slate-400">
              {jobRunning
                ? "Status updates every few seconds. Watch the job panel on the right for logs."
                : coldCount > 0
                  ? `${coldCount} language${coldCount === 1 ? "" : "s"} still cold`
                  : null}
              {!jobRunning && partialCount > 0
                ? `${coldCount > 0 ? " · " : ""}${partialCount} partially warmed (some runtimes missing runner or editor preload)`
                : null}
              {!jobRunning && coldCount === 0 && partialCount === 0
                ? "Use Warm everything below to preload runners and IntelliSense."
                : null}
            </p>
          </div>
        </div>
        <div className="w-full min-w-[12rem] max-w-xs shrink-0">
          <div className="mb-1 flex justify-between text-xs text-slate-500">
            <span>Languages ready</span>
            <span className="tabular-nums text-slate-300">
              {readyLanguages}/{total}
            </span>
          </div>
          <Progress
            percent={percent}
            showInfo={false}
            strokeColor={percent === 100 ? "#34d399" : "#38bdf8"}
            trailColor="rgba(148, 163, 184, 0.2)"
            size="small"
          />
        </div>
      </div>
      {!status.mavenCacheWarm && (
        <p className="mt-3 border-t border-slate-700/50 pt-3 text-xs text-slate-500">
          Java Maven dependency cache is cold — it warms automatically when you warm Java (or use Advanced → Maven).
        </p>
      )}
    </div>
  );
}
