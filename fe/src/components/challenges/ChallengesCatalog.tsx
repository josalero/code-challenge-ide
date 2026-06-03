import { Layers } from "lucide-react";
import { Empty } from "antd";
import type { ChallengeSummary } from "@/api/types";
import ChallengeCard from "@/components/ChallengeCard";
import { Skeleton } from "@/components/ui/skeleton";
import type { LanguageFilter } from "@/utils/challengeByLanguage";
import { formatLanguageLabel } from "@/utils/languageRuntimes";

type Section = { language: string; challenges: ChallengeSummary[] };

type Props = {
  loading?: boolean;
  sections: Section[];
  languageFilter: LanguageFilter;
  flatChallenges: ChallengeSummary[];
  progressBySlug: Map<string, string>;
  onResetFilters: () => void;
};

function ChallengeGrid({
  challenges,
  progressBySlug,
}: {
  challenges: ChallengeSummary[];
  progressBySlug: Map<string, string>;
}) {
  return (
    <ul className="m-0 grid list-none gap-4 p-0 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
      {challenges.map((c) => (
        <li key={c.slug}>
          <ChallengeCard challenge={c} progressState={progressBySlug.get(c.slug)} />
        </li>
      ))}
    </ul>
  );
}

function CatalogSkeleton() {
  return (
    <ul className="m-0 grid list-none gap-4 p-0 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
      {Array.from({ length: 8 }, (_, i) => (
        <li key={i}>
          <Skeleton className="h-[172px] w-full rounded-xl bg-muted dark:bg-slate-800/80" />
        </li>
      ))}
    </ul>
  );
}

export default function ChallengesCatalog({
  loading,
  sections,
  languageFilter,
  flatChallenges,
  progressBySlug,
  onResetFilters,
}: Props) {
  if (loading) {
    return <CatalogSkeleton />;
  }

  if (flatChallenges.length === 0) {
    return (
      <div className="rounded-2xl border border-dashed border-border bg-muted/30 px-6 py-16 dark:border-slate-700/60 dark:bg-slate-900/30">
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={
            <span className="text-muted-foreground">
              No challenges match your filters.{" "}
              <button
                type="button"
                className="cursor-pointer border-0 bg-transparent p-0 font-medium text-emerald-600 underline-offset-2 hover:text-emerald-700 hover:underline dark:text-emerald-400 dark:hover:text-emerald-300"
                onClick={onResetFilters}
              >
                Reset filters
              </button>
            </span>
          }
        />
      </div>
    );
  }

  if (languageFilter !== "all") {
    return (
      <ChallengeGrid challenges={flatChallenges} progressBySlug={progressBySlug} />
    );
  }

  return (
    <div className="flex flex-col gap-10">
      {sections.map(({ language, challenges }) => (
        <section key={language} aria-labelledby={`lang-section-${language}`}>
          <div className="mb-4 flex items-center gap-3">
            <span className="flex size-8 items-center justify-center rounded-lg bg-muted text-muted-foreground ring-1 ring-border dark:bg-slate-800/80 dark:text-slate-400 dark:ring-slate-700/60">
              <Layers className="size-4" aria-hidden />
            </span>
            <div className="min-w-0">
              <h2
                id={`lang-section-${language}`}
                className="text-lg font-semibold text-foreground"
              >
                {formatLanguageLabel(language)}
              </h2>
              <p className="text-xs text-muted-foreground">
                {challenges.length} challenge{challenges.length === 1 ? "" : "s"}
              </p>
            </div>
          </div>
          <ChallengeGrid challenges={challenges} progressBySlug={progressBySlug} />
        </section>
      ))}
    </div>
  );
}
