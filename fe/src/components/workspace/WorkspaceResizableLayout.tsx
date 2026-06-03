import type { ReactNode } from "react";
import { useMemo } from "react";
import {
  Group,
  Panel,
  Separator,
  usePanelRef,
} from "react-resizable-panels";
import PanelCollapseButton from "./PanelCollapseButton";
import WorkspacePanelFrame from "./WorkspacePanelFrame";
import { usePanelCollapsed } from "@/hooks/usePanelCollapsed";

const DEFAULT_HORIZONTAL = { problem: 24, center: 48, output: 28 };
const DEFAULT_VERTICAL = { editor: 76, activity: 24 };

const RESIZE_HIT = { fine: 12, coarse: 32 } as const;

type Props = {
  problem: ReactNode;
  editor: ReactNode;
  activity: ReactNode;
  output: ReactNode;
};

export default function WorkspaceResizableLayout({
  problem,
  editor,
  activity,
  output,
}: Props) {
  const problemRef = usePanelRef();
  const outputRef = usePanelRef();
  const activityRef = usePanelRef();

  const problemCollapsed = usePanelCollapsed();
  const outputCollapsed = usePanelCollapsed();
  const activityCollapsed = usePanelCollapsed();

  const horizontalDefault = useMemo(() => DEFAULT_HORIZONTAL, []);
  const verticalDefault = useMemo(() => DEFAULT_VERTICAL, []);

  return (
    <Group
      id="ctl-workspace-horizontal"
      orientation="horizontal"
      className="ctl-workspace-panel-group h-full min-h-0 w-full bg-slate-800/45"
      defaultLayout={horizontalDefault}
      resizeTargetMinimumSize={RESIZE_HIT}
    >
      <Panel
        id="problem"
        panelRef={problemRef}
        minSize="14"
        maxSize="42"
        collapsible
        collapsedSize="0"
        onResize={problemCollapsed.onResize}
        className="ctl-workspace-panel border-slate-600/50 bg-slate-800/25"
      >
        <WorkspacePanelFrame>
          <div className="relative flex h-full min-h-0 flex-col">
            <div className="absolute right-1 top-1 z-10">
              <PanelCollapseButton
                panelRef={problemRef}
                collapsed={problemCollapsed.collapsed}
                side="left"
                label="problem panel"
              />
            </div>
            {problem}
          </div>
        </WorkspacePanelFrame>
      </Panel>

      <Separator className="ctl-workspace-separator" />

      <Panel
        id="center"
        minSize="28"
        className="ctl-workspace-panel relative bg-[#1e1e1e]"
      >
        {problemCollapsed.collapsed && (
          <div className="pointer-events-none absolute inset-y-0 left-0 z-20 flex items-start pt-1">
            <div className="pointer-events-auto">
              <PanelCollapseButton
                panelRef={problemRef}
                collapsed
                side="left"
                label="problem panel"
              />
            </div>
          </div>
        )}
        {outputCollapsed.collapsed && (
          <div className="pointer-events-none absolute inset-y-0 right-0 z-20 flex items-start pt-1">
            <div className="pointer-events-auto">
              <PanelCollapseButton
                panelRef={outputRef}
                collapsed
                side="right"
                label="output panel"
              />
            </div>
          </div>
        )}
        <Group
          id="ctl-workspace-vertical"
          orientation="vertical"
          className="ctl-workspace-panel-group ctl-workspace-panel-group--nested h-full min-h-0 w-full"
          defaultLayout={verticalDefault}
          resizeTargetMinimumSize={RESIZE_HIT}
        >
          <Panel id="editor" minSize="35" className="ctl-workspace-panel">
            <WorkspacePanelFrame>{editor}</WorkspacePanelFrame>
          </Panel>

          <Separator className="ctl-workspace-separator" />

          <Panel
            id="activity"
            panelRef={activityRef}
            minSize="8"
            maxSize="45"
            collapsible
            collapsedSize="0"
            onResize={activityCollapsed.onResize}
            className="ctl-workspace-panel"
          >
            <WorkspacePanelFrame>
              <div className="relative flex h-full min-h-0 flex-col">
                <div className="absolute right-1 top-1 z-10">
                  <PanelCollapseButton
                    panelRef={activityRef}
                    collapsed={activityCollapsed.collapsed}
                    side="bottom"
                    label="activity panel"
                  />
                </div>
                {activity}
              </div>
            </WorkspacePanelFrame>
          </Panel>
        </Group>
      </Panel>

      <Separator className="ctl-workspace-separator" />

      <Panel
        id="output"
        panelRef={outputRef}
        minSize="16"
        maxSize="48"
        collapsible
        collapsedSize="0"
        onResize={outputCollapsed.onResize}
        className="ctl-workspace-panel border-slate-600/50 bg-slate-800/30"
      >
        <WorkspacePanelFrame>
          <div className="relative flex h-full min-h-0 flex-col">
            <div className="absolute left-1 top-1 z-10">
              <PanelCollapseButton
                panelRef={outputRef}
                collapsed={outputCollapsed.collapsed}
                side="right"
                label="output panel"
              />
            </div>
            {output}
          </div>
        </WorkspacePanelFrame>
      </Panel>
    </Group>
  );
}
