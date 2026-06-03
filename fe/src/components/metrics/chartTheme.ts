import { useMemo } from "react";
import { useTheme } from "@/theme/ThemeProvider";

export type ChartPalette = {
  passed: string;
  inProgress: string;
  notStarted: string;
  failed: string;
  practice: string;
  graded: string;
  grid: string;
  axis: string;
  tooltipBg: string;
  tooltipBorder: string;
  tooltipText: string;
};

const LIGHT: ChartPalette = {
  passed: "#059669",
  inProgress: "#0284c7",
  notStarted: "#94a3b8",
  failed: "#e11d48",
  practice: "#0284c7",
  graded: "#059669",
  grid: "rgba(148, 163, 184, 0.35)",
  axis: "#64748b",
  tooltipBg: "#ffffff",
  tooltipBorder: "#e2e8f0",
  tooltipText: "#0f172a",
};

const DARK: ChartPalette = {
  passed: "#34d399",
  inProgress: "#38bdf8",
  notStarted: "#64748b",
  failed: "#fb7185",
  practice: "#38bdf8",
  graded: "#34d399",
  grid: "rgba(71, 85, 105, 0.45)",
  axis: "#94a3b8",
  tooltipBg: "#1e293b",
  tooltipBorder: "#475569",
  tooltipText: "#f1f5f9",
};

export function useChartPalette(): ChartPalette {
  const { mode } = useTheme();
  return useMemo(() => (mode === "light" ? LIGHT : DARK), [mode]);
}
