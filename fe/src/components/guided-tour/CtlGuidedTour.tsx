import { Tour } from "antd";
import type { TourProps } from "antd";
import { cn } from "../../lib/utils";
import { useTheme } from "../../theme/useTheme";
import type { ThemeMode } from "../../theme/themeStorage";
import { guidedTourIndicators } from "./guidedTourIndicators";

const TOUR_MASK: Record<ThemeMode, string> = {
  light: "rgba(15, 23, 42, 0.42)",
  dark: "rgba(2, 6, 23, 0.62)",
};

/** Theme-aligned Ant Design Tour wrapper (card surfaces, mask, primary actions). */
export default function CtlGuidedTour({
  rootClassName,
  type = "default",
  mask,
  arrow = { pointAtCenter: true },
  gap = { radius: 8 },
  scrollIntoViewOptions = { behavior: "smooth", block: "center" },
  indicatorsRender = guidedTourIndicators,
  ...rest
}: TourProps) {
  const { mode } = useTheme();

  const resolvedMask =
    mask === false
      ? false
      : {
          color: TOUR_MASK[mode],
          ...(typeof mask === "object" && mask != null ? mask : {}),
        };

  return (
    <Tour
      {...rest}
      type={type}
      rootClassName={cn("ctl-guided-tour", rootClassName)}
      mask={resolvedMask}
      arrow={arrow}
      gap={gap}
      scrollIntoViewOptions={scrollIntoViewOptions}
      indicatorsRender={indicatorsRender}
    />
  );
}
