import { useQuery } from "@tanstack/react-query";
import { Alert, Collapse, Spin, Tag } from "antd";
import { ArrowLeft } from "lucide-react";
import { Link, useParams } from "react-router-dom";
import { apiFetch } from "../api/client";
import type { AdminUserChallengeDetailResponse, AdminUserChallengeDetailSubmission, AdminIntegrityEvent } from "../api/types";
import EngagementBadge, { DifficultyChip, LanguageChip } from "../components/admin/AdminBadges";
import AdminPageHero, { AdminReportUserLine } from "../components/admin/AdminPageHero";
import UserIdentityCell, { PassRateCell } from "../components/admin/AdminTableCells";
import AppLayout from "../components/AppLayout";
import CoachMarkdown from "../components/CoachMarkdown";
import CtlCard from "../components/ui/CtlCard";
import { Button } from "@/components/ui/button";
import { ApiPaths } from "../domain/constants";
import { formatDurationMs } from "../utils/formatDuration";
import { cn } from "@/lib/utils";
import type { AdminStatTone } from "../components/admin/AdminStatCard";

function formatWhen(value: string | null) {
  if (!value) {
    return "—";
  }
  return new Date(value).toLocaleString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
}

function submissionLabel(submission: AdminUserChallengeDetailSubmission): string {
  const kind = submission.kind === "RUN" ? "Practice run" : "Graded submit";
  return `${kind} · ${submission.status} · ${formatWhen(submission.createdAt)}`;
}

function FeedbackStatusTag({ status }: { status: string }) {
  const color = status === "pass" ? "success" : status === "warn" ? "warning" : "error";
  return <Tag color={color}>{status}</Tag>;
}

