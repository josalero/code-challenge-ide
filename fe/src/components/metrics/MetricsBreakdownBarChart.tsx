import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { MetricsBreakdownRow } from "@/api/types";
import { useChartPalette } from "./chartTheme";
import MetricsChartCard from "./MetricsChartCard";
import MetricsChartTooltip from "./MetricsChartTooltip";

type Props = {
  title: string;
  description?: string;
  rows: MetricsBreakdownRow[];
  formatLabel?: (label: string) => string;
};

export default function MetricsBreakdownBarChart({
  title,
  description,
  rows,
  formatLabel,
}: Props) {
  const palette = useChartPalette();
  const labelFor = formatLabel ?? ((l: string) => l);

  const data = rows.map((row) => ({
    name: labelFor(row.label),
    Passed: row.passed,
    "In progress": row.inProgress,
    "Not started": row.notStarted,
  }));

  if (data.length === 0) {
    return null;
  }

  const chartHeight = Math.max(220, data.length * 44 + 48);

  return (
    <MetricsChartCard title={title} description={description} minHeight={chartHeight}>
      <ResponsiveContainer width="100%" height={chartHeight - 40}>
        <BarChart
          layout="vertical"
          data={data}
          margin={{ top: 4, right: 12, left: 4, bottom: 4 }}
          barCategoryGap="18%"
        >
          <CartesianGrid strokeDasharray="3 3" stroke={palette.grid} horizontal={false} />
          <XAxis
            type="number"
            allowDecimals={false}
            tick={{ fill: palette.axis, fontSize: 11 }}
            axisLine={{ stroke: palette.grid }}
            tickLine={false}
          />
          <YAxis
            type="category"
            dataKey="name"
            width={100}
            tick={{ fill: palette.axis, fontSize: 11 }}
            axisLine={false}
            tickLine={false}
          />
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
            verticalAlign="top"
            align="right"
            iconType="circle"
            iconSize={8}
            wrapperStyle={{ fontSize: 11, paddingBottom: 8 }}
            formatter={(value) => (
              <span className="text-xs text-muted-foreground">{value}</span>
            )}
          />
          <Bar dataKey="Passed" stackId="a" fill={palette.passed} radius={[0, 0, 0, 0]} />
          <Bar dataKey="In progress" stackId="a" fill={palette.inProgress} />
          <Bar
            dataKey="Not started"
            stackId="a"
            fill={palette.notStarted}
            radius={[0, 4, 4, 0]}
          />
        </BarChart>
      </ResponsiveContainer>
    </MetricsChartCard>
  );
}
