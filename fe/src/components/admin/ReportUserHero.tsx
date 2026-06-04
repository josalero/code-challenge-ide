import { ArrowLeft, Mail } from "lucide-react";
import { Link } from "react-router-dom";
import type { AdminUserChallengeReportResponse } from "@/api/types";
import { Button } from "@/components/ui/button";
import { RoleBadge, StatusBadge } from "./AdminBadges";
import UserIdentityCell from "./AdminTableCells";

function HeroStat({ label, value, tone }: { label: string; value: string | number; tone?: string }) {
  return (
    <div className="rounded-lg border border-border/60 bg-background/60 px-4 py-2.5 text-center min-w-[5.5rem]">
      <p className="text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
        {label}
      </p>
      <p className={`mt-0.5 text-xl font-semibold tabular-nums ${tone ?? "text-foreground"}`}>
        {value}
      </p>
    </div>
  );
}

export default function ReportUserHero({
  user,
  summary,
}: {
  user: AdminUserChallengeReportResponse["user"];
  summary: AdminUserChallengeReportResponse["summary"];
}) {
  const gradedPass =
    summary.gradedPassRatePercent == null ? "—" : `${summary.gradedPassRatePercent}%`;

  return (
    <section className="mb-6 overflow-hidden rounded-xl border border-border/80 bg-card/70 shadow-sm">
      <div className="flex flex-col gap-5 p-5 md:flex-row md:items-center md:justify-between">
        <div className="flex min-w-0 flex-col gap-3 sm:flex-row sm:items-center">
          <UserIdentityCell fullName={user.fullName} email={user.email} />
          <div className="flex flex-wrap gap-2 sm:pl-1">
            <RoleBadge role={user.role} />
            <StatusBadge active={user.active} />
          </div>
        </div>

        <div className="flex flex-wrap gap-2 md:justify-end">
          <HeroStat label="Started" value={summary.started} tone="text-sky-600 dark:text-sky-400" />
          <HeroStat
            label="Completion"
            value={`${summary.completionPercent}%`}
            tone="text-emerald-600 dark:text-emerald-400"
          />
          <HeroStat label="Graded pass" value={gradedPass} />
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3 border-t border-border/60 bg-muted/20 px-5 py-3">
        <p className="mb-0 flex items-center gap-2 text-xs text-muted-foreground">
          <Mail className="size-3.5 shrink-0" aria-hidden />
          {user.email}
        </p>
        <Link to="/admin/users" className="no-underline">
          <Button variant="ghost" size="sm" className="gap-1.5 text-muted-foreground">
            <ArrowLeft className="size-3.5" aria-hidden />
            Users directory
          </Button>
        </Link>
      </div>
    </section>
  );
}
