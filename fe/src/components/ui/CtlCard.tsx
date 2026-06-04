import { Card, type CardProps } from "antd";
import { cn } from "../../lib/utils";

/** Elevated surface card — follows light/dark semantic tokens. */
export default function CtlCard({ className = "", ...props }: CardProps) {
  return (
    <Card
      {...props}
      className={cn(
        "ctl-card overflow-hidden border-border bg-card text-card-foreground shadow-sm",
        "dark:border-slate-800/80 dark:bg-slate-900/90 dark:shadow-black/10",
        className,
      )}
    />
  );
}
