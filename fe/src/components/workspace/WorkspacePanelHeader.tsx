import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

type Props = {
  icon: ReactNode;
  title: string;
  subtitle?: string;
  trailing?: ReactNode;
  className?: string;
};

export default function WorkspacePanelHeader({
  icon,
  title,
  subtitle,
  trailing,
  className,
}: Props) {
  return (
    <div
      className={cn(
        "flex shrink-0 items-center justify-between gap-3 border-b border-slate-700/60 bg-slate-900/80 px-3 py-2",
        className,
      )}
    >
      <div className="flex min-w-0 items-center gap-2.5">
        <span className="flex size-7 shrink-0 items-center justify-center rounded-md bg-emerald-500/10 text-emerald-400 ring-1 ring-emerald-500/20">
          {icon}
        </span>
        <div className="min-w-0">
          <p className="truncate text-sm font-medium text-slate-100">{title}</p>
          {subtitle && (
            <p className="truncate text-xs text-slate-500">{subtitle}</p>
          )}
        </div>
      </div>
      {trailing && <div className="shrink-0">{trailing}</div>}
    </div>
  );
}
