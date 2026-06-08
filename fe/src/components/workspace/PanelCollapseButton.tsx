import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { PanelLeftClose, PanelLeftOpen, PanelRightClose, PanelRightOpen } from "lucide-react";
import type { RefObject } from "react";
import type { PanelImperativeHandle } from "react-resizable-panels";

type Side = "left" | "right" | "bottom";

type Props = {
  panelRef: RefObject<PanelImperativeHandle | null>;
  collapsed: boolean;
  side: Side;
  label: string;
  className?: string;
};

function icons(side: Side, collapsed: boolean) {
  if (side === "left") {
    return collapsed ? PanelLeftOpen : PanelLeftClose;
  }
  if (side === "right") {
    return collapsed ? PanelRightOpen : PanelRightClose;
  }
  return collapsed ? PanelLeftOpen : PanelLeftClose;
}

export default function PanelCollapseButton({
  panelRef,
  collapsed,
  side,
  label,
  className,
}: Props) {
  const Icon = icons(side, collapsed);
  const action = collapsed ? `Show ${label}` : `Hide ${label}`;

  return (
    <Button
      type="button"
      variant="ghost"
      size="icon"
      className={cn(
        "size-9 shrink-0 text-slate-400 hover:text-slate-100 [@media(pointer:coarse)]:size-10",
        className,
      )}
      aria-label={action}
      title={action}
      onClick={() => {
        const panel = panelRef.current;
        if (!panel) {
          return;
        }
        if (collapsed) {
          panel.expand();
        } else {
          panel.collapse();
        }
      }}
    >
      <Icon className="size-3.5" aria-hidden />
    </Button>
  );
}
