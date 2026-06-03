import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

type Props = {
  title: string;
  description?: string;
  children: ReactNode;
  className?: string;
  minHeight?: number;
};

export default function MetricsChartCard({
  title,
  description,
  children,
  className,
  minHeight = 280,
}: Props) {
  return (
    <section
      className={cn(
        "rounded-xl border border-border bg-card/80 p-4 shadow-sm backdrop-blur-sm",
        className,
      )}
      aria-labelledby={`chart-${title.replace(/\s+/g, "-").toLowerCase()}`}
    >
      <header className="mb-3">
        <h2
          id={`chart-${title.replace(/\s+/g, "-").toLowerCase()}`}
          className="text-sm font-semibold text-foreground"
        >
          {title}
        </h2>
        {description && (
          <p className="mt-0.5 text-xs text-muted-foreground">{description}</p>
        )}
      </header>
      <div style={{ minHeight }} className="w-full">
        {children}
      </div>
    </section>
  );
}
