import { useMemo, type ReactNode } from "react";
import {
  AlertCircle,
  BookOpen,
  Bot,
  Clock3,
  FlaskConical,
  History,
  ListChecks,
  ShieldCheck,
  Terminal,
} from "lucide-react";
import ChallengeSessionGuidePanel from "./ChallengeSessionGuidePanel";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import type { ReportResponse, RunnerLogs } from "@/api/types";
import type { SubmissionStatusValue } from "@/domain/constants";
import type { TrackedTest } from "@/domain/runProgressTypes";
import RunProgressPanel from "../RunProgressPanel";
import RunnerLogOutput from "./RunnerLogOutput";
import AiCoachPanel from "../AiCoachPanel";
import FeedbackActionsPanel from "../FeedbackActionsPanel";
import RunResultBanner from "../RunResultBanner";
import AttemptHistoryTab, { type AttemptRecord } from "./AttemptHistoryTab";
import WorkspacePanelHeader from "./WorkspacePanelHeader";
import { FeedbackCategory, FeedbackStatus } from "@/domain/constants";
import { cn } from "@/lib/utils";

export type BottomPanelTab =
  | "guide"
  | "tests"
  | "compiler"
  | "static-analysis"
  | "feedback"
  | "history";

type PanelVariant = "dock" | "sidebar";

type Props = {
  variant?: PanelVariant;
  activeTab: BottomPanelTab;
  onTabChange: (tab: BottomPanelTab) => void;
  challengeSlug: string;
  submissionStatus: SubmissionStatusValue | null;
  isSubmitting: boolean;
  streamConnected: boolean;
  streamReconnecting: boolean;
  trackedTests: TrackedTest[];
  hiddenTestCount: number;
  runtimeVersion: string;
  challengeLanguage: string;
  runStartedAt: number | null;
  runnerLogs: RunnerLogs | null;
  report: ReportResponse | null;
  reportLoading: boolean;
  onReportUpdate: (report: ReportResponse) => void;
  onScrollToCoach: () => void;
  attempts: AttemptRecord[];
  showLiveRun: boolean;
  isTerminal: boolean;
  submitError?: string | null;
  exerciseLocked?: boolean;
  sessionDurationMinutes?: number;
  sessionActive?: boolean;
  sessionExpired?: boolean;
  sessionCountdown?: string | null;
  showStartTest?: boolean;
  onStartTest?: () => void;
  showAbandonAttempt?: boolean;
  onAbandonAttempt?: () => void;
};

const OUTPUT_TAB_ITEMS = [
  { value: "tests", label: "Tests", icon: ListChecks },
  { value: "compiler", label: "Compiler", icon: Terminal },
  { value: "static-analysis", label: "Analysis", icon: ShieldCheck },
  { value: "feedback", label: "Feedback", icon: Bot },
  { value: "history", label: "History", icon: History },
] as const;

function EmptyHint({
  icon,
  title,
  children,
  compact,
}: {
  icon: ReactNode;
  title: string;
  children: ReactNode;
  compact?: boolean;
}) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center px-4 text-center",
        compact ? "py-6" : "py-10",
      )}
    >
      <span className="mb-3 flex size-10 items-center justify-center rounded-full bg-slate-800/80 text-slate-500 ring-1 ring-slate-700/60">
        {icon}
      </span>
      <p className="text-sm font-medium text-slate-300">{title}</p>
      <p className="mt-1 max-w-sm text-sm text-slate-500">{children}</p>
    </div>
  );
}

function PanelScroll({
  sidebar,
  children,
}: {
  sidebar: boolean;
  children: ReactNode;
}) {
  return (
    <ScrollArea className={cn("min-h-0", sidebar ? "h-full flex-1" : "h-full max-h-[300px]")}>
      {children}
    </ScrollArea>
  );
}

