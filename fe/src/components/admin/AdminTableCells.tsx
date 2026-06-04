import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

export function CenteredTableCell({
  children,
  className,
}: {
  children: ReactNode;
  className?: string;
}) {
  return <div className={cn("flex justify-center", className)}>{children}</div>;
}

function initialsFor(name: string, email: string): string {
  const trimmed = name.trim();
  if (trimmed) {
    const parts = trimmed.split(/\s+/).filter(Boolean);
    if (parts.length >= 2) {
      return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
    return trimmed.slice(0, 2).toUpperCase();
  }
  return email.slice(0, 2).toUpperCase();
}

export default function UserIdentityCell({
  fullName,
  email,
  size = "md",
}: {
  fullName: string | null | undefined;
  email: string;
  size?: "sm" | "md";
}) {
  const displayName = fullName?.trim() || email;
  const initials = initialsFor(fullName ?? "", email);
  const isCompact = size === "sm";
  const showEmailLine = Boolean(fullName?.trim());

  return (
    <div className="flex min-w-0 items-center gap-3">
      <span
        className={cn(
          "inline-flex shrink-0 items-center justify-center rounded-full bg-emerald-600 font-semibold text-white dark:bg-emerald-500/30 dark:text-emerald-100",
          isCompact ? "size-8 text-[10px]" : "size-9 text-[11px]",
        )}
        aria-hidden
      >
        {initials}
      </span>
      <div className="min-w-0">
        <p className={cn("truncate font-medium text-foreground", isCompact ? "text-sm" : "text-sm")}>
          {displayName}
        </p>
        {showEmailLine && (
          <p className="truncate text-xs text-muted-foreground">{email}</p>
        )}
      </div>
    </div>
  );
}

export function PassRateCell({ percent }: { percent: number | null }) {
  if (percent == null) {
    return <span className="text-sm text-muted-foreground">—</span>;
  }

  const tone =
    percent >= 80
      ? "bg-emerald-500"
      : percent >= 50
        ? "bg-amber-500"
        : "bg-rose-500";

  return (
    <div className="min-w-[4.5rem]">
      <div className="mb-1 flex items-center justify-between gap-2">
        <span className="text-sm font-medium tabular-nums text-foreground">{percent}%</span>
      </div>
      <div className="h-1.5 overflow-hidden rounded-full bg-muted">
        <div
          className={cn("h-full rounded-full transition-all", tone)}
          style={{ width: `${Math.min(Math.max(percent, 0), 100)}%` }}
        />
      </div>
    </div>
  );
}

export function CatalogProgressCell({
  passed,
  total,
  percent,
}: {
  passed: number;
  total: number;
  percent: number;
}) {
  return (
    <div className="min-w-[8rem]">
      <div className="mb-1.5 flex items-center justify-between text-xs">
        <span className="tabular-nums text-muted-foreground">
          {passed}/{total} of started
        </span>
        <span className="font-medium tabular-nums text-foreground">{percent}%</span>
      </div>
      <div className="h-2 overflow-hidden rounded-full bg-muted">
        <div
          className="h-full rounded-full bg-emerald-500 transition-all"
          style={{ width: `${Math.min(Math.max(percent, 0), 100)}%` }}
        />
      </div>
    </div>
  );
}
