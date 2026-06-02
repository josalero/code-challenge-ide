import { useCallback, useState } from "react";
import type { PanelSize } from "react-resizable-panels";

/** Tracks collapsed state from react-resizable-panels resize events. */
export function usePanelCollapsed(collapsedBelowPercent = 2) {
  const [collapsed, setCollapsed] = useState(false);

  const onResize = useCallback(
    (size: PanelSize) => {
      setCollapsed(size.asPercentage <= collapsedBelowPercent);
    },
    [collapsedBelowPercent],
  );

  return { collapsed, onResize };
}
