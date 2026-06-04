import { useQuery } from "@tanstack/react-query";
import { Alert, Spin, Table } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { apiFetch } from "../api/client";
import type {
  AdminUserChallengeReportResponse,
  AdminUserChallengeReportRow,
} from "../api/types";
import EngagementBadge, { DifficultyChip, LanguageChip } from "../components/admin/AdminBadges";
import ChallengeReportSidebar, {
  type ReportFilter,
  type StatusFilterCounts,
} from "../components/admin/ChallengeReportSidebar";
import ChallengeReportSummary from "../components/admin/ChallengeReportSummary";
import AdminPageHero, { AdminReportUserLine } from "../components/admin/AdminPageHero";
import { PassRateCell, CenteredTableCell } from "../components/admin/AdminTableCells";
import AppLayout from "../components/AppLayout";
import CtlCard from "../components/ui/CtlCard";
import { Button } from "@/components/ui/button";
import { ApiPaths } from "../domain/constants";
import { formatDurationMs } from "../utils/formatDuration";

function formatWhen(value: string | null) {
  if (!value) {
    return "—";
  }
  return new Date(value).toLocaleString(undefined, {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
}

function matchesFilter(row: AdminUserChallengeReportRow, filter: ReportFilter): boolean {
  switch (filter) {
    case "ACTIVE":
      return row.engagementStatus === "IN_PROGRESS";
    case "PASSED":
      return row.engagementStatus === "PASSED";
    case "FAILED":
      return row.engagementStatus === "FAILED";
    case "ABANDONED":
      return row.likelyAbandoned || row.engagementStatus === "LIKELY_ABANDONED";
    default:
      return true;
  }
}

function countStatusFilters(challenges: AdminUserChallengeReportRow[]): StatusFilterCounts {
  return {
    all: challenges.length,
    inProgress: challenges.filter((row) => row.engagementStatus === "IN_PROGRESS").length,
    passed: challenges.filter((row) => row.engagementStatus === "PASSED").length,
    failed: challenges.filter((row) => row.engagementStatus === "FAILED").length,
    abandoned: challenges.filter(
      (row) => row.likelyAbandoned || row.engagementStatus === "LIKELY_ABANDONED",
    ).length,
  };
}

function matchesChallengeSearch(row: AdminUserChallengeReportRow, query: string): boolean {
  const normalized = query.trim().toLowerCase();
  if (!normalized) {
    return true;
  }
  return (
    row.title.toLowerCase().includes(normalized) ||
    row.challengeSlug.toLowerCase().includes(normalized) ||
    row.language.toLowerCase().includes(normalized)
  );
}

export default function AdminUserChallengeReportPage() {
  const { userId = "" } = useParams();
  const navigate = useNavigate();
  const [filter, setFilter] = useState<ReportFilter>("ALL");
  const [searchQuery, setSearchQuery] = useState("");

  const reportQuery = useQuery({
    queryKey: ["admin", "users", userId, "challenge-report"],
    queryFn: () =>
      apiFetch<AdminUserChallengeReportResponse>(ApiPaths.adminUserChallengeReport(userId)),
    enabled: Boolean(userId),
  });

  const report = reportQuery.data;
  const statusCounts = useMemo(
    () => countStatusFilters(report?.challenges ?? []),
    [report?.challenges],
  );
  const filteredRows = useMemo(
    () =>
      (report?.challenges ?? []).filter(
        (row) => matchesFilter(row, filter) && matchesChallengeSearch(row, searchQuery),
      ),
    [report?.challenges, filter, searchQuery],
  );

  const columns: ColumnsType<AdminUserChallengeReportRow> = [
    {
      title: "Challenge",
      key: "challenge",
      fixed: "left",
      width: 240,
      align: "center",
      render: (_, row) => (
        <div className="min-w-0 py-0.5 text-center">
          <p className="mb-0 truncate font-medium text-foreground">{row.title}</p>
          <p className="mb-0 truncate font-mono text-[11px] text-muted-foreground">
            {row.challengeSlug}
          </p>
        </div>
      ),
    },
    {
      title: "Language",
      dataIndex: "language",
      key: "language",
      width: 108,
      align: "center",
      render: (value: string) => <LanguageChip language={value} />,
    },
    {
      title: "Level",
      dataIndex: "difficulty",
      key: "difficulty",
      width: 88,
      align: "center",
      render: (value: string | null) => <DifficultyChip difficulty={value} />,
    },
    {
      title: "Status",
      dataIndex: "engagementStatus",
      key: "engagementStatus",
      width: 132,
      align: "center",
      render: (_, row) => <EngagementBadge status={row.engagementStatus} />,
    },
    {
      title: "Pass rate",
      key: "passRate",
      width: 112,
      align: "center",
      render: (_, row) => (
        <CenteredTableCell>
          <PassRateCell percent={row.passRatePercent} />
        </CenteredTableCell>
      ),
      sorter: (a, b) => (a.passRatePercent ?? -1) - (b.passRatePercent ?? -1),
    },
    {
      title: "Runs",
      key: "runs",
      width: 120,
      align: "center",
      render: (_, row) => (
        <div className="text-center text-sm tabular-nums">
          <span className="text-muted-foreground">{row.practiceRuns}</span>
          <span className="mx-1 text-border">/</span>
          <span className="font-medium">{row.gradedSubmits}</span>
          <p className="mb-0 mt-0.5 text-[10px] uppercase tracking-wide text-muted-foreground">
            practice · graded
          </p>
          {row.cancelledSubmissions > 0 && (
            <p className="mb-0 mt-1 text-[11px] text-rose-600 dark:text-rose-400">
              {row.cancelledSubmissions} cancelled
            </p>
          )}
        </div>
      ),
    },
    {
      title: "Time to pass",
      key: "timeToPass",
      width: 108,
      align: "center",
      render: (_, row) => (
        <span className="text-sm tabular-nums text-muted-foreground">
          {formatDurationMs(row.timeToPassMs)}
        </span>
      ),
    },
    {
      title: "Avg run",
      key: "avgProcessing",
      width: 96,
      align: "center",
      render: (_, row) => (
        <span className="text-sm tabular-nums text-muted-foreground">
          {formatDurationMs(row.avgProcessingMs)}
        </span>
      ),
    },
    {
      title: "Last activity",
      dataIndex: "lastActivityAt",
      key: "lastActivityAt",
      width: 148,
      align: "center",
      render: (value: string | null) => (
        <span className="text-sm tabular-nums text-muted-foreground">{formatWhen(value)}</span>
      ),
      sorter: (a, b) =>
        (a.lastActivityAt ? new Date(a.lastActivityAt).getTime() : 0) -
        (b.lastActivityAt ? new Date(b.lastActivityAt).getTime() : 0),
    },
  ];

  if (reportQuery.isLoading) {
    return (
      <AppLayout>
        <div className="flex min-h-[40vh] flex-col items-center justify-center gap-3">
          <Spin size="large" />
          <p className="text-sm text-muted-foreground">Loading challenge report…</p>
        </div>
      </AppLayout>
    );
  }

  if (reportQuery.isError || !report) {
    return (
      <AppLayout>
        <Alert
          type="error"
          showIcon
          message="Could not load challenge report"
          description="The user may not exist or the API is unavailable."
        />
        <Link to="/admin/users" className="mt-4 inline-block no-underline">
          <Button variant="outline">Back to users</Button>
        </Link>
      </AppLayout>
    );
  }

  const hasActiveFilters = searchQuery.trim().length > 0 || filter !== "ALL";

  return (
    <AppLayout>
      <AdminPageHero
        eyebrow="Challenge report"
        title="Learner activity"
        description={
          <>
            <AdminReportUserLine
              fullName={report.user.fullName}
              email={report.user.email}
              className="mb-2"
            />
            <p className="mb-0">
              Search and filter on the left; summary metrics and per-challenge detail on the right.
            </p>
          </>
        }
        className="mb-5"
      />

      <div className="flex flex-col gap-6 xl:flex-row xl:items-start">
        <ChallengeReportSidebar
          user={report.user}
          filter={filter}
          onFilterChange={setFilter}
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
          statusCounts={statusCounts}
          filteredCount={filteredRows.length}
          hasActiveFilters={hasActiveFilters}
        />

        <main className="min-w-0 flex-1">
          <ChallengeReportSummary summary={report.summary} />

          <CtlCard title="Started challenges">
            <Table
              rowKey="challengeSlug"
              columns={columns}
              dataSource={filteredRows}
              scroll={{ x: 1100 }}
              pagination={{ pageSize: 15, showSizeChanger: true }}
              onRow={(row) => ({
                onClick: () =>
                  navigate(`/admin/users/${userId}/challenge-report/${row.challengeSlug}`),
                className: "cursor-pointer",
              })}
              locale={{
                emptyText:
                  report.challenges.length === 0
                    ? "This user has not started any challenges yet."
                    : "No started challenges match this filter.",
              }}
            />
          </CtlCard>
        </main>
      </div>
    </AppLayout>
  );
}
