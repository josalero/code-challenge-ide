import type { ChallengeSummary } from "@/api/types";
import ChallengesCatalog from "./ChallengesCatalog";
import ChallengesContinueRow from "./ChallengesContinueRow";
import type { LanguageFilter } from "@/utils/challengeByLanguage";

type Section = { language: string; challenges: ChallengeSummary[] };

type Props = {
  loading: boolean;
  sections: Section[];
  languageFilter: LanguageFilter;
  flatChallenges: ChallengeSummary[];
  progressBySlug: Map<string, string>;
  allChallenges: ChallengeSummary[];
  showContinue: boolean;
  onResetFilters: () => void;
};

export default function ChallengesResultsPanel({
  loading,
  sections,
  languageFilter,
  flatChallenges,
  progressBySlug,
  allChallenges,
  showContinue,
  onResetFilters,
}: Props) {
  return (
    <main className="min-w-0" aria-labelledby="challenges-results-heading">
      <div className="mb-4 flex flex-wrap items-baseline justify-between gap-2">
        <div>
          <h2
            id="challenges-results-heading"
            className="text-lg font-semibold text-foreground"
          >
            Results
          </h2>
          <p className="mt-0.5 text-sm text-muted-foreground">
            {loading
              ? "Loading catalog…"
              : `${flatChallenges.length} challenge${flatChallenges.length === 1 ? "" : "s"} match your filters`}
          </p>
        </div>
      </div>

      {showContinue && !loading && (
        <ChallengesContinueRow
          challenges={allChallenges}
          progressBySlug={progressBySlug}
        />
      )}

      <ChallengesCatalog
        loading={loading}
        sections={sections}
        languageFilter={languageFilter}
        flatChallenges={flatChallenges}
        progressBySlug={progressBySlug}
        onResetFilters={onResetFilters}
      />
    </main>
  );
}