function SubmissionDetailPanel({ submission }: { submission: AdminUserChallengeDetailSubmission }) {
  return (
    <div className="space-y-4">
      <dl className="grid gap-2 text-sm sm:grid-cols-2 lg:grid-cols-4">
        <div>
          <dt className="text-xs text-muted-foreground">Runtime</dt>
          <dd className="font-medium">{submission.runtimeVersion ?? "—"}</dd>
        </div>
        <div>
          <dt className="text-xs text-muted-foreground">Processing</dt>
          <dd className="font-medium tabular-nums">{formatDurationMs(submission.processingMs)}</dd>
        </div>
        <div>
          <dt className="text-xs text-muted-foreground">Updated</dt>
          <dd className="font-medium tabular-nums">{formatWhen(submission.updatedAt)}</dd>
        </div>
        <div>
          <dt className="text-xs text-muted-foreground">Submission ID</dt>
          <dd className="truncate font-mono text-xs">{submission.id}</dd>
        </div>
      </dl>

      <section>
        <h4 className="mb-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
          Submitted code
        </h4>
        <pre className="max-h-64 overflow-auto rounded-lg border border-border bg-muted/30 p-3 text-xs leading-relaxed">
          <code>{submission.solutionCode}</code>
        </pre>
      </section>

      {submission.customTestsCode && (
        <section>
          <h4 className="mb-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            Custom tests
          </h4>
          <pre className="max-h-40 overflow-auto rounded-lg border border-border bg-muted/30 p-3 text-xs leading-relaxed">
            <code>{submission.customTestsCode}</code>
          </pre>
        </section>
      )}

      {submission.report && (
        <section>
          <div className="mb-2 flex flex-wrap items-center gap-2">
            <h4 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Evaluator report
            </h4>
            <Tag color={submission.report.blocked ? "error" : "success"}>
              {submission.report.blocked ? "Did not pass" : "Passed"}
            </Tag>
          </div>

          {submission.report.runnerLogs && (
            <div className="mb-3 grid gap-3 lg:grid-cols-2">
              {submission.report.runnerLogs.stdoutTruncated && (
                <div>
                  <p className="mb-1 text-[10px] font-semibold uppercase text-muted-foreground">Stdout</p>
                  <pre className="max-h-32 overflow-auto rounded border border-border bg-muted/20 p-2 text-xs">
                    {submission.report.runnerLogs.stdoutTruncated}
                  </pre>
                </div>
              )}
              {submission.report.runnerLogs.stderrTruncated && (
                <div>
                  <p className="mb-1 text-[10px] font-semibold uppercase text-muted-foreground">Stderr</p>
                  <pre className="max-h-32 overflow-auto rounded border border-border bg-muted/20 p-2 text-xs">
                    {submission.report.runnerLogs.stderrTruncated}
                  </pre>
                </div>
              )}
            </div>
          )}

          {submission.report.feedback.length > 0 ? (
            <ul className="space-y-3">
              {submission.report.feedback.map((item) => (
                <li
                  key={item.id}
                  className="rounded-lg border border-border/80 bg-card/50 p-3 text-sm"
                >
                  <div className="mb-1 flex flex-wrap items-center gap-2">
                    <span className="font-medium">{item.category}</span>
                    <FeedbackStatusTag status={item.status} />
                  </div>
                  <p className="mb-0 text-muted-foreground">{item.message}</p>
                  {item.aiExplanation && (
                    <div className="mt-2 rounded-md border border-sky-500/20 bg-sky-500/[0.06] p-3">
                      <p className="mb-1 text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
                        AI explanation
                      </p>
                      <CoachMarkdown text={item.aiExplanation} className="text-sm" />
                    </div>
                  )}
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-sm text-muted-foreground">No feedback items on this report.</p>
          )}
        </section>
      )}

      {submission.feedbackActions.length > 0 && (
        <section>
          <h4 className="mb-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            AI coach & enhancements
          </h4>
          <ul className="space-y-3">
            {submission.feedbackActions.map((action) => (
              <li
                key={action.id}
                className="rounded-lg border border-border/80 bg-card/50 p-3"
              >
                <div className="mb-2 flex flex-wrap items-center gap-2">
                  <span className="text-sm font-medium">{action.action}</span>
                  <Tag>{action.status}</Tag>
                  <span className="text-xs text-muted-foreground">{formatWhen(action.createdAt)}</span>
                </div>
                {action.errorMessage && (
                  <Alert type="error" showIcon message={action.errorMessage} className="mb-2" />
                )}
                {action.result && (
                  <CoachMarkdown text={action.result} className="text-sm" />
                )}
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}

function integrityEventLabel(event: AdminIntegrityEvent): string {
  if (event.eventType === "TAB_VISIBLE" || event.eventType === "WINDOW_FOCUS") {
    const away =
      event.awayMs != null && event.awayMs > 0 ? ` (${formatDurationMs(event.awayMs)} away)` : "";
    return `${event.eventType}${away}`;
  }
  if (event.eventType === "LARGE_EDIT") {
    const surface = event.editorSurface === "CUSTOM_TESTS" ? "Custom tests" : "Solution";
    return `LARGE_EDIT · ${surface}${event.charCount != null ? ` (${event.charCount} chars)` : ""}`;
  }
  if (event.eventType === "COPY" || event.eventType === "PASTE" || event.eventType === "CUT") {
    const surface = event.editorSurface === "CUSTOM_TESTS" ? "Custom tests" : "Solution";
    const chars =
      event.charCount != null && event.eventType === "PASTE" ? ` (${event.charCount} chars)` : "";
    return `${event.eventType}${chars} · ${surface}`;
  }
  return event.eventType;
}

function IntegritySignalsPanel({
  stats,
  events,
}: {
  stats: AdminUserChallengeDetailResponse["stats"];
  events: AdminIntegrityEvent[];
}) {
  const clipboardTotal =
    stats.clipboardCopyAttempts + stats.clipboardPasteAttempts + stats.clipboardCutAttempts;
  const total =
    clipboardTotal
    + stats.integrityTabHiddenCount
    + stats.integrityWindowBlurCount
    + stats.integrityLargeEditCount;

  return (
    <CtlCard title={`Integrity signals (${total})`} className="mb-4">
      {total === 0 ? (
        <p className="text-sm text-muted-foreground">
          No integrity signals were recorded for this challenge.
        </p>
      ) : (
        <div className="space-y-4">
          <dl className="grid gap-3 text-sm sm:grid-cols-3 lg:grid-cols-6">
            <div className="rounded-lg border border-border/80 bg-card/50 p-3 text-center">
              <dt className="text-[10px] uppercase text-muted-foreground">Paste</dt>
              <dd className="mt-1 text-lg font-semibold tabular-nums text-amber-700 dark:text-amber-400">
                {stats.clipboardPasteAttempts}
              </dd>
            </div>
            <div className="rounded-lg border border-border/80 bg-card/50 p-3 text-center">
              <dt className="text-[10px] uppercase text-muted-foreground">Copy</dt>
              <dd className="mt-1 text-lg font-semibold tabular-nums">{stats.clipboardCopyAttempts}</dd>
            </div>
            <div className="rounded-lg border border-border/80 bg-card/50 p-3 text-center">
              <dt className="text-[10px] uppercase text-muted-foreground">Cut</dt>
              <dd className="mt-1 text-lg font-semibold tabular-nums">{stats.clipboardCutAttempts}</dd>
            </div>
            <div className="rounded-lg border border-border/80 bg-card/50 p-3 text-center">
              <dt className="text-[10px] uppercase text-muted-foreground">Tab hidden</dt>
              <dd className="mt-1 text-lg font-semibold tabular-nums">{stats.integrityTabHiddenCount}</dd>
            </div>
            <div className="rounded-lg border border-border/80 bg-card/50 p-3 text-center">
              <dt className="text-[10px] uppercase text-muted-foreground">Window blur</dt>
              <dd className="mt-1 text-lg font-semibold tabular-nums">{stats.integrityWindowBlurCount}</dd>
            </div>
            <div className="rounded-lg border border-border/80 bg-card/50 p-3 text-center">
              <dt className="text-[10px] uppercase text-muted-foreground">Large edits</dt>
              <dd className="mt-1 text-lg font-semibold tabular-nums">{stats.integrityLargeEditCount}</dd>
            </div>
          </dl>
          {stats.integrityTotalAwayMs > 0 && (
            <p className="text-sm text-muted-foreground">
              Total time away from the workspace:{" "}
              <strong className="text-foreground">{formatDurationMs(stats.integrityTotalAwayMs)}</strong>
            </p>
          )}
          {events.length > 0 && (
            <ul className="max-h-56 space-y-2 overflow-auto rounded-lg border border-border/80 bg-muted/20 p-3 text-sm">
              {events.map((event) => (
                <li key={event.id} className="flex flex-wrap items-center justify-between gap-2">
                  <span className="font-medium">{integrityEventLabel(event)}</span>
                  <span className="text-xs tabular-nums text-muted-foreground">
                    {formatWhen(event.occurredAt)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </CtlCard>
  );
}

const toneValueClass: Partial<Record<AdminStatTone, string>> = {
  success: "text-emerald-600 dark:text-emerald-400",
  active: "text-sky-600 dark:text-sky-400",
  danger: "text-rose-600 dark:text-rose-400",
  warning: "text-amber-700 dark:text-amber-400",
};

function ChallengeDetailStats({
  stats,
}: {
  stats: AdminUserChallengeDetailResponse["stats"];
}) {
  const metrics: { label: string; value: string | number; tone?: AdminStatTone }[] = [
    { label: "Practice runs", value: stats.practiceRuns, tone: "active" },
    { label: "Graded submits", value: stats.gradedSubmits },
    { label: "Graded passes", value: stats.gradedPasses, tone: "success" },
    { label: "Graded fails", value: stats.gradedFails, tone: "danger" },
    { label: "Cancelled", value: stats.cancelledSubmissions, tone: stats.cancelledSubmissions > 0 ? "warning" : undefined },
    { label: "Enhancements", value: stats.enhancementRequests },
    { label: "Feedback items", value: stats.feedbackItems },
    { label: "Warnings", value: stats.feedbackWarnings, tone: stats.feedbackWarnings > 0 ? "warning" : undefined },
    {
      label: "Integrity",
      value:
        stats.clipboardCopyAttempts
        + stats.clipboardPasteAttempts
        + stats.clipboardCutAttempts
        + stats.integrityTabHiddenCount
        + stats.integrityWindowBlurCount
        + stats.integrityLargeEditCount,
      tone:
        stats.clipboardPasteAttempts
          + stats.clipboardCopyAttempts
          + stats.clipboardCutAttempts
          + stats.integrityTabHiddenCount
          + stats.integrityLargeEditCount
          > 0
          ? "warning"
          : undefined,
    },
    { label: "Avg run", value: formatDurationMs(stats.avgProcessingMs) },
  ];

  return (
    <section className="mb-4 overflow-hidden rounded-xl border border-border/80 bg-card/50 shadow-sm">
      <div className="flex flex-wrap divide-x divide-border">
        {metrics.map((metric) => (
          <div key={metric.label} className="min-w-[5.25rem] flex-1 px-3 py-2.5 text-center">
            <p className="truncate text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
              {metric.label}
            </p>
            <p
              className={cn(
                "mt-0.5 text-base font-semibold tabular-nums leading-none text-foreground",
                metric.tone && toneValueClass[metric.tone],
              )}
            >
              {metric.value}
            </p>
          </div>
        ))}
      </div>
    </section>
  );
}

export default function AdminUserChallengeDetailPage() {
  const { userId = "", challengeSlug = "" } = useParams();

  const detailQuery = useQuery({
    queryKey: ["admin", "users", userId, "challenges", challengeSlug, "detail"],
    queryFn: () =>
      apiFetch<AdminUserChallengeDetailResponse>(
        ApiPaths.adminUserChallengeDetail(userId, challengeSlug),
      ),
    enabled: Boolean(userId && challengeSlug),
  });

  if (detailQuery.isLoading) {
    return (
      <AppLayout>
        <div className="flex min-h-[40vh] flex-col items-center justify-center gap-3">
          <Spin size="large" />
          <p className="text-sm text-muted-foreground">Loading challenge detail…</p>
        </div>
      </AppLayout>
    );
  }

  if (detailQuery.isError || !detailQuery.data) {
    return (
      <AppLayout>
        <Alert
          type="error"
          showIcon
          message="Could not load challenge detail"
          description="The challenge may not exist or the user has not started it."
        />
        <Link
          to={`/admin/users/${userId}/challenge-report`}
          className="mt-4 inline-block no-underline"
        >
          <Button variant="outline">Back to report</Button>
        </Link>
      </AppLayout>
    );
  }

  const detail = detailQuery.data;
  const { stats, user } = detail;
  const reportUrl = `/admin/users/${userId}/challenge-report`;

  return (
    <AppLayout>
      <AdminPageHero
        eyebrow="Challenge detail"
        title={stats.title}
        description={
          <>
            <AdminReportUserLine
              fullName={user.fullName}
              email={user.email}
              className="mb-2"
            />
            <span className="inline-flex flex-wrap items-center gap-2">
              <span className="font-mono text-xs text-muted-foreground">{stats.challengeSlug}</span>
              <LanguageChip language={stats.language} />
              <DifficultyChip difficulty={stats.difficulty} />
              <EngagementBadge status={stats.engagementStatus} />
            </span>
          </>
        }
        extra={
          <Link to={reportUrl} className="no-underline">
            <Button variant="outline" className="gap-1.5">
              <ArrowLeft className="size-4" aria-hidden />
              Back to report
            </Button>
          </Link>
        }
        className="mb-4"
      />

      <section className="mb-4 flex flex-col gap-3 rounded-xl border border-border/80 bg-card/70 p-4 shadow-sm sm:flex-row sm:items-center sm:justify-between">
        <UserIdentityCell fullName={user.fullName} email={user.email} size="sm" />
        <div className="flex flex-wrap items-center gap-4 text-sm">
          <div>
            <p className="text-[10px] uppercase text-muted-foreground">Pass rate</p>
            <PassRateCell percent={stats.passRatePercent} />
          </div>
          <div>
            <p className="text-[10px] uppercase text-muted-foreground">Time to pass</p>
            <p className="font-medium tabular-nums">{formatDurationMs(stats.timeToPassMs)}</p>
          </div>
          <div>
            <p className="text-[10px] uppercase text-muted-foreground">Last activity</p>
            <p className="font-medium tabular-nums">{formatWhen(stats.lastActivityAt)}</p>
          </div>
        </div>
      </section>

      <ChallengeDetailStats stats={stats} />

      <IntegritySignalsPanel stats={stats} events={detail.integrityEvents} />

      <CtlCard title={`Submissions (${detail.submissions.length})`}>
        {detail.submissions.length === 0 ? (
          <p className="text-sm text-muted-foreground">No submissions recorded.</p>
        ) : (
          <Collapse
            accordion
            items={detail.submissions.map((submission, index) => ({
              key: submission.id,
              label: (
                <span className="font-medium">
                  #{detail.submissions.length - index} · {submissionLabel(submission)}
                </span>
              ),
              children: <SubmissionDetailPanel submission={submission} />,
            }))}
          />
        )}
      </CtlCard>
    </AppLayout>
  );
}
