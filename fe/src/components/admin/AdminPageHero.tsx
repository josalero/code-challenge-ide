import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

type Props = {
  eyebrow?: string;
  title: string;
  description?: ReactNode;
  extra?: ReactNode;
  className?: string;
};

export default function AdminPageHero({
  eyebrow = "Administration",
  title,
  description,
  extra,
  className,
}: Props) {
  return (
    <header
      className={cn(
        "mb-6 flex flex-col gap-4 rounded-xl border border-border/70 bg-card/40 p-5 shadow-sm md:p-6 lg:flex-row lg:items-start lg:justify-between",
        className,
      )}
    >
      <div className="min-w-0 space-y-2">
        <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-emerald-600 dark:text-emerald-400">
          {eyebrow}
        </p>
        <h1 className="text-2xl font-semibold leading-tight tracking-tight text-foreground md:text-3xl">
          {title}
        </h1>
        {description && (
          <div className="max-w-2xl text-sm leading-relaxed text-muted-foreground md:text-[0.9375rem]">
            {description}
          </div>
        )}
      </div>
      {extra && <div className="flex shrink-0 flex-wrap gap-2">{extra}</div>}
    </header>
  );
}

export function AdminReportUserLine({
  fullName,
  email,
  className,
}: {
  fullName: string | null | undefined;
  email: string;
  className?: string;
}) {
  const name = fullName?.trim();
  return (
    <p className={cn("text-sm md:text-[0.9375rem]", className)}>
      {name ? (
        <>
          <span className="font-semibold text-foreground">{name}</span>
          <span className="mx-2 text-border" aria-hidden>
            ·
          </span>
          <span className="text-muted-foreground">{email}</span>
        </>
      ) : (
        <span className="font-semibold text-foreground">{email}</span>
      )}
    </p>
  );
}

export function AdminInfoCallout({
  title,
  children,
  className,
}: {
  title: string;
  children: ReactNode;
  className?: string;
}) {
  return (
    <aside
      className={cn(
        "mb-6 rounded-xl border border-sky-500/20 bg-sky-500/[0.06] px-4 py-3 text-sm leading-relaxed text-muted-foreground",
        className,
      )}
    >
      <p className="mb-1 font-medium text-foreground">{title}</p>
      {children}
    </aside>
  );
}
