import type { LucideIcon } from "lucide-react";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { cn } from "@/lib/utils";

export type AdminStatTone = "neutral" | "success" | "active" | "warning" | "danger";

const toneStyles: Record<
  AdminStatTone,
  { card: string; icon: string }
> = {
  neutral: {
    card: "border-border/80 bg-card/60",
    icon: "bg-muted text-muted-foreground",
  },
  success: {
    card: "border-emerald-500/25 bg-emerald-500/[0.07]",
    icon: "bg-emerald-500/15 text-emerald-600 dark:text-emerald-400",
  },
  active: {
    card: "border-sky-500/25 bg-sky-500/[0.07]",
    icon: "bg-sky-500/15 text-sky-600 dark:text-sky-400",
  },
  warning: {
    card: "border-amber-500/25 bg-amber-500/[0.07]",
    icon: "bg-amber-500/15 text-amber-700 dark:text-amber-400",
  },
  danger: {
    card: "border-rose-500/25 bg-rose-500/[0.07]",
    icon: "bg-rose-500/15 text-rose-600 dark:text-rose-400",
  },
};

type Props = {
  label: string;
  value: number | string;
  hint?: string;
  icon: LucideIcon;
  tone?: AdminStatTone;
  to?: string;
  className?: string;
};

export default function AdminStatCard({
  label,
  value,
  hint,
  icon: Icon,
  tone = "neutral",
  to,
  className,
}: Props) {
  const styles = toneStyles[tone];

  const body = (
    <>
      <div className="mb-3 flex items-start justify-between gap-2">
        <span
          className={cn(
            "inline-flex size-9 shrink-0 items-center justify-center rounded-lg",
            styles.icon,
          )}
        >
          <Icon className="size-4" aria-hidden />
        </span>
      </div>
      <p className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
        {label}
      </p>
      <p className="mt-1 text-2xl font-semibold tabular-nums leading-none tracking-tight text-foreground md:text-3xl">
        {value}
      </p>
      {hint && <p className="mt-2 text-xs leading-relaxed text-muted-foreground">{hint}</p>}
    </>
  );

  const cardClass = cn(
    "rounded-xl border p-4 shadow-sm transition-colors",
    styles.card,
    to && "hover:border-emerald-500/35 hover:bg-emerald-500/[0.04]",
    className,
  );

  if (to) {
    return (
      <Link to={to} className={cn(cardClass, "block no-underline")}>
        {body}
      </Link>
    );
  }

  return <div className={cardClass}>{body}</div>;
}

export function AdminMetricsStrip({
  items,
}: {
  items: { label: string; value: number | string; tone?: AdminStatTone }[];
}) {
  return (
    <div className="flex flex-wrap divide-x divide-border overflow-hidden rounded-xl border border-border/80 bg-card/50 shadow-sm">
      {items.map((item) => (
        <div key={item.label} className="min-w-[7rem] flex-1 px-4 py-3">
          <p className="text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
            {item.label}
          </p>
          <p
            className={cn(
              "mt-1 text-lg font-semibold tabular-nums text-foreground",
              item.tone === "success" && "text-emerald-600 dark:text-emerald-400",
              item.tone === "active" && "text-sky-600 dark:text-sky-400",
              item.tone === "danger" && "text-rose-600 dark:text-rose-400",
              item.tone === "warning" && "text-amber-700 dark:text-amber-400",
            )}
          >
            {item.value}
          </p>
        </div>
      ))}
    </div>
  );
}

export function AdminFilterPanel({
  children,
  footer,
  className,
}: {
  children: ReactNode;
  footer?: ReactNode;
  className?: string;
}) {
  return (
    <div
      className={cn(
        "rounded-xl border border-border/80 bg-muted/25 p-4 shadow-inner",
        className,
      )}
    >
      {children}
      {footer && (
        <div className="mt-3 border-t border-border/60 pt-3 text-xs text-muted-foreground">
          {footer}
        </div>
      )}
    </div>
  );
}

export function AdminFilterGroup({
  label,
  children,
}: {
  label: string;
  children: ReactNode;
}) {
  return (
    <div className="space-y-1.5">
      <p className="text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
        {label}
      </p>
      {children}
    </div>
  );
}
