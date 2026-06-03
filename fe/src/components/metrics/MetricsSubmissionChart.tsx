import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { useChartPalette } from "./chartTheme";
import MetricsChartCard from "./MetricsChartCard";
import MetricsChartTooltip from "./MetricsChartTooltip";

type Props = {
  practiceRuns: number;
  gradedSubmits: number;
  submissionsFailed: number;
  submissionsCompleted: number;
};

export default function MetricsSubmissionChart({
  practiceRuns,
  gradedSubmits,
  submissionsFailed,
  submissionsCompleted,
}: Props) {
  const palette = useChartPalette();

  const data = [
    { name: "Practice (Run)", count: practiceRuns, fill: palette.practice },
    { name: "Graded (Submit)", count: gradedSubmits, fill: palette.graded },
    { name: "Failed", count: submissionsFailed, fill: palette.failed },
    { name: "Succeeded", count: submissionsCompleted, fill: palette.passed },
  ];

  const total = data.reduce((sum, row) => sum + row.count, 0);

  return (
    <MetricsChartCard
      title="Submission activity"
      description="Runs and submits recorded on your account"
      minHeight={300}
    >
      {total === 0 ? (
        <p className="flex h-[260px] items-center justify-center text-sm text-muted-foreground">
          No runs yet — open a challenge and hit Run.
        </p>
      ) : (
        <ResponsiveContainer width="100%" height={280}>
          <BarChart data={data} margin={{ top: 8, right: 8, left: 0, bottom: 4 }}>
            <CartesianGrid strokeDasharray="3 3" stroke={palette.grid} vertical={false} />
            <XAxis
              dataKey="name"
              tick={{ fill: palette.axis, fontSize: 11 }}
              axisLine={{ stroke: palette.grid }}
              tickLine={false}
              interval={0}
              angle={-12}
              textAnchor="end"
              height={56}
            />
            <YAxis
              allowDecimals={false}
              tick={{ fill: palette.axis, fontSize: 11 }}
              axisLine={false}
              tickLine={false}
              width={36}
            />
            <Tooltip
              cursor={{ fill: palette.grid, opacity: 0.35 }}
              content={({ active, payload, label }) => (
                <MetricsChartTooltip
                  active={active}
                  payload={payload}
                  label={label}
                  palette={palette}
                />
              )}
            />
            <Bar dataKey="count" radius={[6, 6, 0, 0]} maxBarSize={56}>
              {data.map((entry) => (
                <Cell key={entry.name} fill={entry.fill} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      )}
    </MetricsChartCard>
  );
}
