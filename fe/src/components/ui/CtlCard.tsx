import { Card, type CardProps } from "antd";
import { cn } from "../../lib/utils";

/** Consistent elevated surface for the dark training-lab theme. */
export default function CtlCard({ className = "", ...props }: CardProps) {
  return (
    <Card
      {...props}
      className={cn(
        "ctl-card overflow-hidden border-slate-800/80 bg-slate-900/90 shadow-sm shadow-black/10",
        className,
      )}
    />
  );
}
