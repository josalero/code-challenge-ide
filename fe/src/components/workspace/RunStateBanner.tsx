import { AlertCircle, CheckCircle2, Clock, PanelRight, ServerCrash, XCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { WorkspaceRunPhase } from "@/domain/workspaceRunState";
import { RUN_PHASE_LABELS } from "@/domain/workspaceRunState";

type Props = {
  phase: WorkspaceRunPhase;
  message?: string | null;
  onViewOutput?: () => void;
};

const CONFIG: Record<
  Exclude<WorkspaceRunPhase, "idle" | "loading">,
  {
    icon: typeof AlertCircle;
    title: string;
    description: string;
    className: string;
  }
> = {
  running: {
    icon: Clock,
    title: RUN_PHASE_LABELS.running,
    description: "Your solution is executing in an isolated Docker sandbox.",
    className: "border-sky-500/30 bg-sky-500/10 text-sky-100",
  },
  "compilation-error": {
    icon: XCircle,
    title: RUN_PHASE_LABELS["compilation-error"],
    description: "Open the Output panel → Compiler tab for build errors, then run again.",
    className: "border-red-500/35 bg-red-500/10 text-red-100",
  },
  "failed-test": {
    icon: AlertCircle,
    title: RUN_PHASE_LABELS["failed-test"],
    description: "Open the Output panel → Tests tab for failure details and stack traces.",
    className: "border-amber-500/35 bg-amber-500/10 text-amber-50",
  },
  timeout: {
    icon: Clock,
    title: RUN_PHASE_LABELS.timeout,
    description: "The runner exceeded its time limit. Simplify or cancel stuck runs.",
    className: "border-red-500/35 bg-red-500/10 text-red-100",
  },
  "session-expired": {
    icon: Clock,
    title: RUN_PHASE_LABELS["session-expired"],
    description:
      "Your allotted time for this challenge has ended. Leave the workspace or use Redo if the exercise was already submitted.",
    className: "border-red-500/35 bg-red-500/10 text-red-100",
  },
  "service-unavailable": {
    icon: ServerCrash,
    title: RUN_PHASE_LABELS["service-unavailable"],
    description: "Could not reach the runner or API. Check backend and Docker.",
    className: "border-red-500/35 bg-red-500/10 text-red-100",
  },
  "run-passed": {
    icon: CheckCircle2,
    title: RUN_PHASE_LABELS["run-passed"],
    description: "All tests passed in this practice run. Keep editing or submit when ready.",
    className: "border-emerald-500/40 bg-emerald-500/10 text-emerald-50",
  },
  "run-failed": {
    icon: AlertCircle,
    title: RUN_PHASE_LABELS["run-failed"],
    description: "Check the Output panel for test failures and runner logs, then run again.",
    className: "border-amber-500/35 bg-amber-500/10 text-amber-50",
  },
  "successful-submission": {
    icon: CheckCircle2,
    title: RUN_PHASE_LABELS["successful-submission"],
    description: "Submitted and passed. Use Redo to attempt the exercise again.",
    className: "border-emerald-500/40 bg-emerald-500/10 text-emerald-50",
  },
};

const SHOW_OUTPUT_CTA: WorkspaceRunPhase[] = [
  "compilation-error",
  "failed-test",
  "run-failed",
  "run-passed",
  "timeout",
  "service-unavailable",
  "successful-submission",
];

export default function RunStateBanner({ phase, message, onViewOutput }: Props) {
  if (phase === "idle" || phase === "loading") {
    return null;
  }

  const cfg = CONFIG[phase];
  const Icon = cfg.icon;

  return (
    <div
      className={`flex items-start gap-3 rounded-lg border px-3 py-2.5 text-sm ${cfg.className}`}
      role="status"
      aria-live="polite"
    >
      <Icon className="mt-0.5 size-4 shrink-0" aria-hidden />
      <div className="min-w-0 flex-1">
        <p className="font-medium">{cfg.title}</p>
        <p className="mt-0.5 text-xs opacity-90">{message ?? cfg.description}</p>
      </div>
      {onViewOutput && SHOW_OUTPUT_CTA.includes(phase) && (
        <Button
          type="button"
          variant="outline"
          size="sm"
          className="shrink-0 border-current/25 bg-black/10 text-xs hover:bg-black/20"
          onClick={onViewOutput}
        >
          <PanelRight className="size-3.5" aria-hidden />
          View output
        </Button>
      )}
    </div>
  );
}
