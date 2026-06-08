import type { AdminUserChallengeReportResponse } from "@/api/types";
import type { AdminStatTone } from "./AdminStatCard";
import { cn } from "@/lib/utils";

type Props = {
  summary: AdminUserChallengeReportResponse["summary"];
};

type MetricItem = {
  label: string;
  value: string | number;
  hint?: string;
  tone?: AdminStatTone;
};

const toneValueClass: Partial<Record<AdminStatTone, string>> = {
  success: "text-emerald-600 dark:text-emerald-400",
  active: "text-sky-600 dark:text-sky-400",
  danger: "text-rose-600 dark:text-rose-400",
  warning: "text-amber-700 dark:text-amber-400",
};

function CompactMetric({ label, value, hint, tone }: MetricItem) {
  return (
    <div className="border-b border-border px-3 py-2.5 text-center last:border-b-0 sm:border-b-0 lg:min-w-[5.25rem] lg:flex-1">
      <p className="truncate text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
        {label}
      </p>
      <p
        className={cn(
          "mt-0.5 text-base font-semibold tabular-nums leading-none text-foreground",
          tone && toneValueClass[tone],
        )}
      >
        {value}
      </p>
      {hint && (
        <p className="mt-0.5 truncate text-[10px] leading-snug text-muted-foreground">{hint}</p>
      )}
    </div>
  );
}

export default function ChallengeReportSummary({ summary }: Props) {
  const gradedPass =
    summary.gradedPassRatePercent == null ? "—" : `${summary.gradedPassRatePercent}%`;

  const metrics: MetricItem[] = [
    {
      label: "Started",
      value: summary.started,
      hint: `${summary.notStarted} not started`,
      tone: "active",
    },
    {
      label: "Completion",
      value: `${summary.completionPercent}%`,
      hint: `${summary.passed}/${summary.started} of started`,
      tone: "success",
    },
    {
      label: "Graded pass",
      value: gradedPass,
      hint: `${summary.gradedSubmits} graded`,
    },
    {
      label: "Abandoned",
      value: summary.likelyAbandoned,
      hint: "14+ days idle",
      tone: summary.likelyAbandoned > 0 ? "warning" : undefined,
    },
    { label: "In progress", value: summary.attempted, tone: "active" },
    { label: "Passed", value: summary.passed, tone: "success" },
    { label: "Failed", value: summary.failed, tone: "danger" },
    { label: "Submissions", value: summary.submissionsTotal },
    { label: "Practice", value: summary.practiceRuns },
  ];

  return (
    <section
      className="mb-4 overflow-hidden rounded-xl border border-border/80 bg-card/50 shadow-sm"
      aria-label="Challenge report summary"
    >
      <div className="grid grid-cols-2 divide-y divide-border sm:grid-cols-3 sm:divide-y-0 lg:flex lg:flex-wrap lg:divide-x lg:divide-y-0">
        {metrics.map((metric) => (
          <CompactMetric key={metric.label} {...metric} />
        ))}
      </div>
    </section>
  );
}