function ExecutionErrorNotice({
  message,
  compact,
}: {
  message: string | null | undefined;
  compact: boolean;
}) {
  if (!message) {
    return null;
  }

  return (
    <div
      className={cn(
        "flex items-start gap-2 rounded-md border border-red-500/30 bg-red-500/10 text-red-100",
        compact ? "px-2.5 py-2 text-xs" : "px-3 py-2 text-sm",
      )}
      role="alert"
    >
      <AlertCircle className="mt-0.5 size-4 shrink-0" aria-hidden />
      <div className="min-w-0">
        <p className="font-medium">Execution issue</p>
        <p className="mt-0.5 whitespace-pre-wrap text-red-100/85">{message}</p>
      </div>
    </div>
  );
}

export default function WorkspaceBottomPanel({
  variant = "dock",
  activeTab,
  onTabChange,
  challengeSlug,
  submissionStatus,
  isSubmitting,
  streamConnected,
  streamReconnecting,
  trackedTests,
  hiddenTestCount,
  runtimeVersion,
  challengeLanguage,
  runStartedAt,
  runnerLogs,
  report,
  reportLoading,
  onReportUpdate,
  onScrollToCoach,
  attempts,
  showLiveRun,
  isTerminal,
  submitError = null,
  exerciseLocked = false,
  sessionDurationMinutes = 0,
  sessionActive = false,
  sessionExpired = false,
  sessionCountdown = null,
  showStartTest = false,
  onStartTest,
  showAbandonAttempt = false,
  onAbandonAttempt,
}: Props) {
  const sidebar = variant === "sidebar";
  const showGuideTab = sessionDurationMinutes > 0;

  const tabItems = useMemo(
    () =>
      showGuideTab
        ? [{ value: "guide" as const, label: "Guide", icon: BookOpen }, ...OUTPUT_TAB_ITEMS]
        : OUTPUT_TAB_ITEMS,
    [showGuideTab],
  );

  const staticAnalysisItems =
    report?.feedback.filter(
      (f) =>
        f.category === FeedbackCategory.STYLE
        || f.category === FeedbackCategory.COVERAGE,
    ) ?? [];

  const failedTests = trackedTests.filter((t) => t.status === "fail").length;
  const staticIssues = staticAnalysisItems.filter(
    (i) => i.status !== FeedbackStatus.pass,
  ).length;

  const tabBadge = useMemo(
    () => ({
      tests: failedTests > 0 ? failedTests : submitError ? "!" : undefined,
      compiler: runnerLogs?.stderrTruncated || runnerLogs?.stdoutTruncated ? "!" : undefined,
      "static-analysis": staticIssues > 0 ? staticIssues : undefined,
      feedback: report ? (report.blocked ? "!" : "✓") : undefined,
      history: attempts.length > 0 ? attempts.length : undefined,
    }),
    [failedTests, submitError, runnerLogs, staticIssues, report, attempts.length],
  );

  const activeLabel = tabItems.find((t) => t.value === activeTab)?.label ?? "Output";

  const tabTriggers = tabItems.map(({ value, label, icon: Icon }) => (
    <TabsTrigger
      key={value}
      value={value}
      className={cn(
        "gap-1.5 text-xs data-[state=active]:bg-slate-800 data-[state=active]:text-slate-100",
        sidebar
          ? "relative h-10 w-full justify-center rounded-none px-0 py-2 data-[state=active]:bg-slate-800/90"
          : "rounded-md px-2.5 py-1.5",
      )}
      title={sidebar ? label : undefined}
    >
      <Icon className="size-4 shrink-0 opacity-80" aria-hidden />
      {!sidebar && <span className="hidden sm:inline">{label}</span>}
      {value !== "guide" && tabBadge[value] !== undefined && (
        <Badge
          variant="outline"
          className={cn(
            "font-normal border-slate-600/60",
            sidebar
              ? "absolute right-1 top-1 size-4 min-w-0 p-0 text-[9px]"
              : "h-4 min-w-4 px-1 text-[10px]",
          )}
        >
          {tabBadge[value]}
        </Badge>
      )}
      {sidebar && <span className="sr-only">{label}</span>}
    </TabsTrigger>
  ));

  const panelBody = (
    <div
      className={cn(
        "min-h-0 flex-1 overflow-hidden",
        sidebar ? "flex flex-col px-2 pb-2 pt-1" : "px-3 pb-3 pt-2",
      )}
    >
      {showGuideTab && (
        <TabsContent
          value="guide"
          className="mt-0 h-full min-h-0 data-[state=inactive]:hidden"
        >
          <PanelScroll sidebar={sidebar}>
            <ChallengeSessionGuidePanel
              sessionDurationMinutes={sessionDurationMinutes}
              sessionActive={sessionActive}
              sessionExpired={sessionExpired}
              sessionCountdown={sessionCountdown}
              showStartTest={showStartTest}
              onStartTest={onStartTest}
              showAbandonAttempt={showAbandonAttempt}
              onAbandonAttempt={onAbandonAttempt}
            />
          </PanelScroll>
        </TabsContent>
      )}

      <TabsContent
        value="tests"
        className="mt-0 flex h-full min-h-0 flex-col gap-3 data-[state=inactive]:hidden"
      >
        <ExecutionErrorNotice message={submitError} compact={sidebar} />
        <div className="min-h-0 flex-1">
          {showLiveRun || trackedTests.length > 0 ? (
            <RunProgressPanel
              submissionStatus={submissionStatus}
              isSubmitting={isSubmitting}
              streamConnected={streamConnected}
              streamReconnecting={streamReconnecting}
              trackedTests={trackedTests}
              hiddenTestCount={hiddenTestCount}
              runtimeVersion={runtimeVersion}
              challengeLanguage={challengeLanguage}
              runStartedAt={runStartedAt}
            />
          ) : (
            <EmptyHint
              compact={sidebar}
              icon={<FlaskConical className="size-5" aria-hidden />}
              title="Ready to run"
            >
              Press <strong className="text-emerald-400">Run</strong> or{" "}
              <strong className="text-emerald-400">Submit</strong> to execute your solution.
            </EmptyHint>
          )}
        </div>
      </TabsContent>

      <TabsContent
        value="compiler"
        className="mt-0 flex h-full min-h-0 flex-col gap-3 data-[state=inactive]:hidden"
      >
        <ExecutionErrorNotice message={submitError} compact={sidebar} />
        {runnerLogs?.stderrTruncated || runnerLogs?.stdoutTruncated ? (
          <PanelScroll sidebar={sidebar}>
            <div className="ctl-workspace-terminal p-3">
              <RunnerLogOutput logs={runnerLogs} />
            </div>
          </PanelScroll>
        ) : (
          <EmptyHint
            compact={sidebar}
            icon={<Terminal className="size-5" aria-hidden />}
            title="No output yet"
          >
            Compiler and runner logs appear here after you execute your solution.
          </EmptyHint>
        )}
      </TabsContent>

      <TabsContent
        value="static-analysis"
        className="mt-0 h-full min-h-0 data-[state=inactive]:hidden"
      >
        {reportLoading && !report && (
          <div className="space-y-2 px-1">
            <Skeleton className="h-4 w-3/4 bg-slate-800" />
            <Skeleton className="h-4 w-1/2 bg-slate-800" />
          </div>
        )}
        {staticAnalysisItems.length > 0 ? (
          <PanelScroll sidebar={sidebar}>
            <ul className="space-y-2 pr-2">
              {staticAnalysisItems.map((item) => (
                <li key={item.id} className="ctl-workspace-test-card text-sm">
                  <div className="mb-1 flex items-center gap-2">
                    <span className="font-medium text-slate-200">{item.category}</span>
                    <Badge
                      variant="outline"
                      className={
                        item.status === FeedbackStatus.fail
                          ? "border-red-500/40 text-red-400"
                          : item.status === FeedbackStatus.warn
                            ? "border-amber-500/40 text-amber-400"
                            : "border-emerald-500/40 text-emerald-400"
                      }
                    >
                      {item.status}
                    </Badge>
                  </div>
                  <p className="text-slate-400">{item.message}</p>
                </li>
              ))}
            </ul>
          </PanelScroll>
        ) : report ? (
          <EmptyHint
            compact={sidebar}
            icon={<ShieldCheck className="size-5" aria-hidden />}
            title="All clear"
          >
            No coverage or linter issues were reported for this run.
          </EmptyHint>
        ) : (
          <EmptyHint
            compact={sidebar}
            icon={<ShieldCheck className="size-5" aria-hidden />}
            title="After your run"
          >
            Coverage thresholds and linter findings show up here once evaluation completes.
          </EmptyHint>
        )}
      </TabsContent>

      <TabsContent
        value="feedback"
        className="mt-0 flex h-full min-h-0 flex-col gap-3 data-[state=inactive]:hidden"
      >
        <ExecutionErrorNotice message={submitError} compact={sidebar} />
        {reportLoading && !report && (
          <div className="flex items-center gap-3 py-4" role="status">
            <Skeleton className="size-5 rounded-full bg-slate-800" />
            <span className="text-sm text-slate-400">Loading evaluator report…</span>
          </div>
        )}
        {report && (
          <PanelScroll sidebar={sidebar}>
            <div className="space-y-4 pr-2 pb-2">
              <RunResultBanner report={report} onScrollToCoach={onScrollToCoach} />
              <AiCoachPanel
                report={report}
                challengeSlug={challengeSlug}
                onReportUpdate={onReportUpdate}
              />
              <FeedbackActionsPanel submissionId={report.submissionId} />
            </div>
          </PanelScroll>
        )}
        {!report && !reportLoading && (
          <EmptyHint
            compact={sidebar}
            icon={<Bot className="size-5" aria-hidden />}
            title="AI coach awaits"
          >
            {exerciseLocked
              ? "Review feedback and AI alternatives below, or Redo to edit your solution again."
              : isTerminal
                ? "Submit (not Run) to unlock full coach feedback and AI alternatives."
                : "Submit your solution for evaluator feedback and AI coaching."}
          </EmptyHint>
        )}
      </TabsContent>

      <TabsContent value="history" className="mt-0 h-full min-h-0 data-[state=inactive]:hidden">
        {attempts.length > 0 ? (
          <PanelScroll sidebar={sidebar}>
            <AttemptHistoryTab attempts={attempts} />
          </PanelScroll>
        ) : (
          <EmptyHint
            compact={sidebar}
            icon={<Clock3 className="size-5" aria-hidden />}
            title="No attempts yet"
          >
            Each run or submit in this session is tracked here so you can compare outcomes.
          </EmptyHint>
        )}
      </TabsContent>
    </div>
  );

  return (
    <section
      className="flex h-full min-h-0 w-full flex-1 flex-col bg-slate-900/50"
      aria-label="Run output and feedback"
      data-learner-tour="output-panel"
    >
      <WorkspacePanelHeader
        icon={<Terminal className="size-3.5" aria-hidden />}
        title={sidebar ? activeLabel : "Output"}
        subtitle={
          sidebar
            ? "Run results & coach"
            : showGuideTab
              ? "Guide · tests · feedback"
              : "Tests · compiler · feedback"
        }
        className="shrink-0 py-1.5"
      />

      <Tabs
        value={activeTab}
        onValueChange={(v) => onTabChange(v as BottomPanelTab)}
        orientation={sidebar ? "vertical" : "horizontal"}
        className={cn(
          "min-h-0 flex-1 gap-0",
          sidebar ? "flex flex-row" : "flex flex-col",
        )}
      >
        <TabsList
          className={cn(
            "shrink-0 gap-0.5 bg-slate-900/60",
            sidebar
              ? "h-full w-12 flex-col rounded-none border-r border-slate-700/60 py-1"
              : "h-auto min-h-9 w-full justify-start rounded-none border-b border-slate-700/60 px-2 py-1",
          )}
          aria-label="Output panels"
        >
          {tabTriggers}
        </TabsList>

        {panelBody}
      </Tabs>
    </section>
  );
}
