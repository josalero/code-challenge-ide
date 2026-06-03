import {
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from "recharts";
import { useChartPalette } from "./chartTheme";
import MetricsChartCard from "./MetricsChartCard";
import MetricsChartTooltip from "./MetricsChartTooltip";

type Props = {
  passed: number;
  inProgress: number;
  notStarted: number;
  catalogTotal: number;
};

export default function MetricsProgressDonut({
  passed,
  inProgress,
  notStarted,
  catalogTotal,
}: Props) {
  const palette = useChartPalette();

  const segments = [
    { name: "Passed", value: passed, color: palette.passed },
    { name: "In progress", value: inProgress, color: palette.inProgress },
    { name: "Not started", value: notStarted, color: palette.notStarted },
  ].filter((s) => s.value > 0);

  const empty = catalogTotal === 0 || segments.length === 0;

  return (
    <MetricsChartCard
      title="Progress mix"
      description="How challenges in the catalog are distributed by status"
      minHeight={300}
    >
      {empty ? (
        <p className="flex h-[260px] items-center justify-center text-sm text-muted-foreground">
          No catalog data yet.
        </p>
      ) : (
        <ResponsiveContainer width="100%" height={280}>
          <PieChart>
            <Pie
              data={segments}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="46%"
              innerRadius={62}
              outerRadius={96}
              paddingAngle={2}
              stroke="transparent"
            >
              {segments.map((entry) => (
                <Cell key={entry.name} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip
              content={({ active, payload, label }) => (
                <MetricsChartTooltip
                  active={active}
                  payload={payload}
                  label={label}
                  palette={palette}
                />
              )}
            />
            <Legend
              verticalAlign="bottom"
              iconType="circle"
              iconSize={8}
              formatter={(value) => (
                <span className="text-xs text-muted-foreground">{value}</span>
              )}
            />
          </PieChart>
        </ResponsiveContainer>
      )}
    </MetricsChartCard>
  );
}
