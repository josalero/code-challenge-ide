import { ArrowRight } from "lucide-react";
import { Link } from "react-router-dom";
import type { ChallengeSummary } from "../api/types";
import { difficultyColorClass } from "./workspace/difficultyBadgeStyles";
import { languageBadgeClass } from "./workspace/languageBadgeStyles";
import {
  challengeActionLabel,
  challengeProgressAccent,
  challengeProgressLabel,
} from "../utils/challengeCardUi";
import { formatLanguageLabel, runnerPipelineLabel } from "../utils/languageRuntimes";
import { cn } from "@/lib/utils";

type Props = {
  challenge: ChallengeSummary;
  progressState?: string;
};

export default function ChallengeCard({ challenge, progressState }: Props) {
  const accent = challengeProgressAccent(progressState);
  const action = challengeActionLabel(progressState);

  return (
    <article
      className={cn(
        "ctl-challenge-card group relative flex h-full min-h-[172px] flex-col overflow-hidden rounded-xl",
        "border border-border bg-card shadow-sm transition-all duration-200",
        "hover:border-emerald-500/40 hover:shadow-md",
        "dark:border-slate-600/45 dark:bg-slate-800/40 dark:shadow-none",
        "dark:hover:border-emerald-500/35 dark:hover:bg-slate-800/70 dark:hover:shadow-md dark:hover:shadow-black/20",
      )}
    >
      <div
        className={cn(
          "absolute inset-y-0 left-0 w-1 bg-gradient-to-b to-transparent opacity-80",
          accent.border,
        )}
        aria-hidden
      />

      <div className="flex flex-1 flex-col p-4 pl-5">
        <div className="mb-3 flex flex-wrap items-center gap-1.5">
          {challenge.language && (
            <span
              className={cn(
                "rounded-md border px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide",
                languageBadgeClass(challenge.language),
              )}
            >
              {formatLanguageLabel(challenge.language)}
            </span>
          )}
          <span
            className={cn(
              "rounded-md border px-2 py-0.5 text-[10px] font-medium capitalize",
              difficultyColorClass(challenge.difficulty),
            )}
          >
            {challenge.difficulty}
          </span>
        </div>

        <h3 className="mb-1 line-clamp-2 text-base font-semibold leading-snug text-foreground">
          <Link
            to={`/challenges/${challenge.slug}`}
            className="text-inherit no-underline after:absolute after:inset-0 hover:text-emerald-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-500 dark:hover:text-emerald-300"
          >
            {challenge.title}
          </Link>
        </h3>

        <p className="line-clamp-2 text-xs leading-relaxed text-muted-foreground">
          {runnerPipelineLabel(challenge.language)}
        </p>

        <div className="mt-auto flex items-center justify-between gap-2 pt-4">
          <span
            className={cn(
              "inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-[10px] font-medium",
              accent.chip,
            )}
          >
            <span className={cn("size-1.5 rounded-full", accent.dot)} aria-hidden />
            {challengeProgressLabel(progressState)}
          </span>
          <span className="inline-flex items-center gap-1 text-sm font-medium text-emerald-600 transition-colors group-hover:text-emerald-700 dark:text-emerald-400/90 dark:group-hover:text-emerald-300">
            {action}
            <ArrowRight
              className="size-3.5 transition-transform group-hover:translate-x-0.5"
              aria-hidden
            />
          </span>
        </div>
      </div>
    </article>
  );
}
