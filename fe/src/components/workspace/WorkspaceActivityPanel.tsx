import { ChevronDown, Radio } from "lucide-react";
import { useEffect, useRef, useState, type Ref } from "react";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { ActivityEntry, ActivityKind } from "@/domain/runProgressTypes";
import { cn } from "@/lib/utils";
import { inferActivityKind } from "@/utils/activityLog";

type Props = {
  activityLog: ActivityEntry[];
  isActive: boolean;
  streamConnected: boolean;
  streamReconnecting: boolean;
  /** Fills a resizable panel instead of a fixed max-height strip. */
  layout?: "inline" | "resizable";
};

function entryKind(entry: ActivityEntry): ActivityKind {
  return entry.kind ?? inferActivityKind(entry.message);
}

function ActivityLine({
  entry,
  scrollRef,
}: {
  entry: ActivityEntry;
  scrollRef?: Ref<HTMLLIElement>;
}) {
  const kind = entryKind(entry);
  const isTest = kind.startsWith("test-");

  if (isTest) {
    const statusLabel =
      kind === "test-pass" ? "Pass" : kind === "test-skip" ? "Skip" : "Fail";
    return (
      <li
        ref={scrollRef}
        className="flex items-start gap-2 rounded-md px-1.5 py-1 hover:bg-slate-800/50"
        title={entry.title}
      >
        <span
          className={cn(
            "mt-0.5 shrink-0 rounded px-1.5 py-0.5 text-[10px] font-semibold uppercase tracking-wide",
            kind === "test-pass" && "bg-emerald-500/20 text-emerald-300",
            kind === "test-fail" && "bg-red-500/20 text-red-300",
            kind === "test-skip" && "bg-slate-600/40 text-slate-400",
          )}
        >
          {statusLabel}
        </span>
        <span className="min-w-0 flex-1 text-xs leading-snug text-slate-200">
          {entry.message}
        </span>
      </li>
    );
  }

  return (
    <li
      ref={scrollRef}
      className={cn(
        "rounded-md px-1.5 py-1 text-xs leading-relaxed",
        kind === "info" && "text-sky-200",
        kind === "success" && "text-emerald-300",
        kind === "warning" && "text-amber-200",
        kind === "error" && "text-red-300",
      )}
      title={entry.title}
    >
      {entry.message}
    </li>
  );
}

export default function WorkspaceActivityPanel({
  activityLog,
  isActive,
  streamConnected,
  streamReconnecting,
  layout = "inline",
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

  const resizable = layout === "resizable";

  if (!resizable && activityLog.length === 0 && !isActive) {
    return null;
  }

  return (
    <div
      className={cn(
        "border-t border-slate-800/90 bg-[#161b22]",
        resizable ? "flex h-full min-h-0 flex-1 flex-col" : "shrink-0",
        !resizable && open ? "max-h-[min(44vh,280px)]" : "",
      )}
      aria-label="Run activity"
    >
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        className="flex w-full items-center gap-2 px-3 py-2 text-left text-xs text-slate-400 transition-colors hover:bg-slate-800/40 hover:text-slate-300"
        aria-expanded={open}
      >
        <span className="font-medium text-slate-200">Activity</span>
        <span className="text-slate-500">({activityLog.length})</span>
        {isActive && (
          <span className="flex items-center gap-1 text-slate-400">
            <Radio className="size-3 animate-pulse text-sky-400" aria-hidden />
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
        <ScrollArea
          className={cn(
            "min-h-0 flex-1 border-t border-slate-800/60",
            resizable ? "" : "h-[min(36vh,240px)]",
          )}
        >
          <ul className="space-y-0.5 px-2 py-2">
            {activityLog.length === 0 ? (
              <li className="px-1.5 py-2 text-xs text-slate-500">Waiting for runner events…</li>
            ) : (
              activityLog.map((entry, i) => (
                <ActivityLine
                  key={entry.id}
                  entry={entry}
                  scrollRef={i === activityLog.length - 1 ? scrollEndRef : undefined}
                />
              ))
            )}
          </ul>
        </ScrollArea>
      )}
    </div>
  );
}
