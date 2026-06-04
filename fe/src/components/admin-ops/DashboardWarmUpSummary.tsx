import { Alert, Progress, Spin } from "antd";
import { CheckCircle2, CircleAlert, Flame, ServerCog } from "lucide-react";
import { Link } from "react-router-dom";
import type { RunnerOpsStatus } from "@/api/types";
import { cn } from "@/lib/utils";
import {
  groupLanguagesByWarmState,
  languageStateLabel,
  formatLastWarmUpAt,
  WARM_LANGUAGES,
} from "./opsWarmUtils";
import CtlCard from "../ui/CtlCard";

type Props = {
  ops: RunnerOpsStatus | undefined;
  isLoading: boolean;
  isError: boolean;
};

export default function DashboardWarmUpSummary({ ops, isLoading, isError }: Props) {
  const warmGroups = ops ? groupLanguagesByWarmState(ops.languages) : [];
  const readyLanguages = warmGroups.filter((group) => group.state === "ready").length;
  const warmPercent =
    WARM_LANGUAGES.length > 0
      ? Math.round((readyLanguages / WARM_LANGUAGES.length) * 100)
      : 0;
  const dockerReady = Boolean(ops?.dockerAvailable && ops?.dockerEnabled);
  const warmJobRunning = Boolean(ops?.activeJobId);
  const allReady = readyLanguages === WARM_LANGUAGES.length && WARM_LANGUAGES.length > 0;
  const attentionLanguages = warmGroups.filter((group) => group.state !== "ready");

  return (
    <CtlCard
      title="Warm-up"
      extra={
        <Link
          to="/admin/ops"
          className="inline-flex items-center gap-1 text-xs font-medium text-emerald-600 no-underline hover:text-emerald-500 dark:text-emerald-400"
        >
          <ServerCog className="size-3.5" aria-hidden />
          Ops console
        </Link>
      }
    >
      {isLoading && (
        <div className="flex justify-center py-6">
          <Spin size="small" />
        </div>
      )}

      {isError && (
        <Alert
          type="warning"
          showIcon
          message="Status unavailable"
          description="Open Ops for runner details."
          className="!py-2"
        />
      )}

      {ops && (
        <div className="space-y-3">
          {!dockerReady ? (
            <Alert
              type="error"
              showIcon
              message="Docker unavailable"
              description={
                ops.dockerEnabled
                  ? "API cannot reach Docker."
                  : "Docker integration is disabled."
              }
              className="!py-2"
            />
          ) : (
            <>
              <div className="flex items-center gap-2">
                {allReady && !warmJobRunning ? (
                  <CheckCircle2 className="size-4 shrink-0 text-emerald-500" aria-hidden />
                ) : warmJobRunning ? (
                  <Flame className="size-4 shrink-0 text-sky-500" aria-hidden />
                ) : (
                  <CircleAlert className="size-4 shrink-0 text-amber-500" aria-hidden />
                )}
                <span className="min-w-0 flex-1 text-sm text-foreground">
                  {warmJobRunning
                    ? "Warm job running"
                    : allReady
                      ? "All languages ready"
                      : `${readyLanguages}/${WARM_LANGUAGES.length} languages ready`}
                </span>
                <span className="shrink-0 text-xs tabular-nums text-muted-foreground">
                  {warmPercent}%
                </span>
              </div>

              <Progress
                percent={warmPercent}
                size="small"
                strokeColor={warmPercent === 100 ? "#34d399" : "#38bdf8"}
                showInfo={false}
              />

              {attentionLanguages.length > 0 && !allReady && (
                <div className="flex flex-wrap gap-1.5">
                  {attentionLanguages.map((group) => (
                    <span
                      key={group.language}
                      className={cn(
                        "inline-flex items-center rounded px-2 py-0.5 text-[11px] font-medium ring-1 ring-inset",
                        group.state === "partial" &&
                          "bg-sky-500/10 text-sky-700 ring-sky-500/25 dark:text-sky-300",
                        (group.state === "cold" || group.state === "missing") &&
                          "bg-muted text-muted-foreground ring-border",
                      )}
                      title={languageStateLabel(group.state)}
                    >
                      {group.language}
                    </span>
                  ))}
                </div>
              )}

              <p className="mb-0 text-[11px] text-muted-foreground">
                Last warm-up: {formatLastWarmUpAt(ops.lastWarmUpAt)}
                {" · "}
                Maven cache {ops.mavenCacheWarm ? "warm" : "cold"}
              </p>
            </>
          )}
        </div>
      )}
    </CtlCard>
  );
}
