import { useQuery } from "@tanstack/react-query";
import { Alert, Spin } from "antd";
import {
  BarChart3,
  CheckCircle2,
  CircleDashed,
  FlaskConical,
  Send,
  Target,
  TrendingUp,
  XCircle,
} from "lucide-react";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "../api/client";
import type { MeMetricsResponse, MetricsBreakdownRow } from "../api/types";
import AppLayout from "../components/AppLayout";
import MetricsBreakdownBarChart from "../components/metrics/MetricsBreakdownBarChart";
import MetricsProgressDonut from "../components/metrics/MetricsProgressDonut";
import MetricsSubmissionChart from "../components/metrics/MetricsSubmissionChart";
import { Button } from "@/components/ui/button";
import { ApiPaths } from "../domain/constants";
import { formatLanguageLabel } from "../utils/languageRuntimes";
import { cn } from "@/lib/utils";

function StatCard({
  label,
  value,
  hint,
  icon,
  tone = "neutral",
}: {
  label: string;
  value: number | string;
  hint?: string;
  icon: ReactNode;
  tone?: "neutral" | "success" | "active" | "danger";
}) {
  const toneClass = {
    neutral: "border-border bg-muted/40",
    success: "border-emerald-500/30 bg-emerald-500/10",
    active: "border-sky-500/30 bg-sky-500/10",
    danger: "border-rose-500/30 bg-rose-500/10",
  }[tone];

  return (
    <div className={cn("rounded-xl border px-4 py-3", toneClass)}>
      <div className="mb-2 flex items-center gap-2 text-muted-foreground">
        {icon}
        <span className="text-xs font-medium uppercase tracking-wide">{label}</span>
      </div>
      <p className="text-2xl font-semibold leading-none text-foreground">{value}</p>
      {hint && <p className="mt-1.5 text-xs text-muted-foreground">{hint}</p>}
    </div>
  );
}

