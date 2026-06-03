import { ArrowRight } from "lucide-react";
import { Link } from "react-router-dom";
import type { ChallengeSummary } from "@/api/types";
import { ProgressState } from "@/domain/constants";
import { formatLanguageLabel } from "@/utils/languageRuntimes";
import {
  challengeActionLabel,
  challengeProgressAccent,
  challengeProgressLabel,
} from "@/utils/challengeCardUi";
import { difficultyColorClass } from "@/components/workspace/difficultyBadgeStyles";
import { languageBadgeClass } from "@/components/workspace/languageBadgeStyles";
import { cn } from "@/lib/utils";

type Props = {
  challenges: ChallengeSummary[];
  progressBySlug: Map<string, string>;
};

export default function ChallengesContinueRow({ challenges, progressBySlug }: Props) {
  const items = challenges.filter((c) => {
    const state = progressBySlug.get(c.slug);
    return state === ProgressState.ATTEMPTED || state === ProgressState.FAILED;
  });

  if (items.length === 0) {
    return null;
  }

  return (
    <section className="mb-8" aria-labelledby="continue-heading">
      <div className="mb-3 flex items-baseline justify-between gap-2">
        <h2 id="continue-heading" className="text-sm font-semibold text-foreground">
          Continue practicing
        </h2>
        <span className="text-xs text-muted-foreground">{items.length} active</span>
      </div>
      <ul className="m-0 flex list-none gap-3 overflow-x-auto p-0 pb-1 [-ms-overflow-style:none] [scrollbar-width:thin]">
        {items.slice(0, 6).map((challenge) => {
          const state = progressBySlug.get(challenge.slug);
          const accent = challengeProgressAccent(state);
          return (
            <li key={challenge.slug} className="w-[min(100%,280px)] shrink-0 sm:w-[260px]">
              <Link
                to={`/challenges/${challenge.slug}`}
                className={cn(
                  "group flex h-full flex-col rounded-xl border p-4 no-underline transition-all",
                  "border-border bg-card shadow-sm hover:border-emerald-500/40 hover:shadow-md",
                  "dark:border-slate-700/60 dark:bg-slate-900/60 dark:shadow-none",
                  "dark:hover:bg-slate-800/70 dark:hover:shadow-lg dark:hover:shadow-black/20",
                )}
              >
                <div className="mb-2 flex flex-wrap gap-1.5">
                  <span
                    className={cn(
                      "rounded-md border px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide",
                      languageBadgeClass(challenge.language),
                    )}
                  >
                    {formatLanguageLabel(challenge.language)}
                  </span>
                  <span
                    className={cn(
                      "rounded-md border px-2 py-0.5 text-[10px] font-medium capitalize",
                      difficultyColorClass(challenge.difficulty),
                    )}
                  >
                    {challenge.difficulty}
                  </span>
                </div>
                <p className="line-clamp-2 text-sm font-semibold leading-snug text-foreground group-hover:text-emerald-700 dark:group-hover:text-emerald-100">
                  {challenge.title}
                </p>
                <div className="mt-auto flex items-center justify-between gap-2 pt-3">
                  <span
                    className={cn(
                      "rounded-full border px-2 py-0.5 text-[10px] font-medium",
                      accent.chip,
                    )}
                  >
                    {challengeProgressLabel(state)}
                  </span>
                  <span className="inline-flex items-center gap-1 text-xs font-medium text-emerald-600 dark:text-emerald-400">
                    {challengeActionLabel(state)}
                    <ArrowRight
                      className="size-3 transition-transform group-hover:translate-x-0.5"
                      aria-hidden
                    />
                  </span>
                </div>
              </Link>
            </li>
          );
        })}
      </ul>
    </section>
  );
}
