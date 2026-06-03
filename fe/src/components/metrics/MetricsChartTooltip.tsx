import type { ChartPalette } from "./chartTheme";

type TooltipEntry = {
  name?: unknown;
  value?: unknown;
  color?: string;
  dataKey?: unknown;
};

type Props = {
  active?: boolean;
  payload?: TooltipEntry[];
  label?: unknown;
  palette: ChartPalette;
};

function formatValue(value: unknown): string | number {
  if (Array.isArray(value)) {
    return value.map(String).join(", ");
  }
  if (typeof value === "number" || typeof value === "string") {
    return value;
  }
  return 0;
}

export default function MetricsChartTooltip({
  active,
  payload,
  label,
  palette,
}: Props) {
  if (!active || !payload?.length) {
    return null;
  }

  return (
    <div
      className="rounded-lg border px-3 py-2 text-xs shadow-md"
      style={{
        backgroundColor: palette.tooltipBg,
        borderColor: palette.tooltipBorder,
        color: palette.tooltipText,
      }}
    >
      {label != null && label !== "" && (
        <p className="mb-1.5 font-medium">{String(label)}</p>
      )}
      <ul className="space-y-1">
        {payload.map((entry) => (
          <li key={`${String(entry.name)}-${String(entry.dataKey)}`} className="flex items-center gap-2">
            <span
              className="size-2 shrink-0 rounded-full"
              style={{ backgroundColor: entry.color }}
              aria-hidden
            />
            <span style={{ color: palette.tooltipText }}>{String(entry.name ?? "")}</span>
            <span className="ml-auto tabular-nums font-semibold">
              {formatValue(entry.value)}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}
