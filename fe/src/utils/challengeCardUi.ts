import { ProgressState } from "@/domain/constants";

export function challengeActionLabel(state?: string): string {
  if (state === ProgressState.PASSED) {
    return "Review";
  }
  if (state === ProgressState.ATTEMPTED || state === ProgressState.FAILED) {
    return "Continue";
  }
  return "Start";
}

export function challengeProgressLabel(state?: string): string {
  if (!state || state === ProgressState.NOT_STARTED) {
    return "Not started";
  }
  if (state === ProgressState.PASSED) {
    return "Passed";
  }
  if (state === ProgressState.ATTEMPTED) {
    return "In progress";
  }
  if (state === ProgressState.FAILED) {
    return "Needs work";
  }
  return state;
}

/** Left accent + status chip tones for catalog cards. */
export function challengeProgressAccent(state?: string): {
  border: string;
  chip: string;
  dot: string;
} {
  if (state === ProgressState.PASSED) {
    return {
      border: "from-emerald-500/80",
      chip:
        "border-emerald-500/35 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
      dot: "bg-emerald-500 dark:bg-emerald-400",
    };
  }
  if (state === ProgressState.ATTEMPTED) {
    return {
      border: "from-sky-500/80",
      chip: "border-sky-500/35 bg-sky-500/10 text-sky-700 dark:text-sky-300",
      dot: "bg-sky-500 dark:bg-sky-400",
    };
  }
  if (state === ProgressState.FAILED) {
    return {
      border: "from-rose-500/80",
      chip: "border-rose-500/35 bg-rose-500/10 text-rose-700 dark:text-rose-300",
      dot: "bg-rose-500 dark:bg-rose-400",
    };
  }
  return {
    border: "from-slate-400/60",
    chip:
      "border-border bg-muted text-muted-foreground dark:border-slate-600/50 dark:bg-slate-800/60 dark:text-slate-400",
    dot: "bg-slate-400 dark:bg-slate-500",
  };
}

export function challengeMatchesSearch(
  challenge: { title: string; slug: string },
  query: string,
): boolean {
  const q = query.trim().toLowerCase();
  if (!q) {
    return true;
  }
  return (
    challenge.title.toLowerCase().includes(q)
    || challenge.slug.toLowerCase().includes(q)
  );
}
