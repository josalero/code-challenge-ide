import { Tag, Typography } from "antd";
import {
  CheckCircle2,
  CircleAlert,
  Loader2,
  XCircle,
} from "lucide-react";
import type { RunnerOpsJob } from "@/api/types";
import CtlCard from "@/components/ui/CtlCard";
import { cn } from "@/lib/utils";
import { JOB_TYPE_LABELS } from "./opsWarmUtils";

function jobStatusTag(status: string) {
  if (status === "RUNNING") {
    return <Tag color="processing">Running</Tag>;
  }
  if (status === "COMPLETED") {
    return <Tag color="success">Completed</Tag>;
  }
  if (status === "FAILED") {
    return <Tag color="error">Failed</Tag>;
  }
  return <Tag>{status}</Tag>;
}

type Props = {
  job: RunnerOpsJob | undefined;
  busy: boolean;
};

export default function OpsJobPanel({ job, busy }: Props) {
  return (
    <CtlCard
      title="Current job"
      extra={
        busy ? (
          <span className="inline-flex items-center gap-1.5 text-xs text-sky-600 dark:text-sky-400">
            <Loader2 className="size-3.5 animate-spin" aria-hidden />
            In progress
          </span>
        ) : null
      }
    >
      {!job ? (
        <div className="space-y-3 py-4">
          <div className="flex flex-col items-center gap-2 text-center">
            <CircleAlert className="size-8 text-muted-foreground/60" aria-hidden />
            <p className="text-sm text-muted-foreground">No job yet</p>
          </div>
          <div className="rounded-md border border-border bg-muted/30 px-3 py-2.5 text-xs leading-relaxed text-muted-foreground">
            <p className="font-medium text-foreground">What happens when you warm?</p>
            <ol className="mt-2 list-decimal space-y-1 pl-4">
              <li>Starts a smoke submission in each runner image (validates Run tests).</li>
              <li>Starts each editor language server once (speeds up IntelliSense).</li>
              <li>For Java, may download Maven dependencies into the cache volume.</li>
            </ol>
            <p className="mt-2">Logs appear here while the job runs. Large jobs can take several minutes.</p>
          </div>
        </div>
      ) : (
        <div className="space-y-3">
          <div className="flex flex-wrap items-center gap-2">
            {jobStatusTag(job.status)}
            <Typography.Text className="text-xs text-muted-foreground">
              {JOB_TYPE_LABELS[job.type] ?? job.type}
            </Typography.Text>
          </div>
          <p className="text-sm text-foreground">{job.message || "Working…"}</p>
          {job.status === "COMPLETED" && (
            <p className="inline-flex items-center gap-1.5 text-xs text-emerald-600 dark:text-emerald-400">
              <CheckCircle2 className="size-3.5" aria-hidden />
              Finished — language status above will update within a few seconds.
            </p>
          )}
          {job.status === "FAILED" && (
            <p className="inline-flex items-center gap-1.5 text-xs text-red-600 dark:text-red-400">
              <XCircle className="size-3.5" aria-hidden />
              Failed — check the log below. Languages that completed before the error may show as partially ready.
            </p>
          )}
          {job.logTail ? (
            <pre
              className={cn(
                "ctl-ops-log max-h-[min(28rem,55vh)] overflow-auto p-3",
              )}
            >
              {job.logTail}
            </pre>
          ) : (
            <p className="text-xs text-muted-foreground">Waiting for log output…</p>
          )}
        </div>
      )}
    </CtlCard>
  );
}
