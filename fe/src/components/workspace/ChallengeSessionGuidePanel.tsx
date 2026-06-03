import { Clock, Play, XCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { formatSessionCountdown } from "@/utils/challengeSession";

type Props = {
  sessionDurationMinutes: number;
  sessionActive: boolean;
  sessionExpired: boolean;
  sessionCountdown: string | null;
  showStartTest: boolean;
  onStartTest?: () => void;
  showAbandonAttempt: boolean;
  onAbandonAttempt?: () => void;
};

export default function ChallengeSessionGuidePanel({
  sessionDurationMinutes,
  sessionActive,
  sessionExpired,
  sessionCountdown,
  showStartTest,
  onStartTest,
  showAbandonAttempt,
  onAbandonAttempt,
}: Props) {
  const limitMinutes = sessionDurationMinutes > 0 ? sessionDurationMinutes : 60;

  return (
    <div className="space-y-4 px-1 py-1 text-sm text-slate-300">
      <div className="rounded-lg border border-amber-500/25 bg-amber-500/5 px-3 py-3">
        <div className="flex items-center gap-2">
          <Clock className="size-4 text-amber-400/90" aria-hidden />
          <p className="font-medium text-amber-100/95">
            {limitMinutes}-minute timed attempt
          </p>
        </div>
        {sessionActive && sessionCountdown && (
          <p className="mt-2 font-mono text-lg tabular-nums text-amber-100">
            {sessionExpired ? "Time's up" : `${sessionCountdown} remaining`}
          </p>
        )}
      </div>

      <section aria-labelledby="guide-steps-heading">
        <h2
          id="guide-steps-heading"
          className="mb-2 text-xs font-semibold uppercase tracking-wider text-slate-400"
        >
          How it works
        </h2>
        <ol className="list-decimal space-y-2 pl-4 text-slate-400">
          <li>
            Read the <strong className="text-slate-200">problem</strong> on the left and the{" "}
            <strong className="text-slate-200">starter skeleton</strong> in the center editor
            (read-only until you start).
          </li>
          <li>
            Press <strong className="text-slate-200">Start test</strong> when you are ready to
            code — the countdown begins then.
          </li>
          <li>
            Use <strong className="text-slate-200">Run</strong> to practice, then{" "}
            <strong className="text-slate-200">Submit</strong> for full feedback.
          </li>
        </ol>
      </section>

      <section aria-labelledby="guide-actions-heading">
        <h2
          id="guide-actions-heading"
          className="mb-2 text-xs font-semibold uppercase tracking-wider text-slate-400"
        >
          Actions
        </h2>
        <ul className="space-y-2 text-slate-400">
          <li>
            <strong className="text-slate-200">Cancel run</strong> — stops only the current
            Docker test; the clock keeps going.
          </li>
          <li>
            <strong className="text-slate-200">Abandon</strong> — resets the timer and exits focus
            mode; your draft is kept. Press Start test again for a new full limit.
          </li>
        </ul>
      </section>

      <div className="flex flex-col gap-2 pt-1">
        {showStartTest && onStartTest && (
          <Button
            className="w-full gap-2 bg-emerald-600 font-semibold text-white hover:bg-emerald-500"
            onClick={onStartTest}
          >
            <Play className="size-4" aria-hidden />
            Start test
          </Button>
        )}
        {showAbandonAttempt && onAbandonAttempt && (
          <Button
            variant="outline"
            className="w-full gap-2 border-slate-600/60 text-slate-300 hover:border-red-500/40 hover:text-red-200"
            onClick={onAbandonAttempt}
          >
            <XCircle className="size-4" aria-hidden />
            Abandon attempt
          </Button>
        )}
        {sessionActive && !sessionExpired && (
          <p className="text-center text-xs text-slate-500">
            Full limit: {formatSessionCountdown(limitMinutes * 60)}
          </p>
        )}
      </div>
    </div>
  );
}
