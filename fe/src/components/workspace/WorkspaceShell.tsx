import { BookOpen, Code2, PanelRight } from "lucide-react";
import { useEffect, useState } from "react";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import ChallengeInstructionsPanel from "./ChallengeInstructionsPanel";
import CodeEditorPanel from "./CodeEditorPanel";
import WorkspaceHeader from "./WorkspaceHeader";
import WorkspaceActivityPanel from "./WorkspaceActivityPanel";
import WorkspaceBottomPanel, {
  type BottomPanelTab,
} from "./WorkspaceBottomPanel";
import WorkspacePanelFrame from "./WorkspacePanelFrame";
import WorkspaceResizableLayout from "./WorkspaceResizableLayout";
import RunStateBanner from "./RunStateBanner";
import type { ChallengeDetail, ReportResponse, RunnerLogs } from "@/api/types";
import type { WorkspaceRunPhase } from "@/domain/workspaceRunState";
import type { SubmissionStatusValue } from "@/domain/constants";
import type { AutosaveStatus } from "@/hooks/useAutosaveDraft";
import type { ActivityEntry, TrackedTest } from "@/domain/runProgressTypes";
import type { AttemptRecord } from "./AttemptHistoryTab";
import { useMediaQuery } from "@/hooks/useMediaQuery";
import { cn } from "@/lib/utils";

type Props = {
  slug: string;
  challenge: ChallengeDetail;
  runtimeVersion: string;
  onRuntimeChange: (version: string) => void;
  solutionCode: string;
  customTestsCode: string;
  onSolutionChange: (code: string) => void;
  onCustomTestsChange: (code: string) => void;
  workspaceTab: "solution" | "custom";
  onWorkspaceTabChange: (tab: "solution" | "custom") => void;
  isRunning: boolean;
  runPhase: WorkspaceRunPhase;
  autosaveStatus: AutosaveStatus;
  onRunTests: () => void;
  onSubmit: () => void;
  exerciseLocked?: boolean;
  onRedo?: () => void;
  redoLoading?: boolean;
  onCancel?: () => void;
  cancelLoading?: boolean;
  showCancel: boolean;
  onResetStarter: () => void;
  onSaveCustomTests?: () => void;
  saveCustomTestsLoading?: boolean;
  bottomTab: BottomPanelTab;
  onBottomTabChange: (tab: BottomPanelTab) => void;
  submissionStatus: SubmissionStatusValue | null;
  isSubmitting: boolean;
  streamConnected: boolean;
  streamReconnecting: boolean;
  activityLog: ActivityEntry[];
  trackedTests: TrackedTest[];
  runStartedAt: number | null;
  runnerLogs: RunnerLogs | null;
  report: ReportResponse | null;
  reportLoading: boolean;
  onReportUpdate: (report: ReportResponse) => void;
  onScrollToCoach: () => void;
  attempts: AttemptRecord[];
  showLiveRun: boolean;
  isTerminal: boolean;
  submitError: string | null;
  loading?: boolean;
};

type MobilePane = "instructions" | "editor" | "output";

const MOBILE_PANES: { id: MobilePane; label: string; icon: typeof BookOpen }[] = [
  { id: "instructions", label: "Problem", icon: BookOpen },
  { id: "editor", label: "Editor", icon: Code2 },
  { id: "output", label: "Output", icon: PanelRight },
];

function WorkspaceSkeleton() {
  return (
    <div className="flex h-full w-full flex-col" role="status" aria-label="Loading workspace">
      <Skeleton className="h-14 shrink-0 rounded-none bg-slate-800/80" />
      <div className="grid min-h-0 flex-1 grid-cols-[minmax(220px,1fr)_minmax(0,2fr)_minmax(200px,1fr)] gap-px bg-slate-800/40">
        <Skeleton className="h-full rounded-none bg-slate-800/50" />
        <Skeleton className="h-full rounded-none bg-slate-800/50" />
        <Skeleton className="h-full rounded-none bg-slate-800/50" />
      </div>
    </div>
  );
}

