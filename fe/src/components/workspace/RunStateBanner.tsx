import { AlertCircle, CheckCircle2, Clock, ServerCrash, XCircle } from "lucide-react";
import type { WorkspaceRunPhase } from "@/domain/workspaceRunState";
import { RUN_PHASE_LABELS } from "@/domain/workspaceRunState";

type Props = {
  phase: WorkspaceRunPhase;
  message?: string | null;
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
    description: "Fix syntax or compile errors in the Compiler tab, then run again.",
    className: "border-red-500/35 bg-red-500/10 text-red-100",
  },
  "failed-test": {
    icon: AlertCircle,
    title: RUN_PHASE_LABELS["failed-test"],
    description: "One or more tests did not pass. Review Tests and Feedback tabs.",
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
    description: "Some tests failed. Fix your solution and run again, or submit for full feedback.",
    className: "border-amber-500/35 bg-amber-500/10 text-amber-50",
  },
  "successful-submission": {
    icon: CheckCircle2,
    title: RUN_PHASE_LABELS["successful-submission"],
    description: "Submitted and passed. Use Redo to attempt the exercise again.",
    className: "border-emerald-500/40 bg-emerald-500/10 text-emerald-50",
  },
};

export default function RunStateBanner({ phase, message }: Props) {
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
      <div>
        <p className="font-medium">{cfg.title}</p>
        <p className="mt-0.5 text-xs opacity-90">{message ?? cfg.description}</p>
      </div>
    </div>
  );
}
