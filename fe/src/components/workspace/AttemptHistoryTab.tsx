import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { SubmissionStatusValue } from "@/domain/constants";

export type AttemptRecord = {
  id: string;
  status: SubmissionStatusValue;
  createdAt: string;
  passed: boolean | null;
  summary?: string;
};

type Props = {
  attempts: AttemptRecord[];
  onSelectAttempt?: (id: string) => void;
};

export default function AttemptHistoryTab({ attempts, onSelectAttempt }: Props) {
  return (
    <ScrollArea className="h-full max-h-[280px]">
      <ul className="divide-y divide-slate-800/80">
        {attempts.map((attempt) => (
          <li key={attempt.id}>
            <button
              type="button"
              className="flex w-full items-start justify-between gap-3 px-1 py-3 text-left text-sm transition-colors hover:bg-slate-800/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-500/50"
              onClick={() => onSelectAttempt?.(attempt.id)}
            >
              <div>
                <span className="font-medium text-slate-200">
                  {new Date(attempt.createdAt).toLocaleString()}
                </span>
                {attempt.summary && (
                  <p className="mt-0.5 line-clamp-2 text-xs text-slate-500">
                    {attempt.summary}
                  </p>
                )}
              </div>
              <div className="flex shrink-0 flex-col items-end gap-1">
                <Badge
                  variant="outline"
                  className="border-slate-600/60 font-normal text-slate-400"
                >
                  {attempt.status}
                </Badge>
                {attempt.passed === true && (
                  <Badge className="border-emerald-500/40 bg-emerald-500/10 text-emerald-400">
                    Passed
                  </Badge>
                )}
                {attempt.passed === false && (
                  <Badge variant="outline" className="border-red-500/40 text-red-400">
                    Blocked
                  </Badge>
                )}
              </div>
            </button>
          </li>
        ))}
      </ul>
    </ScrollArea>
  );
}
