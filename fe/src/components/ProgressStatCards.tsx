import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  CodeOutlined,
} from "@ant-design/icons";
import { Typography } from "antd";

type Stats = {
  passed: number;
  attempted: number;
  failed: number;
  total: number;
};

type Props = {
  stats: Stats;
};

const ITEMS = [
  {
    key: "passed",
    label: "Passed",
    icon: <CheckCircleOutlined />,
    color: "text-emerald-400",
    ring: "ring-emerald-500/20",
    bg: "bg-emerald-500/10",
  },
  {
    key: "attempted",
    label: "In progress",
    icon: <ClockCircleOutlined />,
    color: "text-sky-400",
    ring: "ring-sky-500/20",
    bg: "bg-sky-500/10",
  },
  {
    key: "failed",
    label: "Needs work",
    icon: <CodeOutlined />,
    color: "text-rose-400",
    ring: "ring-rose-500/20",
    bg: "bg-rose-500/10",
  },
] as const;

export default function ProgressStatCards({ stats }: Props) {
  const values: Record<string, number> = {
    passed: stats.passed,
    attempted: stats.attempted,
    failed: stats.failed,
  };

  return (
    <div className="mb-8 grid gap-4 sm:grid-cols-3">
      {ITEMS.map((item) => (
        <div
          key={item.key}
          className={`rounded-xl border border-slate-800/80 p-4 ring-1 ${item.ring} ${item.bg}`}
        >
          <div className="flex items-center gap-2 text-slate-400 text-xs uppercase tracking-wide">
            <span className={item.color}>{item.icon}</span>
            {item.label}
          </div>
          <Typography.Title
            level={3}
            className={`!mb-0 !mt-2 ${item.color}`}
          >
            {values[item.key]}
            <span className="text-slate-500 text-base font-normal">
              {" "}
              / {stats.total}
            </span>
          </Typography.Title>
        </div>
      ))}
    </div>
  );
}
