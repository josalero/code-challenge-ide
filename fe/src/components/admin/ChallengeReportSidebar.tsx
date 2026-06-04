import { ArrowLeft, BarChart3, Search } from "lucide-react";
import { Input } from "antd";
import { Link } from "react-router-dom";
import type { AdminUserChallengeReportResponse } from "@/api/types";
import { Button } from "@/components/ui/button";
import { RoleBadge, StatusBadge } from "./AdminBadges";
import { AdminFilterGroup } from "./AdminStatCard";
import UserIdentityCell from "./AdminTableCells";
import { cn } from "@/lib/utils";

type ReportFilter = "ALL" | "ACTIVE" | "PASSED" | "FAILED" | "ABANDONED";

type StatusFilterCounts = {
  all: number;
  inProgress: number;
  passed: number;
  failed: number;
  abandoned: number;
};

type Props = {
  user: AdminUserChallengeReportResponse["user"];
  filter: ReportFilter;
  onFilterChange: (filter: ReportFilter) => void;
  searchQuery: string;
  onSearchChange: (query: string) => void;
  statusCounts: StatusFilterCounts;
  filteredCount: number;
  hasActiveFilters: boolean;
};

type CountTone = "neutral" | "active" | "success" | "danger" | "warning";

const countToneClass: Record<CountTone, string> = {
  neutral: "bg-muted text-muted-foreground",
  active: "bg-sky-500/15 text-sky-700 dark:text-sky-300",
  success: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-300",
  danger: "bg-rose-500/15 text-rose-700 dark:text-rose-300",
  warning: "bg-amber-500/15 text-amber-800 dark:text-amber-300",
};

function FilterOption({
  active,
  label,
  count,
  countTone = "neutral",
  onClick,
}: {
  active: boolean;
  label: string;
  count: number;
  countTone?: CountTone;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "flex w-full items-center justify-between gap-2 rounded-lg border px-3 py-2 text-left text-sm transition-colors",
        active
          ? "border-emerald-500/40 bg-emerald-500/10 font-medium text-foreground"
          : "border-transparent bg-transparent text-muted-foreground hover:border-border/60 hover:bg-muted/40 hover:text-foreground",
      )}
    >
      <span>{label}</span>
      <span
        className={cn(
          "inline-flex min-w-[1.375rem] shrink-0 items-center justify-center rounded-full px-1.5 py-0.5 text-[11px] font-semibold tabular-nums",
          countToneClass[countTone],
        )}
      >
        {count}
      </span>
    </button>
  );
}

export default function ChallengeReportSidebar({
  user,
  filter,
  onFilterChange,
  searchQuery,
  onSearchChange,
  statusCounts,
  filteredCount,
  hasActiveFilters,
}: Props) {
  return (
    <aside className="flex w-full shrink-0 flex-col gap-4 xl:sticky xl:top-4 xl:w-[17.5rem] xl:max-w-[17.5rem]">
      <section className="rounded-xl border border-border/80 bg-card/70 p-4 shadow-sm">
        <UserIdentityCell fullName={user.fullName} email={user.email} size="sm" />
        <div className="mt-3 flex flex-wrap gap-2">
          <RoleBadge role={user.role} />
          <StatusBadge active={user.active} />
        </div>
        <Link to="/admin/users" className="mt-3 block no-underline">
          <Button variant="outline" size="sm" className="w-full gap-1.5">
            <ArrowLeft className="size-3.5" aria-hidden />
            Users directory
          </Button>
        </Link>
      </section>

      <section className="rounded-xl border border-border/80 bg-muted/25 p-4 shadow-inner">
        <AdminFilterGroup label="Search">
          <Input
            allowClear
            prefix={<Search className="size-4 text-muted-foreground" aria-hidden />}
            placeholder="Title, slug, language"
            value={searchQuery}
            onChange={(event) => onSearchChange(event.target.value)}
            aria-label="Search started challenges"
          />
        </AdminFilterGroup>

        <div className="mt-4">
          <p className="mb-2 text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
            Status
          </p>
          <div className="flex flex-col gap-1">
            <FilterOption
              active={filter === "ALL"}
              label="All started"
              count={statusCounts.all}
              onClick={() => onFilterChange("ALL")}
            />
            <FilterOption
              active={filter === "ACTIVE"}
              label="In progress"
              count={statusCounts.inProgress}
              countTone="active"
              onClick={() => onFilterChange("ACTIVE")}
            />
            <FilterOption
              active={filter === "PASSED"}
              label="Passed"
              count={statusCounts.passed}
              countTone="success"
              onClick={() => onFilterChange("PASSED")}
            />
            <FilterOption
              active={filter === "FAILED"}
              label="Failed"
              count={statusCounts.failed}
              countTone="danger"
              onClick={() => onFilterChange("FAILED")}
            />
            <FilterOption
              active={filter === "ABANDONED"}
              label="Likely abandoned"
              count={statusCounts.abandoned}
              countTone="warning"
              onClick={() => onFilterChange("ABANDONED")}
            />
          </div>
        </div>

        <p className="mt-4 flex items-center justify-center gap-1.5 border-t border-border/60 pt-3 text-center text-xs text-muted-foreground">
          <BarChart3 className="size-3.5 shrink-0" aria-hidden />
          {filteredCount} of {statusCounts.all} shown
          {hasActiveFilters ? " (filtered)" : ""}
        </p>
      </section>
    </aside>
  );
}

export type { ReportFilter, StatusFilterCounts };