function paneClass(pane: MobilePane, active: MobilePane): string {
  return cn(
    "flex min-h-0 min-w-0 flex-col overflow-hidden",
    pane !== active && "max-md:hidden",
  );
}

export default function WorkspaceShell({
  slug,
  challenge,
  runtimeVersion,
  onRuntimeChange,
  solutionCode,
  customTestsCode,
  onSolutionChange,
  onCustomTestsChange,
  workspaceTab,
  onWorkspaceTabChange,
  isRunning,
  runPhase,
  autosaveStatus,
  onRunTests,
  onSubmit,
  exerciseLocked = false,
  onRedo,
  redoLoading,
  onCancel,
  cancelLoading,
  showCancel,
  onResetStarter,
  onSaveCustomTests,
  saveCustomTestsLoading,
  bottomTab,
  onBottomTabChange,
  submissionStatus,
  isSubmitting,
  streamConnected,
  streamReconnecting,
  activityLog,
  trackedTests,
  runStartedAt,
  runnerLogs,
  report,
  reportLoading,
  onReportUpdate,
  onScrollToCoach,
  attempts,
  showLiveRun,
  isTerminal,
  submitError,
  loading,
}: Props) {
  const [mobilePane, setMobilePane] = useState<MobilePane>("editor");
  const [instructionsOpen, setInstructionsOpen] = useState(false);
  const isDesktopWorkspace = useMediaQuery("(min-width: 768px)");

  useEffect(() => {
    if (showLiveRun) {
      setMobilePane("output");
    }
  }, [showLiveRun]);

  const header = (
    <WorkspaceHeader
      challenge={challenge}
      runtimeVersion={runtimeVersion}
      onRuntimeChange={onRuntimeChange}
      isRunning={isRunning}
      runPhase={runPhase}
      autosaveStatus={autosaveStatus}
      onRunTests={onRunTests}
      onSubmit={onSubmit}
      exerciseLocked={exerciseLocked}
      onRedo={onRedo}
      redoLoading={redoLoading}
      onCancel={onCancel}
      cancelLoading={cancelLoading}
      showCancel={showCancel}
      onResetStarter={onResetStarter}
      onSaveCustomTests={onSaveCustomTests}
      saveCustomTestsLoading={saveCustomTestsLoading}
      activeTab={workspaceTab}
    />
  );

  const editorPanel = (
    <CodeEditorPanel
      slug={slug}
      language={challenge.language}
      solutionCode={solutionCode}
      customTestsCode={customTestsCode}
      onSolutionChange={onSolutionChange}
      onCustomTestsChange={onCustomTestsChange}
      workspaceTab={workspaceTab}
      onWorkspaceTabChange={onWorkspaceTabChange}
      readOnly={isRunning || exerciseLocked}
    />
  );

  const instructionsPanel = (
    <ChallengeInstructionsPanel challenge={challenge} runtimeVersion={runtimeVersion} />
  );

  const outputPanel = (
    <WorkspaceBottomPanel
      variant="sidebar"
      activeTab={bottomTab}
      onTabChange={onBottomTabChange}
      runPhase={runPhase}
      challengeSlug={slug}
      submissionStatus={submissionStatus}
      isSubmitting={isSubmitting}
      streamConnected={streamConnected}
      streamReconnecting={streamReconnecting}
      trackedTests={trackedTests}
      hiddenTestCount={challenge.hiddenTestCount}
      runtimeVersion={runtimeVersion}
      challengeLanguage={challenge.language}
      runStartedAt={runStartedAt}
      runnerLogs={runnerLogs}
      report={report}
      reportLoading={reportLoading}
      onReportUpdate={onReportUpdate}
      onScrollToCoach={onScrollToCoach}
      attempts={attempts}
      showLiveRun={showLiveRun}
      isTerminal={isTerminal}
      exerciseLocked={exerciseLocked}
    />
  );

  if (loading) {
    return (
      <div className="ctl-workspace-shell flex h-full min-h-0 flex-1 flex-col overflow-hidden">
        <WorkspaceSkeleton />
      </div>
    );
  }

  return (
    <div className="ctl-workspace-shell flex h-full min-h-0 flex-1 flex-col overflow-hidden">
      {header}

      {(submitError
        || (runPhase !== "idle" && runPhase !== "loading" && runPhase !== "running")) && (
        <div className="shrink-0 border-b border-slate-800/80 px-4 py-2">
          <RunStateBanner phase={runPhase} message={submitError} />
        </div>
      )}

      <div
        className="flex shrink-0 items-center gap-2 border-b border-slate-800/80 px-3 py-2 md:hidden"
        role="tablist"
        aria-label="Workspace panels"
      >
        {MOBILE_PANES.map(({ id, label, icon: Icon }) => (
          <Button
            key={id}
            type="button"
            variant={mobilePane === id ? "secondary" : "outline"}
            size="sm"
            className={cn(
              "h-8 gap-1.5 text-xs",
              mobilePane === id
                ? "border-slate-600/60 bg-slate-800 text-slate-100"
                : "border-slate-700/60 bg-transparent text-slate-400",
            )}
            role="tab"
            aria-selected={mobilePane === id}
            onClick={() => setMobilePane(id)}
          >
            <Icon className="size-3.5" aria-hidden />
            {label}
          </Button>
        ))}
        <Sheet open={instructionsOpen} onOpenChange={setInstructionsOpen}>
          <SheetTrigger
            render={
              <Button
                variant="outline"
                size="sm"
                className="ml-auto h-8 border-slate-600/60 bg-slate-800/40 text-xs"
              />
            }
          >
            <BookOpen className="size-3.5" aria-hidden />
            Expand
          </SheetTrigger>
          <SheetContent side="left" className="w-[min(100vw,420px)] border-slate-700/60 p-0">
            <SheetHeader className="sr-only">
              <SheetTitle>{challenge.title}</SheetTitle>
            </SheetHeader>
            {instructionsPanel}
          </SheetContent>
        </Sheet>
      </div>

      {!isDesktopWorkspace && (
      <div className="ctl-workspace-ide-grid min-h-0 flex-1 overflow-hidden">
        <div
          className={cn(
            "ctl-workspace-ide-column border-slate-700/70 bg-slate-900/30",
            paneClass("instructions", mobilePane),
          )}
        >
          <WorkspacePanelFrame>{instructionsPanel}</WorkspacePanelFrame>
        </div>

        <div className={cn("ctl-workspace-ide-column bg-[#1e1e1e]", paneClass("editor", mobilePane))}>
          <WorkspacePanelFrame>
            <div className="flex h-full min-h-0 w-full flex-col overflow-hidden">
              <div className="min-h-0 flex-1 overflow-hidden">{editorPanel}</div>
              <WorkspaceActivityPanel
                activityLog={activityLog}
                isActive={isRunning || showLiveRun}
                streamConnected={streamConnected}
                streamReconnecting={streamReconnecting}
              />
            </div>
          </WorkspacePanelFrame>
        </div>

        <div
          className={cn(
            "ctl-workspace-ide-column border-slate-700/70 bg-slate-900/40",
            paneClass("output", mobilePane),
          )}
        >
          <WorkspacePanelFrame>{outputPanel}</WorkspacePanelFrame>
        </div>
      </div>
      )}

      {isDesktopWorkspace && (
      <div className="min-h-0 flex-1 overflow-hidden">
        <WorkspaceResizableLayout
          problem={instructionsPanel}
          editor={editorPanel}
          activity={
            <WorkspaceActivityPanel
              activityLog={activityLog}
              isActive={isRunning || showLiveRun}
              streamConnected={streamConnected}
              streamReconnecting={streamReconnecting}
              layout="resizable"
            />
          }
          output={outputPanel}
        />
      </div>
      )}
    </div>
  );
}
