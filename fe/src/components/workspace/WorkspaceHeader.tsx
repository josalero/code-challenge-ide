import { Dropdown, Modal, Popconfirm } from "antd";
import type { MenuProps } from "antd";
import {
  ArrowLeft,
  Clock,
  Compass,
  Ellipsis,
  FlaskConical,
  Loader2,
  Play,
  RotateCcw,
  Save,
  Send,
  Square,
  XCircle,
} from "lucide-react";
import { Link } from "react-router-dom";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import type { ChallengeDetail } from "@/api/types";
import type { AutosaveStatus } from "@/hooks/useAutosaveDraft";
import {
  RUN_PHASE_LABELS,
  type WorkspaceRunPhase,
} from "@/domain/workspaceRunState";
import {
  formatLanguageLabel,
  formatRuntimeLabel,
  sortRuntimesByVersionDesc,
} from "@/utils/languageRuntimes";
import { difficultyColorClass } from "./difficultyBadgeStyles";
import { languageBadgeClass } from "./languageBadgeStyles";
import { useMediaQuery } from "@/hooks/useMediaQuery";

const TOOLBAR_BUTTON_CLASS =
  "min-h-10 border-slate-600/60 bg-slate-800/40 lg:min-h-8";

type Props = {
  challenge: ChallengeDetail;
  runtimeVersion: string;
  onRuntimeChange: (version: string) => void;
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
  activeTab: "solution" | "custom";
  sessionActive?: boolean;
  sessionCountdown?: string | null;
  sessionExpired?: boolean;
  sessionDurationMinutes?: number;
  onAbandonAttempt?: () => void;
  showAbandonAttempt?: boolean;
  onStartTest?: () => void;
  showStartTest?: boolean;
  showGuidedTour?: boolean;
  onGuidedTour?: () => void;
};

function autosaveLabel(status: AutosaveStatus): string {
  switch (status) {
    case "pending":
      return "Saving…";
    case "saved":
      return "Saved";
    case "error":
      return "Save failed";
    default:
      return "";
  }
}

