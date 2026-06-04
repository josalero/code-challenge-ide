import { useQuery } from "@tanstack/react-query";
import { Alert, Spin } from "antd";
import {
  BookOpen,
  FlaskConical,
  Inbox,
  ServerCog,
  Users,
} from "lucide-react";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "../api/client";
import type { AdminDashboardStats, RunnerOpsStatus } from "../api/types";
import DashboardWarmUpSummary from "../components/admin-ops/DashboardWarmUpSummary";
import AppLayout from "../components/AppLayout";
import PageHeader from "../components/ui/PageHeader";
import { ApiPaths } from "../domain/constants";
import { cn } from "../lib/utils";

function StatCard({
  label,
  value,
  hint,
  icon,
  tone = "neutral",
  to,
}: {
  label: string;
  value: number | string;
  hint?: string;
  icon: ReactNode;
  tone?: "neutral" | "success" | "active" | "warning" | "danger";
  to?: string;
}) {
  const toneClass = {
    neutral: "border-border bg-muted/40",
    success: "border-emerald-500/30 bg-emerald-500/10",
    active: "border-sky-500/30 bg-sky-500/10",
    warning: "border-amber-500/30 bg-amber-500/10",
    danger: "border-rose-500/30 bg-rose-500/10",
  }[tone];

  const body = (
    <>
      <div className="mb-2 flex items-center gap-2 text-muted-foreground">
        {icon}
        <span className="text-xs font-medium uppercase tracking-wide">{label}</span>
      </div>
      <p className="text-3xl font-semibold leading-none text-foreground">{value}</p>
      {hint && <p className="mt-2 text-xs text-muted-foreground">{hint}</p>}
    </>
  );

  const className = cn(
    "rounded-xl border px-4 py-4 transition-colors",
    toneClass,
    to && "hover:border-emerald-500/30 hover:bg-emerald-500/5",
  );

  if (to) {
    return (
      <Link to={to} className={cn(className, "block no-underline")}>
        {body}
      </Link>
    );
  }

  return <div className={className}>{body}</div>;
}

function QuickLink({
  to,
  title,
  description,
  icon,
}: {
  to: string;
  title: string;
  description: string;
  icon: ReactNode;
}) {
  return (
    <Link
      to={to}
      className="flex gap-3 rounded-lg border border-border bg-card p-3 no-underline transition-colors hover:border-emerald-500/30 hover:bg-emerald-500/5"
    >
      <span className="flex size-9 shrink-0 items-center justify-center rounded-md bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
        {icon}
      </span>
      <span className="min-w-0">
        <span className="block text-sm font-semibold text-foreground">{title}</span>
        <span className="mt-0.5 block text-xs text-muted-foreground">{description}</span>
      </span>
    </Link>
  );
}

export default function AdminDashboardPage() {
  const statsQuery = useQuery({
    queryKey: ["admin", "dashboard"],
    queryFn: () => apiFetch<AdminDashboardStats>(ApiPaths.ADMIN_DASHBOARD),
  });

  const opsQuery = useQuery({
    queryKey: ["ops", "runners", "status"],
    queryFn: () => apiFetch<RunnerOpsStatus>(ApiPaths.OPS_RUNNERS_STATUS),
    refetchInterval: 30_000,
  });

  const stats = statsQuery.data;

  if (statsQuery.isLoading) {
    return (
      <AppLayout>
        <div className="flex min-h-[40vh] items-center justify-center">
          <Spin size="large" />
        </div>
      </AppLayout>
    );
  }

  if (statsQuery.isError || !stats) {
    return (
      <AppLayout>
        <Alert
          type="error"
          showIcon
          message="Could not load dashboard"
          description="Refresh the page or check that the API is running."
        />
      </AppLayout>
    );
  }

  const pendingRequests = stats.accessRequests.pending;
  const pipelineHint = [
    stats.submissions.running > 0 ? `${stats.submissions.running} running` : null,
    stats.submissions.pending > 0 ? `${stats.submissions.pending} queued` : null,
    `${stats.submissions.completed} completed`,
    stats.submissions.failed > 0 ? `${stats.submissions.failed} failed` : null,
  ]
    .filter(Boolean)
    .join(" · ");

  return (
    <AppLayout>
      <PageHeader
        title="Admin dashboard"
        description="Overview of users, catalog, access requests, and platform health."
      />

      <div className="space-y-6">
        <section aria-label="Key metrics">
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <StatCard
              label="Pending requests"
              value={pendingRequests}
              hint={
                pendingRequests > 0
                  ? "Needs review"
                  : `${stats.accessRequests.approved} approved · ${stats.accessRequests.rejected} rejected`
              }
              icon={<Inbox className="size-4" aria-hidden />}
              tone={pendingRequests > 0 ? "warning" : "neutral"}
              to="/admin/access-requests"
            />
            <StatCard
              label="Users"
              value={stats.users.total}
              hint={`${stats.users.admins} admin · ${stats.users.learners} learners`}
              icon={<Users className="size-4" aria-hidden />}
              to="/admin/users"
            />
            <StatCard
              label="Challenges"
              value={stats.challenges.total}
              hint="Published catalog"
              icon={<BookOpen className="size-4" aria-hidden />}
              tone="active"
              to="/challenges"
            />
            <StatCard
              label="Submissions"
              value={stats.submissions.total}
              hint={pipelineHint}
              icon={<FlaskConical className="size-4" aria-hidden />}
              to="/metrics"
            />
          </div>
        </section>

        <div className="grid gap-6 lg:grid-cols-2">
          <section aria-label="Quick actions">
            <h2 className="mb-3 text-sm font-semibold text-foreground">Quick actions</h2>
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1 xl:grid-cols-2">
              <QuickLink
                to="/admin/access-requests"
                title="Review access requests"
                description={
                  pendingRequests > 0
                    ? `${pendingRequests} waiting for review`
                    : "No pending requests"
                }
                icon={<Inbox className="size-4" aria-hidden />}
              />
              <QuickLink
                to="/admin/users"
                title="Users & activity"
                description="Directory, metrics, and account management"
                icon={<Users className="size-4" aria-hidden />}
              />
              <QuickLink
                to="/challenges/new"
                title="Create challenge"
                description="Add a catalog item"
                icon={<BookOpen className="size-4" aria-hidden />}
              />
              <QuickLink
                to="/admin/ops"
                title="Ops & warm-up"
                description="Runners, LSP, infrastructure"
                icon={<ServerCog className="size-4" aria-hidden />}
              />
            </div>
          </section>

          <aside aria-label="Warm-up status">
            <DashboardWarmUpSummary
              ops={opsQuery.data}
              isLoading={opsQuery.isLoading}
              isError={opsQuery.isError}
            />
          </aside>
        </div>
      </div>
    </AppLayout>
  );
}
