import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Alert,
  App,
  Button as AntButton,
  Input,
  Modal,
  Segmented,
  Select,
  Switch,
  Table,
  Tooltip,
} from "antd";
import type { ColumnsType } from "antd/es/table";
import { Activity, BarChart3, CalendarDays, Gauge, Plus, Search, Shield, UserMinus, Users, UserX, X } from "lucide-react";
import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch, ApiError } from "../api/client";
import type { AdminUserSummary, UserRole } from "../api/types";
import { RoleBadge, StatusBadge } from "../components/admin/AdminBadges";
import AdminPageHero, { AdminInfoCallout } from "../components/admin/AdminPageHero";
import AdminStatCard from "../components/admin/AdminStatCard";
import { AdminFilterGroup, AdminFilterPanel } from "../components/admin/AdminStatCard";
import UserIdentityCell, { CatalogProgressCell, CenteredTableCell } from "../components/admin/AdminTableCells";
import AdminCreateUserModal from "../components/admin-users/AdminCreateUserModal";
import AdminChallengeQuotaModal from "../components/admin-users/AdminChallengeQuotaModal";
import AdminIntegrityMonitoringModal from "../components/admin-users/AdminIntegrityMonitoringModal";
import AppLayout from "../components/AppLayout";
import CtlCard from "../components/ui/CtlCard";
import { Button } from "@/components/ui/button";
import { ApiPaths } from "../domain/constants";
import { useAuth } from "../auth/useAuth";

type ActivityFilter = "ALL" | "ACTIVE_7D" | "ACTIVE_30D" | "IDLE_30D";
type RoleFilter = "ALL" | UserRole;
type ProgressFilter = "ALL" | "HAS_ACTIVITY" | "NO_ACTIVITY" | "LOW_PROGRESS";

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

function daysSince(value: string | null): number | null {
  if (!value) {
    return null;
  }
  const diffMs = Date.now() - new Date(value).getTime();
  return diffMs / (1000 * 60 * 60 * 24);
}

function isRecentlyActive(user: AdminUserSummary, withinDays: number): boolean {
  if (!user.active || !user.lastActivityAt) {
    return false;
  }
  const days = daysSince(user.lastActivityAt);
  return days !== null && days <= withinDays;
}

function matchesSearch(user: AdminUserSummary, query: string): boolean {
  const normalized = query.trim().toLowerCase();
  if (!normalized) {
    return true;
  }
  const haystack = [user.fullName, user.email].filter(Boolean).join(" ").toLowerCase();
  return haystack.includes(normalized);
}

function matchesProgressFilter(user: AdminUserSummary, filter: ProgressFilter): boolean {
  switch (filter) {
    case "HAS_ACTIVITY":
      return user.submissionsTotal > 0 || user.challengesPassed > 0 || Boolean(user.lastActivityAt);
    case "NO_ACTIVITY":
      return user.submissionsTotal === 0 && !user.lastActivityAt;
    case "LOW_PROGRESS":
      return user.challengesStarted > 0 && user.completionPercent < 25;
    default:
      return true;
  }
}

function hasReportData(row: AdminUserSummary): boolean {
  return (
    row.submissionsTotal > 0 ||
    row.challengesPassed > 0 ||
    row.completionPercent > 0 ||
    Boolean(row.lastActivityAt)
  );
}

function exerciseLimitLabel(row: AdminUserSummary): string {
  if (row.role !== "USER") {
    return "—";
  }
  if (row.effectiveChallengeLimit == null) {
    return "Unlimited";
  }
  return String(row.effectiveChallengeLimit);
}

function exerciseLimitHint(row: AdminUserSummary): string | undefined {
  if (row.role !== "USER") {
    return undefined;
  }
  if (row.challengeQuotaOverride == null) {
    return "platform default";
  }
  if (row.challengeQuotaOverride === 0) {
    return "override · unlimited";
  }
  return "override · extended";
}

