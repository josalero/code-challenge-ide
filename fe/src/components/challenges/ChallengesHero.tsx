import { CheckCircle2, CircleDashed, ListTodo, Target } from "lucide-react";
import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

type ProgressStats = {
  total: number;
  passed: number;
  attempted: number;
  failed: number;
  notStarted: number;
};

type Props = {
  stats: ProgressStats;
  extra?: ReactNode;
};

function StatPill({
  icon,
  label,
  value,
  className,
}: {
  icon: ReactNode;
  label: string;
  value: number;
  className?: string;
}) {
  return (
    <div
      className={cn(
        "flex items-center gap-3 rounded-xl border border-border bg-muted/50 px-4 py-3 dark:border-slate-600/40 dark:bg-slate-800/35",
        className,
      )}
    >
      <span className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-background text-muted-foreground ring-1 ring-border dark:bg-slate-800/80 dark:text-slate-300 dark:ring-slate-700/60">
        {icon}
      </span>
      <div className="min-w-0">
        <p className="text-lg font-semibold leading-none text-foreground">{value}</p>
        <p className="mt-1 text-xs font-medium uppercase tracking-wide text-muted-foreground">
          {label}
        </p>
      </div>
    </div>
  );
}

export default function ChallengesHero({ stats, extra }: Props) {
  const completionPct =
    stats.total > 0 ? Math.round((stats.passed / stats.total) * 100) : 0;

  return (
    <section
      className={cn(
        "ctl-challenges-hero relative overflow-hidden rounded-2xl border px-5 py-6 md:px-8 md:py-8",
        "border-border bg-gradient-to-br from-card via-card to-muted/40",
        "dark:border-slate-600/40 dark:from-slate-800/30 dark:via-slate-800/25 dark:to-slate-900/20",
      )}
      aria-labelledby="challenges-hero-heading"
    >
      <div
        className="pointer-events-none absolute -right-16 -top-20 size-64 rounded-full bg-emerald-500/15 blur-3xl dark:bg-emerald-500/10"
        aria-hidden
      />
      <div
        className="pointer-events-none absolute -bottom-24 left-1/3 size-48 rounded-full bg-sky-500/10 blur-3xl dark:bg-sky-500/5"
        aria-hidden
      />

      <div className="relative flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
        <div className="min-w-0 flex-1">
          <p className="mb-2 text-xs font-semibold uppercase tracking-widest text-emerald-600 dark:text-emerald-400/90">
            Training catalog
          </p>
          <h1
            id="challenges-hero-heading"
            className="text-2xl font-semibold tracking-tight text-foreground md:text-3xl"
          >
            Pick up where you left off
          </h1>
          <p className="mt-2 max-w-xl text-sm leading-relaxed text-muted-foreground md:text-base">
            Practice in a full IDE with Docker runners, hidden tests, and AI feedback after
            submit. Filter by language or progress to find your next exercise.
          </p>

          <div className="mt-5">
            <div className="mb-2 flex items-center justify-between gap-2 text-xs text-muted-foreground">
              <span>Catalog completion</span>
              <span className="font-medium text-foreground">
                {stats.passed} / {stats.total} passed
                <span className="text-muted-foreground"> ({completionPct}%)</span>
              </span>
            </div>
            <div
              className="h-2 overflow-hidden rounded-full bg-muted ring-1 ring-border dark:bg-slate-800/80 dark:ring-slate-700/50"
              role="progressbar"
              aria-valuenow={completionPct}
              aria-valuemin={0}
              aria-valuemax={100}
              aria-label={`${completionPct}% of challenges passed`}
            >
              <div
                className="h-full rounded-full bg-gradient-to-r from-emerald-600 to-emerald-400 transition-[width] duration-500"
                style={{ width: `${completionPct}%` }}
              />
            </div>
          </div>
        </div>

        {extra && <div className="shrink-0 lg:pt-1">{extra}</div>}
      </div>

      <div className="relative mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <StatPill
          icon={<ListTodo className="size-4" aria-hidden />}
          label="Total"
          value={stats.total}
        />
        <StatPill
          icon={<CheckCircle2 className="size-4 text-emerald-600 dark:text-emerald-400" aria-hidden />}
          label="Passed"
          value={stats.passed}
          className="border-emerald-500/25 dark:border-emerald-500/20"
        />
        <StatPill
          icon={<Target className="size-4 text-sky-600 dark:text-sky-400" aria-hidden />}
          label="In progress"
          value={stats.attempted}
          className="border-sky-500/25 dark:border-sky-500/20"
        />
        <StatPill
          icon={<CircleDashed className="size-4 text-muted-foreground" aria-hidden />}
          label="Not started"
          value={stats.notStarted}
        />
      </div>
    </section>
  );
}
