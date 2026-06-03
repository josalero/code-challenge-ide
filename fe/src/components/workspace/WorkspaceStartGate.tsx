import { Clock, Play } from "lucide-react";
import { Button } from "@/components/ui/button";

type Props = {
  visible: boolean;
  limitMinutes: number;
  onStartTest: () => void;
};

/** Bottom bar — keeps the starter skeleton visible in the editor above. */
export default function WorkspaceStartGate({ visible, limitMinutes, onStartTest }: Props) {
  if (!visible) {
    return null;
  }

  return (
    <div
      className="absolute inset-x-0 bottom-0 z-20 flex flex-wrap items-center justify-between gap-3 border-t border-amber-500/30 bg-slate-900/95 px-4 py-3 shadow-[0_-8px_24px_rgba(0,0,0,0.35)]"
      role="region"
      aria-label="Start timed attempt"
    >
      <div className="flex min-w-0 items-start gap-2.5 text-left">
        <Clock className="mt-0.5 size-4 shrink-0 text-amber-400/90" aria-hidden />
        <div className="min-w-0">
          <p className="text-sm font-medium text-slate-100">
            Review the starter skeleton above (read-only)
          </p>
          <p className="mt-0.5 text-xs leading-relaxed text-slate-400">
            Open the <strong className="text-slate-300">Guide</strong> tab on the right for the full
            timed-attempt steps, then start the {limitMinutes}-minute timer when you are ready.
          </p>
        </div>
      </div>
      <Button
        size="lg"
        className="h-10 shrink-0 gap-2 bg-emerald-600 px-6 font-semibold text-white hover:bg-emerald-500"
        onClick={onStartTest}
      >
        <Play className="size-4" aria-hidden />
        Start test
      </Button>
    </div>
  );
}