export default function AdminUsersPage() {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const { user: currentUser } = useAuth();
  const navigate = useNavigate();
  const [createOpen, setCreateOpen] = useState(false);
  const [includeInactive, setIncludeInactive] = useState(false);
  const [activityFilter, setActivityFilter] = useState<ActivityFilter>("ALL");
  const [roleFilter, setRoleFilter] = useState<RoleFilter>("ALL");
  const [progressFilter, setProgressFilter] = useState<ProgressFilter>("ALL");
  const [searchQuery, setSearchQuery] = useState("");
  const [deactivateTarget, setDeactivateTarget] = useState<AdminUserSummary | null>(null);
  const [quotaTarget, setQuotaTarget] = useState<AdminUserSummary | null>(null);
  const [integrityTarget, setIntegrityTarget] = useState<AdminUserSummary | null>(null);

  const usersQuery = useQuery({
    queryKey: ["admin", "users", includeInactive],
    queryFn: () =>
      apiFetch<AdminUserSummary[]>(
        `${ApiPaths.ADMIN_USERS}?includeInactive=${includeInactive}`,
      ),
  });

  const deactivateMutation = useMutation({
    mutationFn: (userId: string) =>
      apiFetch<void>(ApiPaths.adminUserDeactivate(userId), { method: "POST" }),
    onSuccess: () => {
      message.success("User deactivated");
      setDeactivateTarget(null);
      void queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      void queryClient.invalidateQueries({ queryKey: ["admin", "dashboard"] });
    },
    onError: (error) => {
      void queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      if (error instanceof ApiError) {
        message.error(error.message);
        return;
      }
      message.error("Could not deactivate user");
    },
  });

  const users = useMemo(() => usersQuery.data ?? [], [usersQuery.data]);

  const soleActiveAdminId = useMemo(() => {
    const activeAdmins = users.filter((row) => row.active && row.role === "ADMIN");
    return activeAdmins.length === 1 ? activeAdmins[0]!.id : null;
  }, [users]);

  const insights = useMemo(() => {
    const activeUsers = users.filter((row) => row.active);
    const learners = activeUsers.filter((row) => row.role === "USER");
    const active7d = activeUsers.filter((row) => isRecentlyActive(row, 7)).length;
    const active30d = activeUsers.filter((row) => isRecentlyActive(row, 30)).length;
    const deactivated = users.filter((row) => !row.active).length;
    const noActivity = activeUsers.filter(
      (row) => row.submissionsTotal === 0 && !row.lastActivityAt,
    ).length;
    const lowProgress = activeUsers.filter(
      (row) => row.challengesStarted > 0 && row.completionPercent < 25,
    ).length;
    const idle30d = activeUsers.filter(
      (row) => row.lastActivityAt && !isRecentlyActive(row, 30),
    ).length;

    return {
      activeUsers: activeUsers.length,
      learners: learners.length,
      admins: activeUsers.filter((row) => row.role === "ADMIN").length,
      active7d,
      active30d,
      deactivated,
      noActivity,
      lowProgress,
      idle30d,
    };
  }, [users]);

  const filteredUsers = useMemo(() => {
    return users.filter((row) => {
      if (!matchesSearch(row, searchQuery)) {
        return false;
      }
      if (roleFilter !== "ALL" && row.role !== roleFilter) {
        return false;
      }
      if (!includeInactive && !row.active) {
        return false;
      }
      if (activityFilter === "ACTIVE_7D" && !isRecentlyActive(row, 7)) {
        return false;
      }
      if (activityFilter === "ACTIVE_30D" && !isRecentlyActive(row, 30)) {
        return false;
      }
      if (
        activityFilter === "IDLE_30D" &&
        (!row.lastActivityAt || isRecentlyActive(row, 30))
      ) {
        return false;
      }
      if (!matchesProgressFilter(row, progressFilter)) {
        return false;
      }
      return true;
    });
  }, [users, searchQuery, roleFilter, includeInactive, activityFilter, progressFilter]);

  const hasActiveFilters =
    searchQuery.trim().length > 0 ||
    roleFilter !== "ALL" ||
    activityFilter !== "ALL" ||
    progressFilter !== "ALL";

  const clearFilters = () => {
    setSearchQuery("");
    setRoleFilter("ALL");
    setActivityFilter("ALL");
    setProgressFilter("ALL");
  };

  const columns: ColumnsType<AdminUserSummary> = [
    {
      title: "User",
      key: "user",
      width: 200,
      ellipsis: true,
      align: "center",
      render: (_, row) => (
        <CenteredTableCell>
          <UserIdentityCell fullName={row.fullName} email={row.email} size="sm" />
        </CenteredTableCell>
      ),
    },
    {
      title: "Role",
      dataIndex: "role",
      key: "role",
      width: 110,
      align: "center",
      render: (role: string) => <RoleBadge role={role} />,
    },
    {
      title: "Status",
      key: "status",
      width: 120,
      align: "center",
      render: (_, row) => <StatusBadge active={row.active} />,
    },
    {
      title: "Last activity",
      dataIndex: "lastActivityAt",
      key: "lastActivityAt",
      width: 160,
      align: "center",
      render: (value: string | null) => (
        <span className="text-sm tabular-nums text-muted-foreground">{formatWhen(value)}</span>
      ),
      sorter: (a, b) =>
        (a.lastActivityAt ? new Date(a.lastActivityAt).getTime() : 0) -
        (b.lastActivityAt ? new Date(b.lastActivityAt).getTime() : 0),
      defaultSortOrder: "descend",
    },
    {
      title: "Submissions",
      key: "submissions",
      width: 100,
      align: "center",
      render: (_, row) => (
        <span className="text-sm font-medium tabular-nums">{row.submissionsTotal}</span>
      ),
      sorter: (a, b) => a.submissionsTotal - b.submissionsTotal,
    },
    {
      title: "Progress",
      key: "progress",
      width: 168,
      align: "center",
      render: (_, row) => (
        <CenteredTableCell>
          <CatalogProgressCell
            passed={row.challengesPassed}
            total={row.challengesStarted}
            percent={row.completionPercent}
          />
        </CenteredTableCell>
      ),
      sorter: (a, b) => a.completionPercent - b.completionPercent,
    },
    {
      title: "Joined",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 160,
      align: "center",
      render: (value: string) => (
        <span className="text-sm tabular-nums text-muted-foreground">{formatWhen(value)}</span>
      ),
    },
    {
      title: "Exercise limit",
      key: "exerciseLimit",
      width: 120,
      align: "center",
      render: (_, row) => (
        <div className="text-center text-sm">
          <p className="mb-0 font-medium tabular-nums text-foreground">{exerciseLimitLabel(row)}</p>
          {exerciseLimitHint(row) && (
            <p className="mb-0 mt-0.5 text-[10px] uppercase tracking-wide text-muted-foreground">
              {exerciseLimitHint(row)}
            </p>
          )}
        </div>
      ),
    },
    {
      title: "Challenge report",
      key: "actions",
      width: 188,
      fixed: "right",
      align: "center",
      render: (_, row) => {
        const hasReport = hasReportData(row);
        return (
          <div
            className="flex flex-wrap items-center justify-center gap-1"
            onClick={(event) => event.stopPropagation()}
            onKeyDown={(event) => event.stopPropagation()}
          >
            {hasReport ? (
              <Link to={`/admin/users/${row.id}/challenge-report`} className="no-underline">
                <Button variant="ghost" size="sm" className="h-7 gap-1 px-2 text-xs">
                  <BarChart3 className="size-3.5" aria-hidden />
                  Report
                </Button>
              </Link>
            ) : (
              <Tooltip title="No challenge activity yet — report unlocks after first run or submit">
                <span>
                  <Button variant="ghost" size="sm" className="h-7 gap-1 px-2 text-xs" disabled>
                    <BarChart3 className="size-3.5" aria-hidden />
                    Report
                  </Button>
                </span>
              </Tooltip>
            )}
            {row.active ? (
              <>
                {row.role === "USER" && (
                  <>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-7 gap-1 px-2 text-xs"
                      onClick={() => setQuotaTarget(row)}
                    >
                      <Gauge className="size-3.5" aria-hidden />
                      Limit
                    </Button>
                    <Tooltip
                      title={
                        row.integrityMonitoringDisabled
                          ? "Integrity monitoring disabled for this learner"
                          : "Configure integrity monitoring"
                      }
                    >
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-7 gap-1 px-2 text-xs"
                        onClick={() => setIntegrityTarget(row)}
                      >
                        <Shield className="size-3.5" aria-hidden />
                        Integrity
                      </Button>
                    </Tooltip>
                  </>
                )}
                <Tooltip
                  title={
                    row.id === soleActiveAdminId
                      ? "At least one administrator must stay active"
                      : row.id === currentUser?.id
                        ? "You cannot deactivate your own account"
                        : undefined
                  }
                >
                  <span>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-7 px-2 text-xs text-destructive hover:text-destructive"
                      disabled={
                        row.id === currentUser?.id || row.id === soleActiveAdminId
                      }
                      onClick={() => setDeactivateTarget(row)}
                    >
                      <UserX className="size-3.5" aria-hidden />
                      Deactivate
                    </Button>
                  </span>
                </Tooltip>
              </>
            ) : null}
          </div>
        );
      },
    },
  ];

  return (
    <AppLayout>
      <AdminPageHero
        eyebrow="Users & activity"
        title="User directory"
        description={
          <>
            Search and filter learners, track catalog progress, and open the{" "}
            <strong className="font-medium text-foreground">challenge report</strong> for per-user
            submission history, pass rates, AI coach feedback, and abandonment signals.
          </>
        }
        extra={
          <>
            <Link to="/admin/access-requests" className="no-underline">
              <Button variant="outline">Access requests</Button>
            </Link>
            <Button className="gap-1.5" onClick={() => setCreateOpen(true)}>
              <Plus className="size-4" aria-hidden />
              Create user
            </Button>
          </>
        }
      />

      {usersQuery.isError && (
        <Alert
          type="error"
          showIcon
          className="mb-4"
          message="Could not load users"
          description="Refresh the page or check that the API is running."
        />
      )}

      <div className="mb-4 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <AdminStatCard
          label="Active accounts"
          value={insights.activeUsers}
          hint={`${insights.learners} learners · ${insights.admins} admins`}
          icon={Users}
        />
        <AdminStatCard
          label="Active last 7 days"
          value={insights.active7d}
          hint="Submission activity"
          icon={Activity}
          tone="success"
        />
        <AdminStatCard
          label="Active last 30 days"
          value={insights.active30d}
          hint={`${insights.idle30d} idle 30+ days`}
          icon={CalendarDays}
          tone="active"
        />
        <AdminStatCard
          label="Deactivated"
          value={includeInactive ? insights.deactivated : "—"}
          hint={includeInactive ? "Shown in directory" : "Enable toggle below"}
          icon={UserMinus}
          tone={includeInactive && insights.deactivated > 0 ? "warning" : "neutral"}
        />
      </div>

      <AdminInfoCallout title="Challenge reports" className="mb-4 mt-6">
        Use <strong className="text-foreground">Report</strong> on any learner with activity to open
        their challenge report — started challenges, pass rates, run history, and drill-down detail
        with evaluator feedback and AI coach reviews. Click a row to open the report quickly.
        Learners default to {users[0]?.platformDefaultChallengeLimit ?? 5} started exercises; use{" "}
        <strong className="text-foreground">Limit</strong> to extend selected users.
      </AdminInfoCallout>

      {(insights.noActivity > 0 || insights.lowProgress > 0 || insights.idle30d > 0) && (
        <div className="mb-6 flex flex-wrap items-center gap-2">
          <span className="text-xs font-medium text-muted-foreground">Quick filters:</span>
          {insights.noActivity > 0 && (
            <Button
              variant="outline"
              size="sm"
              className="h-7 text-xs"
              onClick={() => {
                setProgressFilter("NO_ACTIVITY");
                setActivityFilter("ALL");
              }}
            >
              No activity ({insights.noActivity})
            </Button>
          )}
          {insights.lowProgress > 0 && (
            <Button
              variant="outline"
              size="sm"
              className="h-7 text-xs"
              onClick={() => setProgressFilter("LOW_PROGRESS")}
            >
              Below 25% ({insights.lowProgress})
            </Button>
          )}
          {insights.idle30d > 0 && (
            <Button
              variant="outline"
              size="sm"
              className="h-7 text-xs"
              onClick={() => setActivityFilter("IDLE_30D")}
            >
              Idle 30+ days ({insights.idle30d})
            </Button>
          )}
        </div>
      )}

      <CtlCard title="Directory">
        <AdminFilterPanel
          className="mb-4"
          footer={
            <>
              Showing <strong className="text-foreground">{filteredUsers.length}</strong> of{" "}
              {users.length} user{users.length === 1 ? "" : "s"}
              {hasActiveFilters ? " matching filters" : ""}
            </>
          }
        >
          <div className="flex flex-col gap-4">
            <div className="flex flex-col gap-3 lg:flex-row lg:items-end">
              <div className="flex-1">
                <AdminFilterGroup label="Search">
                  <Input
                    allowClear
                    prefix={<Search className="size-4 text-muted-foreground" aria-hidden />}
                    placeholder="Name or email"
                    value={searchQuery}
                    onChange={(event) => setSearchQuery(event.target.value)}
                    aria-label="Search users by name or email"
                  />
                </AdminFilterGroup>
              </div>
              <div className="flex flex-wrap items-center gap-3 lg:pb-0.5">
                <label className="inline-flex items-center gap-2 text-xs text-muted-foreground">
                  <Switch
                    size="small"
                    checked={includeInactive}
                    onChange={setIncludeInactive}
                    aria-label="Include deactivated users"
                  />
                  Include deactivated
                </label>
                {hasActiveFilters && (
                  <AntButton
                    type="text"
                    size="small"
                    icon={<X className="size-4" aria-hidden />}
                    onClick={clearFilters}
                  >
                    Clear filters
                  </AntButton>
                )}
              </div>
            </div>

            <div className="grid gap-4 md:grid-cols-3">
              <AdminFilterGroup label="Role">
                <Segmented
                  block
                  size="small"
                  value={roleFilter}
                  onChange={(value) => setRoleFilter(value as RoleFilter)}
                  options={[
                    { label: "All", value: "ALL" },
                    { label: "Learners", value: "USER" },
                    { label: "Admins", value: "ADMIN" },
                  ]}
                />
              </AdminFilterGroup>
              <AdminFilterGroup label="Activity">
                <Segmented
                  block
                  size="small"
                  value={activityFilter}
                  onChange={(value) => setActivityFilter(value as ActivityFilter)}
                  options={[
                    { label: "Any", value: "ALL" },
                    { label: "7 days", value: "ACTIVE_7D" },
                    { label: "30 days", value: "ACTIVE_30D" },
                  ]}
                />
              </AdminFilterGroup>
              <AdminFilterGroup label="Progress">
                <Select
                  size="small"
                  value={progressFilter}
                  onChange={setProgressFilter}
                  className="w-full"
                  options={[
                    { value: "ALL", label: "Any progress" },
                    { value: "HAS_ACTIVITY", label: "Has submissions" },
                    { value: "NO_ACTIVITY", label: "No activity yet" },
                    { value: "LOW_PROGRESS", label: "Below 25% complete" },
                  ]}
                />
              </AdminFilterGroup>
            </div>
          </div>
        </AdminFilterPanel>

        <Table
          rowKey="id"
          loading={usersQuery.isLoading}
          columns={columns}
          dataSource={filteredUsers}
          pagination={{ pageSize: 20, showSizeChanger: true }}
          scroll={{ x: 1160 }}
          rowClassName={(record) =>
            !record.active ? "opacity-70" : hasReportData(record) ? "cursor-pointer" : ""
          }
          onRow={(row) =>
            hasReportData(row)
              ? {
                  onClick: () => navigate(`/admin/users/${row.id}/challenge-report`),
                }
              : {}
          }
          locale={{
            emptyText: hasActiveFilters
              ? "No users match your search or filters."
              : "No users found.",
          }}
        />
      </CtlCard>

      <AdminCreateUserModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={() => void queryClient.invalidateQueries({ queryKey: ["admin", "users"] })}
      />

      <AdminChallengeQuotaModal
        user={quotaTarget}
        open={quotaTarget !== null}
        onClose={() => setQuotaTarget(null)}
        onUpdated={() => void queryClient.invalidateQueries({ queryKey: ["admin", "users"] })}
      />

      <AdminIntegrityMonitoringModal
        user={integrityTarget}
        open={integrityTarget !== null}
        onClose={() => setIntegrityTarget(null)}
        onUpdated={() => void queryClient.invalidateQueries({ queryKey: ["admin", "users"] })}
      />

      <Modal
        title="Deactivate user"
        open={deactivateTarget !== null}
        okText="Deactivate"
        okButtonProps={{ danger: true, loading: deactivateMutation.isPending }}
        onOk={() => {
          if (!deactivateTarget) {
            return;
          }
          return deactivateMutation.mutateAsync(deactivateTarget.id);
        }}
        onCancel={() => setDeactivateTarget(null)}
      >
        {deactivateTarget && (
          <p className="text-sm text-muted-foreground">
            Deactivate <strong className="text-foreground">{deactivateTarget.fullName}</strong> (
            {deactivateTarget.email})? They will not be able to sign in again. Submission history is
            retained.
            {deactivateTarget.role === "ADMIN" && soleActiveAdminId === deactivateTarget.id && (
              <>
                {" "}
                This account is the only active administrator and cannot be deactivated while you
                remain signed in.
              </>
            )}
          </p>
        )}
      </Modal>
    </AppLayout>
  );
}
