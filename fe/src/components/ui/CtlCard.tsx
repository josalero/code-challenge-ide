import { Card, type CardProps } from "antd";

/** Consistent elevated surface for the dark training-lab theme. */
export default function CtlCard({ className = "", ...props }: CardProps) {
  return (
    <Card
      {...props}
      className={`ctl-card border-slate-800/80 bg-slate-900/90 shadow-sm ${className}`.trim()}
    />
  );
}