function BreakdownTable({
  title,
  rows,
  formatLabel,
}: {
  title: string;
  rows: MetricsBreakdownRow[];
  formatLabel?: (label: string) => string;
}) {
  if (rows.length === 0) {
    return null;
  }

  const labelFor = formatLabel ?? ((l: string) => l);

  return (
    <section className="rounded-xl border border-border bg-card/60 p-4">
      <h2 className="mb-3 text-sm font-semibold text-foreground">{title}</h2>
      <div className="overflow-x-auto">
        <table className="w-full min-w-[320px] border-collapse text-left text-sm">
          <thead>
            <tr className="border-b border-border text-[10px] font-semibold uppercase tracking-wide text-muted-foreground">
              <th className="pb-2 pr-3 font-medium">Group</th>
              <th className="pb-2 pr-3 text-right font-medium">Total</th>
              <th className="pb-2 pr-3 text-right font-medium">Passed</th>
              <th className="pb-2 pr-3 text-right font-medium">Active</th>
              <th className="pb-2 text-right font-medium">New</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.label} className="border-b border-border/60 last:border-0">
                <td className="py-2.5 pr-3 font-medium capitalize text-foreground">
                  {labelFor(row.label)}
                </td>
                <td className="py-2.5 pr-3 text-right tabular-nums text-muted-foreground">
                  {row.total}
                </td>
                <td className="py-2.5 pr-3 text-right tabular-nums text-emerald-600 dark:text-emerald-300">
                  {row.passed}
                </td>
                <td className="py-2.5 pr-3 text-right tabular-nums text-sky-600 dark:text-sky-300">
                  {row.inProgress}
                </td>
                <td className="py-2.5 text-right tabular-nums text-muted-foreground">
                  {row.notStarted}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default function MetricsPage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["me", "metrics"],
    queryFn: () => apiFetch<MeMetricsResponse>(ApiPaths.ME_METRICS),
  });

  const inProgressCount = data ? data.attempted + data.failed : 0;

  return (
    <AppLayout contentLayout="wide">
      <div className="mb-6 flex flex-col gap-4 border-b border-border pb-5 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="mb-1 flex items-center gap-2 text-xs font-semibold uppercase tracking-widest text-emerald-600 dark:text-emerald-400/90">
            <BarChart3 className="size-3.5" aria-hidden />
            Learning dashboard
          </p>
          <h1 className="text-2xl font-semibold text-foreground md:text-3xl">Your progress</h1>
          <p className="mt-2 max-w-2xl text-sm text-muted-foreground">
            Catalog completion, practice runs, and graded submits — visualized from your account.
          </p>
        </div>
        <Link to="/challenges" className="shrink-0 no-underline">
          <Button variant="outline">Browse challenges</Button>
        </Link>
      </div>

      {error && (
        <Alert
          type="error"
          showIcon
          role="alert"
          message={(error as Error).message}
          className="mb-6"
        />
      )}

      {isLoading && (
        <div className="flex justify-center py-20" role="status">
          <Spin size="large" tip="Loading metrics…">
            <div className="min-h-[120px] w-full" aria-hidden />
          </Spin>
        </div>
      )}

      {data && !isLoading && (
        <div className="flex flex-col gap-8">
          <section
            className="rounded-xl border border-border bg-gradient-to-br from-card/90 to-muted/30 p-5 shadow-sm"
            aria-labelledby="completion-heading"
          >
            <div className="mb-4 flex flex-wrap items-end justify-between gap-3">
              <div className="flex items-center gap-2">
                <span className="flex size-9 items-center justify-center rounded-lg bg-emerald-500/15 text-emerald-600 ring-1 ring-emerald-500/25 dark:text-emerald-400">
                  <TrendingUp className="size-4" aria-hidden />
                </span>
                <div>
                  <h2
                    id="completion-heading"
                    className="text-sm font-semibold text-foreground"
                  >
                    Catalog completion
                  </h2>
                  <p className="text-xs text-muted-foreground">
                    Passed challenges out of those you have started
                  </p>
                </div>
              </div>
              <p className="text-2xl font-semibold tabular-nums text-foreground">
                {data.completionPercent}
                <span className="text-base font-medium text-muted-foreground">%</span>
              </p>
            </div>
            <div
              className="h-4 overflow-hidden rounded-full bg-muted ring-1 ring-border"
              role="progressbar"
              aria-valuenow={data.completionPercent}
              aria-valuemin={0}
              aria-valuemax={100}
              aria-label={`${data.completionPercent}% of started challenges passed`}
            >
              <div
                className="h-full rounded-full bg-gradient-to-r from-emerald-600 to-emerald-400 transition-[width] duration-500"
                style={{ width: `${data.completionPercent}%` }}
              />
            </div>
            <p className="mt-2 text-sm text-muted-foreground">
              <span className="font-medium text-foreground">{data.passed}</span> passed of{" "}
              <span className="font-medium text-foreground">{data.challengesStarted}</span> started
              {" · "}
              <span className="font-medium text-foreground">{data.catalogTotal}</span> in catalog
            </p>
          </section>

          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <StatCard
              label="Passed"
              value={data.passed}
              icon={<CheckCircle2 className="size-4 text-emerald-500" aria-hidden />}
              tone="success"
            />
            <StatCard
              label="In progress"
              value={inProgressCount}
              hint={`${data.attempted} attempted · ${data.failed} needs work`}
              icon={<Target className="size-4 text-sky-500" aria-hidden />}
              tone="active"
            />
            <StatCard
              label="Not started"
              value={data.notStarted}
              icon={<CircleDashed className="size-4" aria-hidden />}
            />
            <StatCard
              label={data.maxStartedChallenges != null ? "Exercise slots" : "Catalog size"}
              value={
                data.maxStartedChallenges != null
                  ? `${data.challengesStarted}/${data.maxStartedChallenges}`
                  : data.catalogTotal
              }
              hint={
                data.maxStartedChallenges != null
                  ? `${data.challengesRemaining ?? 0} new exercises available`
                  : undefined
              }
              icon={<BarChart3 className="size-4" aria-hidden />}
            />
          </div>

          <div className="grid gap-6 xl:grid-cols-2">
            <MetricsProgressDonut
              passed={data.passed}
              inProgress={inProgressCount}
              notStarted={data.notStarted}
              catalogTotal={data.catalogTotal}
            />
            <MetricsSubmissionChart
              practiceRuns={data.practiceRuns}
              gradedSubmits={data.gradedSubmits}
              submissionsFailed={data.submissionsFailed}
              submissionsCompleted={data.submissionsCompleted}
            />
          </div>

          <section aria-labelledby="runs-heading">
            <h2
              id="runs-heading"
              className="mb-3 text-sm font-semibold uppercase tracking-wide text-muted-foreground"
            >
              Run totals
            </h2>
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <StatCard
                label="Total runs"
                value={data.submissionsTotal}
                icon={<FlaskConical className="size-4" aria-hidden />}
              />
              <StatCard
                label="Practice (Run)"
                value={data.practiceRuns}
                icon={<FlaskConical className="size-4 text-sky-500" aria-hidden />}
                tone="active"
              />
              <StatCard
                label="Graded (Submit)"
                value={data.gradedSubmits}
                icon={<Send className="size-4 text-emerald-500" aria-hidden />}
                tone="success"
              />
              <StatCard
                label="Failed runs"
                value={data.submissionsFailed}
                hint={`${data.submissionsCompleted} completed successfully`}
                icon={<XCircle className="size-4 text-rose-500" aria-hidden />}
                tone="danger"
              />
            </div>
          </section>

          <div className="grid gap-6 xl:grid-cols-2">
            <MetricsBreakdownBarChart
              title="By language"
              description="Stacked progress per runtime"
              rows={data.byLanguage}
              formatLabel={formatLanguageLabel}
            />
            <MetricsBreakdownBarChart
              title="By difficulty"
              description="Stacked progress per level"
              rows={data.byDifficulty}
            />
          </div>

          <div className="grid gap-6 lg:grid-cols-2">
            <BreakdownTable
              title="Language detail"
              rows={data.byLanguage}
              formatLabel={formatLanguageLabel}
            />
            <BreakdownTable title="Difficulty detail" rows={data.byDifficulty} />
          </div>
        </div>
      )}
    </AppLayout>
  );
}
