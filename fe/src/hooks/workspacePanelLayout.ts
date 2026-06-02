import type { Layout } from "react-resizable-panels";

/** Validates persisted panel percentages before applying them. */
export function normalizePanelLayout(
  layout: Layout | undefined,
  panelIds: readonly string[],
  fallback: Layout,
): Layout {
  if (!layout) {
    return fallback;
  }

  let sum = 0;
  const normalized: Layout = {};

  for (const id of panelIds) {
    const value = layout[id];
    if (typeof value !== "number" || !Number.isFinite(value) || value < 0) {
      return fallback;
    }
    normalized[id] = value;
    sum += value;
  }

  if (sum < 92 || sum > 108) {
    return fallback;
  }

  return normalized;
}