export default function WorkspaceHeader({
  challenge,
  runtimeVersion,
  onRuntimeChange,
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
  activeTab,
  sessionActive = false,
  sessionCountdown = null,
  sessionExpired = false,
  sessionDurationMinutes = 0,
  onAbandonAttempt,
  showAbandonAttempt = false,
  onStartTest,
  showStartTest = false,
  showGuidedTour = false,
  onGuidedTour,
}: Props) {
  const autosaveText = autosaveLabel(autosaveStatus);
  const canEdit = sessionActive && !isRunning && !exerciseLocked && !sessionExpired;
  const actionsDisabled = !canEdit;
  const limitMinutes =
    sessionDurationMinutes > 0 ? sessionDurationMinutes : 60;
  const showTimer = limitMinutes > 0 && !exerciseLocked;
  const compactToolbar = useMediaQuery("(max-width: 1023px)");

  const overflowMenuItems: MenuProps["items"] = [
    activeTab === "custom" && onSaveCustomTests
      ? {
          key: "save-tests",
          label: "Save custom tests",
          disabled: isRunning,
          onClick: onSaveCustomTests,
        }
      : null,
    {
      key: "reset",
      label: "Reset starter code",
      disabled: isRunning,
      onClick: onResetStarter,
    },
    exerciseLocked && onRedo
      ? {
          key: "redo",
          label: "Redo exercise",
          disabled: redoLoading || isRunning,
          onClick: onRedo,
        }
      : null,
    showCancel && onCancel
      ? {
          key: "cancel",
          label: "Cancel run",
          disabled: cancelLoading,
          onClick: onCancel,
        }
      : null,
    showAbandonAttempt && onAbandonAttempt
      ? {
          key: "abandon",
          label: "Abandon timed attempt",
          disabled: isRunning,
          danger: true,
          onClick: () => {
            Modal.confirm({
              title: "Abandon this timed attempt?",
              content:
                "Stops and resets the countdown; your code draft stays saved. Not the same as Cancel run, which only stops the current test.",
              okText: "Abandon",
              cancelText: "Keep going",
              okButtonProps: { danger: true },
              onOk: onAbandonAttempt,
            });
          },
        }
      : null,
  ].filter(Boolean) as MenuProps["items"];

  const showOverflowMenu = compactToolbar && (overflowMenuItems?.length ?? 0) > 0;

  return (
    <header
      className="shrink-0 border-b border-slate-700/60 bg-slate-900/95"
      role="toolbar"
      aria-label="Workspace actions"
    >
      <div className="flex flex-wrap items-center gap-x-3 gap-y-2 px-3 py-2 lg:px-4">
        {!sessionActive && (
          <>
            <Link
              to="/challenges"
              className="inline-flex shrink-0 items-center gap-1 text-xs text-slate-500 no-underline hover:text-emerald-400"
            >
              <ArrowLeft className="size-3.5" aria-hidden />
              <span className="hidden sm:inline">Challenges</span>
            </Link>
            {showGuidedTour && onGuidedTour && (
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={onGuidedTour}
                className="h-8 gap-1 px-2 text-xs text-slate-400 hover:text-emerald-400"
              >
                <Compass className="size-3.5" aria-hidden />
                <span className="hidden sm:inline">Tour</span>
              </Button>
            )}
            <div className="hidden h-4 w-px bg-slate-700/80 sm:block" aria-hidden />
          </>
        )}

        <div className="flex min-w-0 flex-1 flex-col gap-1 sm:flex-row sm:items-center sm:gap-2">
          <h1 className="truncate text-base font-semibold text-slate-50 lg:text-lg">
            {challenge.title}
          </h1>
          <div className="flex shrink-0 flex-wrap items-center gap-1.5">
            <Badge
              variant="outline"
              className={`inline-flex shrink-0 text-[10px] sm:text-xs ${difficultyColorClass(challenge.difficulty)}`}
            >
              {challenge.difficulty}
            </Badge>
            <Badge
              variant="outline"
              className={`inline-flex shrink-0 text-[10px] sm:text-xs ${languageBadgeClass(challenge.language)}`}
            >
              {formatLanguageLabel(challenge.language)}
            </Badge>
          </div>
        </div>

        <div className="flex w-full flex-wrap items-center gap-2 lg:ml-auto lg:w-auto">
          <Select
            value={runtimeVersion}
            onValueChange={(v) => {
              if (v) {
                onRuntimeChange(v);
              }
            }}
            disabled={actionsDisabled}
          >
            <SelectTrigger
              className="h-10 w-full min-w-0 border-slate-600/60 bg-slate-800/60 text-xs sm:h-8 sm:w-auto sm:min-w-[120px] lg:min-h-8"
              size="sm"
              aria-label={`${formatLanguageLabel(challenge.language)} runtime version`}
            >
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {sortRuntimesByVersionDesc(challenge.runtimes.filter((r) => r.active)).map((r) => (
                  <SelectItem key={r.version} value={r.version}>
                    {formatRuntimeLabel(challenge.language, r.version)}
                  </SelectItem>
                ))}
            </SelectContent>
          </Select>

          {showTimer && (
            <Tooltip>
              <TooltipTrigger
                render={
                  <Badge
                    variant="outline"
                    className={
                      sessionExpired
                        ? "border-red-500/40 bg-red-500/10 text-red-200"
                        : sessionActive
                          ? "border-amber-500/40 bg-amber-500/10 text-amber-100"
                          : "border-slate-600/50 bg-slate-800/50 text-slate-300"
                    }
                    role="timer"
                    aria-live={sessionActive ? "polite" : undefined}
                    aria-label={
                      sessionExpired
                        ? "Challenge time limit reached"
                        : sessionActive
                          ? `Time remaining: ${sessionCountdown}`
                          : `${limitMinutes} minute limit; press Start test to begin`
                    }
                  />
                }
              >
                <Clock className="size-3 shrink-0" aria-hidden />
                {sessionExpired ? (
                  "Time's up"
                ) : sessionActive && sessionCountdown ? (
                  sessionCountdown
                ) : (
                  <>
                    <span className="font-mono tabular-nums">{limitMinutes}:00</span>
                    <span className="hidden text-slate-400 sm:inline">· not started</span>
                  </>
                )}
              </TooltipTrigger>
              <TooltipContent>
                {sessionExpired
                  ? "Your allotted time for this challenge has ended."
                  : sessionActive
                    ? "Countdown is running. Run and Submit are disabled when time is up."
                    : `You have ${limitMinutes} minutes after Start test. Until then you can read the starter skeleton in the editor (read-only).`}
              </TooltipContent>
            </Tooltip>
          )}

          {showStartTest && onStartTest && (
            <Button
              size="sm"
              onClick={onStartTest}
              data-learner-tour="start-test"
              className="min-h-10 gap-1.5 bg-emerald-600 font-semibold text-white hover:bg-emerald-500 lg:min-h-8"
            >
              <Play className="size-3.5" aria-hidden />
              Start test
            </Button>
          )}

          {runPhase !== "idle" && runPhase !== "loading" && (
            <Badge
              variant="outline"
              className={
                runPhase === "running"
                  ? "animate-pulse border-sky-500/40 bg-sky-500/10 text-sky-300"
                  : runPhase === "successful-submission"
                    ? "border-emerald-500/40 bg-emerald-500/10 text-emerald-300"
                    : "border-slate-600/60 text-slate-300"
              }
            >
              {RUN_PHASE_LABELS[runPhase]}
            </Badge>
          )}

          {autosaveText && (
            <span
              className="hidden items-center gap-1 text-xs text-slate-500 md:flex"
              role="status"
              aria-live="polite"
            >
              {autosaveStatus === "pending" && (
                <Loader2 className="size-3 animate-spin" aria-hidden />
              )}
              {autosaveText}
            </span>
          )}

          {!compactToolbar && activeTab === "custom" && onSaveCustomTests && (
            <Button
              variant="outline"
              size="sm"
              disabled={isRunning}
              onClick={onSaveCustomTests}
              className={TOOLBAR_BUTTON_CLASS}
            >
              {saveCustomTestsLoading ? (
                <Loader2 className="animate-spin" aria-hidden />
              ) : (
                <Save aria-hidden />
              )}
              <span className="hidden sm:inline">Save tests</span>
            </Button>
          )}

          {!compactToolbar && (
          <Tooltip>
            <TooltipTrigger
              render={
                <Button
                  variant="outline"
                  size="sm"
                  disabled={isRunning}
                  onClick={onResetStarter}
                  className={TOOLBAR_BUTTON_CLASS}
                />
              }
            >
              <RotateCcw aria-hidden />
              <span className="sr-only">Reset</span>
            </TooltipTrigger>
            <TooltipContent>Reset solution to starter code</TooltipContent>
          </Tooltip>
          )}

          <Tooltip>
            <TooltipTrigger
              render={
                <Button
                  variant="secondary"
                  size="sm"
                  disabled={actionsDisabled}
                  onClick={onRunTests}
                  data-learner-tour="run-action"
                  className={`min-h-10 border border-slate-600/50 bg-slate-800/80 text-slate-100 lg:min-h-8 ${compactToolbar ? "flex-1 sm:flex-none" : ""}`}
                />
              }
            >
              {isRunning ? (
                <Loader2 className="animate-spin" aria-hidden />
              ) : (
                <Play aria-hidden />
              )}
              Run
            </TooltipTrigger>
            <TooltipContent>
              {sessionExpired
                ? "Time limit reached — you can no longer run tests"
                : exerciseLocked
                  ? "Exercise submitted — use Redo to practice again"
                  : !sessionActive
                    ? "Start test first to enable Run"
                    : "Practice run in Docker (⌘/Ctrl + Enter) — does not lock editing"}
            </TooltipContent>
          </Tooltip>

          <Button
            size="sm"
            disabled={actionsDisabled}
            onClick={onSubmit}
            data-learner-tour="submit-action"
            className={`min-h-10 bg-emerald-600 font-medium text-white hover:bg-emerald-500 lg:min-h-8 ${compactToolbar ? "flex-1 sm:flex-none" : ""}`}
          >
            {isRunning ? (
              <Loader2 className="animate-spin" aria-hidden />
            ) : (
              <Send aria-hidden />
            )}
            Submit
          </Button>

          {!compactToolbar && exerciseLocked && onRedo && (
            <Button
              variant="outline"
              size="sm"
              disabled={redoLoading || isRunning}
              onClick={onRedo}
              className="min-h-10 border-amber-500/40 text-amber-200 lg:min-h-8"
            >
              {redoLoading ? (
                <Loader2 className="animate-spin" aria-hidden />
              ) : (
                <RotateCcw aria-hidden />
              )}
              Redo exercise
            </Button>
          )}

          {!compactToolbar && showCancel && onCancel && (
            <Button
              variant="destructive"
              size="sm"
              disabled={cancelLoading}
              onClick={onCancel}
              className="min-h-10 lg:min-h-8"
            >
              {cancelLoading ? (
                <Loader2 className="animate-spin" aria-hidden />
              ) : (
                <Square aria-hidden />
              )}
              <span className="sr-only">Cancel run</span>
            </Button>
          )}

          {!compactToolbar && showAbandonAttempt && onAbandonAttempt && (
            <Popconfirm
              title="Abandon this timed attempt?"
              description="Stops and resets the countdown; your code draft stays saved. Not the same as Cancel run, which only stops the current test."
              onConfirm={onAbandonAttempt}
              okText="Abandon"
              cancelText="Keep going"
              okButtonProps={{ danger: true }}
              disabled={isRunning}
            >
              <Button
                variant="outline"
                size="sm"
                disabled={isRunning}
                className="min-h-10 border-slate-600/60 text-slate-300 hover:border-red-500/40 hover:text-red-200 lg:min-h-8"
              >
                <XCircle aria-hidden />
                <span className="hidden sm:inline">Abandon</span>
              </Button>
            </Popconfirm>
          )}

          {showOverflowMenu && (
            <Dropdown menu={{ items: overflowMenuItems }} trigger={["click"]} placement="bottomRight">
              <Button
                variant="outline"
                size="sm"
                className={`${TOOLBAR_BUTTON_CLASS} px-2.5`}
                aria-label="More workspace actions"
              >
                <Ellipsis className="size-4" aria-hidden />
              </Button>
            </Dropdown>
          )}
        </div>
      </div>

      <div className="hidden items-center gap-2 border-t border-slate-800/60 px-4 py-1 text-[11px] text-slate-500 lg:flex">
        <FlaskConical className="size-3 text-emerald-500/80" aria-hidden />
        <span>
          {challenge.publicTests.length} public · {challenge.hiddenTestCount} hidden tests
        </span>
        <span className="text-slate-700" aria-hidden>
          ·
        </span>
        <kbd className="rounded border border-slate-700 bg-slate-900 px-1 font-mono text-slate-400">
          ⌘/Ctrl+Enter
        </kbd>
        <span>to run</span>
        {showTimer && !sessionActive && !sessionExpired && (
          <>
            <span className="text-slate-700" aria-hidden>
              ·
            </span>
            <Clock className="size-3 text-amber-500/70" aria-hidden />
            <span>Press Start test to begin the {limitMinutes}-minute attempt</span>
          </>
        )}
      </div>
    </header>
  );
}
