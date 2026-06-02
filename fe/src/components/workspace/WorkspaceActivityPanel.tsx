import { ChevronDown, Radio } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { ActivityEntry } from "@/domain/runProgressTypes";
import { cn } from "@/lib/utils";

type Props = {
  activityLog: ActivityEntry[];
  isActive: boolean;
  streamConnected: boolean;
  streamReconnecting: boolean;
};

export default function WorkspaceActivityPanel({
  activityLog,
  isActive,
  streamConnected,
  streamReconnecting,
}: Props) {
  const [open, setOpen] = useState(false);
  const scrollEndRef = useRef<HTMLLIElement | null>(null);

  useEffect(() => {
    if (isActive) {
      setOpen(true);
    }
  }, [isActive]);

  useEffect(() => {
    if (open && scrollEndRef.current) {
      scrollEndRef.current.scrollIntoView({ block: "nearest" });
    }
  }, [activityLog.length, open]);

  if (activityLog.length === 0 && !isActive) {
    return null;
  }

  return (
    <div
      className={cn(
        "shrink-0 border-t border-slate-800/90 bg-[#161b22]",
        open ? "max-h-[min(40vh,220px)]" : "",
      )}
      aria-label="Run activity"
    >
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        className="flex w-full items-center gap-2 px-3 py-1.5 text-left text-xs text-slate-400 transition-colors hover:bg-slate-800/40 hover:text-slate-300"
        aria-expanded={open}
      >
        <span className="font-medium text-slate-300">Activity</span>
        <span className="text-slate-500">({activityLog.length})</span>
        {isActive && (
          <span className="flex items-center gap-1 text-slate-500">
            <Radio className="size-3 animate-pulse text-sky-500" aria-hidden />
            {streamConnected
              ? "Live"
              : streamReconnecting
                ? "Reconnecting…"
                : "Connecting…"}
          </span>
        )}
        <ChevronDown
          className={cn(
            "ml-auto size-3.5 shrink-0 transition-transform",
            open && "rotate-180",
          )}
          aria-hidden
        />
      </button>

      {open && (
        <ScrollArea className="h-[min(32vh,180px)] border-t border-slate-800/60">
          <ul className="space-y-0.5 px-3 py-2 font-mono text-[11px] leading-relaxed text-slate-500">
            {activityLog.length === 0 ? (
              <li className="py-2 text-slate-600">Waiting for runner events…</li>
            ) : (
              activityLog.map((entry, i) => (
                <li
                  key={entry.id}
                  ref={i === activityLog.length - 1 ? scrollEndRef : undefined}
                  className="break-words"
                >
                  {entry.message}
                </li>
              ))
            )}
          </ul>
        </ScrollArea>
      )}
    </div>
  );
}
