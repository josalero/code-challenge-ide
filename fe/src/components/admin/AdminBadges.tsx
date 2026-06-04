import type { ChallengeEngagementStatus } from "@/api/types";
import { formatLanguageLabel } from "@/utils/languageRuntimes";
import { cn } from "@/lib/utils";

const styles: Record<
  ChallengeEngagementStatus,
  { label: string; className: string }
> = {
  PASSED: {
    label: "Passed",
    className:
      "border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
  },
  FAILED: {
    label: "Failed",
    className: "border-rose-500/30 bg-rose-500/10 text-rose-700 dark:text-rose-300",
  },
  LIKELY_ABANDONED: {
    label: "Likely abandoned",
    className: "border-amber-500/30 bg-amber-500/10 text-amber-800 dark:text-amber-300",
  },
  IN_PROGRESS: {
    label: "In progress",
    className: "border-sky-500/30 bg-sky-500/10 text-sky-700 dark:text-sky-300",
  },
  NOT_STARTED: {
    label: "Not started",
    className: "border-border bg-muted/50 text-muted-foreground",
  },
};

export default function EngagementBadge({ status }: { status: ChallengeEngagementStatus }) {
  const style = styles[status];
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full border px-2.5 py-0.5 text-[11px] font-medium leading-none",
        style.className,
      )}
    >
      {style.label}
    </span>
  );
}

export function RoleBadge({ role }: { role: "ADMIN" | "USER" | string }) {
  const isAdmin = role === "ADMIN";
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full border px-2.5 py-0.5 text-[11px] font-medium",
        isAdmin
          ? "border-violet-500/30 bg-violet-500/10 text-violet-700 dark:text-violet-300"
          : "border-border bg-muted/40 text-muted-foreground",
      )}
    >
      {isAdmin ? "Admin" : "Learner"}
    </span>
  );
}

export function StatusBadge({ active }: { active: boolean }) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-[11px] font-medium",
        active
          ? "border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300"
          : "border-border bg-muted/40 text-muted-foreground",
      )}
    >
      <span
        className={cn("size-1.5 rounded-full", active ? "bg-emerald-500" : "bg-muted-foreground/50")}
        aria-hidden
      />
      {active ? "Active" : "Deactivated"}
    </span>
  );
}

export function DifficultyChip({ difficulty }: { difficulty: string | null }) {
  if (!difficulty) {
    return <span className="text-muted-foreground">—</span>;
  }
  const normalized = difficulty.toLowerCase();
  const tone =
    normalized === "easy"
      ? "border-emerald-500/25 bg-emerald-500/5 text-emerald-700 dark:text-emerald-400"
      : normalized === "hard"
        ? "border-rose-500/25 bg-rose-500/5 text-rose-700 dark:text-rose-400"
        : "border-amber-500/25 bg-amber-500/5 text-amber-800 dark:text-amber-400";

  return (
    <span
      className={cn(
        "inline-flex rounded-md border px-1.5 py-0.5 text-[10px] font-medium uppercase tracking-wide",
        tone,
      )}
    >
      {difficulty}
    </span>
  );
}

function languageChipTone(language: string): string {
  switch (language) {
    case "java":
      return "border-blue-500/30 bg-blue-500/10 text-blue-700 dark:text-blue-300";
    case "python":
      return "border-amber-500/30 bg-amber-500/10 text-amber-800 dark:text-amber-300";
    case "sql":
      return "border-cyan-500/30 bg-cyan-500/10 text-cyan-800 dark:text-cyan-300";
    case "typescript":
      return "border-indigo-500/30 bg-indigo-500/10 text-indigo-700 dark:text-indigo-300";
    case "react":
      return "border-sky-500/30 bg-sky-500/10 text-sky-700 dark:text-sky-300";
    case "vue":
      return "border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300";
    case "angular":
      return "border-rose-500/30 bg-rose-500/10 text-rose-700 dark:text-rose-300";
    case "go":
      return "border-teal-500/30 bg-teal-500/10 text-teal-700 dark:text-teal-300";
    case "node":
      return "border-lime-500/30 bg-lime-500/10 text-lime-800 dark:text-lime-300";
    case "csharp":
      return "border-violet-500/30 bg-violet-500/10 text-violet-700 dark:text-violet-300";
    case "rust":
      return "border-orange-500/30 bg-orange-500/10 text-orange-800 dark:text-orange-300";
    case "cpp":
      return "border-orange-600/30 bg-orange-600/10 text-orange-800 dark:text-orange-300";
    default:
      return "border-border bg-muted/40 text-muted-foreground";
  }
}

export function LanguageChip({ language }: { language: string | null | undefined }) {
  if (!language) {
    return <span className="text-muted-foreground">—</span>;
  }

  return (
    <span
      className={cn(
        "inline-flex max-w-full truncate rounded-full border px-2.5 py-0.5 text-[11px] font-medium leading-none",
        languageChipTone(language),
      )}
    >
      {formatLanguageLabel(language)}
    </span>
  );
}
