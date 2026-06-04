import { Plus } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Alert } from "antd";
import { apiFetch } from "../api/client";
import type { ChallengeSummary, PageResponse, ProgressEntry } from "../api/types";
import { ApiPaths, ProgressState } from "../domain/constants";
import { useAuth } from "../auth/useAuth";
import AppLayout from "../components/AppLayout";
import {
  challengeQuotaMessage,
  useChallengeQuota,
} from "../hooks/useChallengeQuota";
import ChallengesFilterBar from "../components/challenges/ChallengesFilterBar";
import ChallengesHero from "../components/challenges/ChallengesHero";
import ChallengesResultsPanel from "../components/challenges/ChallengesResultsPanel";
import { Button } from "@/components/ui/button";
import {
  filterByLanguage,
  groupChallengesByLanguage,
  type LanguageFilter,
  uniqueLanguages,
} from "../utils/challengeByLanguage";
import { challengeMatchesSearch } from "../utils/challengeCardUi";
import {
  matchesProgressFilter,
  type ProgressFilter,
} from "../utils/challengeProgress";

export default function ChallengesPage() {
  const { isAdmin, user } = useAuth();
  const [filter, setFilter] = useState<ProgressFilter>("all");
  const [languageFilter, setLanguageFilter] = useState<LanguageFilter>("all");
  const [search, setSearch] = useState("");

  const { data, isLoading, error } = useQuery({
    queryKey: ["challenges"],
    queryFn: () =>
      apiFetch<PageResponse<ChallengeSummary>>(`${ApiPaths.CHALLENGES}?size=200`),
  });

  const progressQuery = useQuery({
    queryKey: ["progress"],
    queryFn: () => apiFetch<ProgressEntry[]>(ApiPaths.ME_PROGRESS),
  });

  const quotaQuery = useChallengeQuota(Boolean(user) && !isAdmin);
  const quotaMessage =
    quotaQuery.data?.maxStartedChallenges != null
    && (quotaQuery.data.challengesRemaining ?? 0) <= 0
      ? challengeQuotaMessage(quotaQuery.data)
      : null;

  const progressBySlug = useMemo(
    () =>
      new Map(
        (progressQuery.data ?? []).map((entry) => [entry.challengeSlug, entry.state]),
      ),
    [progressQuery.data],
  );

  const challenges = useMemo(() => data?.content ?? [], [data?.content]);
  const languages = useMemo(() => uniqueLanguages(challenges), [challenges]);

  const languageCounts = useMemo(() => {
    const counts = new Map<string, number>();
    for (const c of challenges) {
      counts.set(c.language, (counts.get(c.language) ?? 0) + 1);
    }
    return counts;
  }, [challenges]);

  const filteredChallenges = useMemo(
    () =>
      filterByLanguage(
        challenges.filter(
          (c) =>
            challengeMatchesSearch(c, search)
            && matchesProgressFilter(progressBySlug.get(c.slug), filter),
        ),
        languageFilter,
      ),
    [challenges, filter, languageFilter, progressBySlug, search],
  );

  const languageSections = useMemo(
    () => groupChallengesByLanguage(filteredChallenges),
    [filteredChallenges],
  );

  const progressStats = useMemo(() => {
    const passed = challenges.filter(
      (c) => progressBySlug.get(c.slug) === ProgressState.PASSED,
    ).length;
    const attempted = challenges.filter(
      (c) => progressBySlug.get(c.slug) === ProgressState.ATTEMPTED,
    ).length;
    const failed = challenges.filter(
      (c) => progressBySlug.get(c.slug) === ProgressState.FAILED,
    ).length;
    return {
      passed,
      attempted,
      failed,
      notStarted: Math.max(challenges.length - passed - attempted - failed, 0),
      total: challenges.length,
    };
  }, [challenges, progressBySlug]);

  const hasActiveFilters =
    filter !== "all" || languageFilter !== "all" || search.trim().length > 0;

  const resetFilters = () => {
    setFilter("all");
    setLanguageFilter("all");
    setSearch("");
  };

  const adminAction = isAdmin ? (
    <Link to="/challenges/new" className="no-underline">
      <Button className="gap-2 bg-emerald-600 text-white hover:bg-emerald-500">
        <Plus className="size-4" aria-hidden />
        Create challenge
      </Button>
    </Link>
  ) : null;

  return (
    <AppLayout contentLayout="wide">
      <div className="flex flex-col">
        {error && (
          <Alert
            type="error"
            showIcon
            role="alert"
            message={(error as Error).message}
            className="mb-6"
          />
        )}

        {!error && (
          <>
            {quotaMessage && (
              <Alert
                type="warning"
                showIcon
                className="mb-6"
                message="Exercise limit reached"
                description={quotaMessage}
              />
            )}

            <ChallengesHero stats={progressStats} extra={adminAction} />

            {(isLoading || challenges.length > 0) && (
              <div className="mt-8 grid gap-6 lg:grid-cols-[minmax(240px,280px)_minmax(0,1fr)] lg:items-start">
                <ChallengesFilterBar
                  search={search}
                  onSearchChange={setSearch}
                  languageFilter={languageFilter}
                  onLanguageFilterChange={setLanguageFilter}
                  progressFilter={filter}
                  onProgressFilterChange={setFilter}
                  languages={languages}
                  languageCounts={languageCounts}
                  visibleCount={filteredChallenges.length}
                  totalCount={challenges.length}
                  onReset={resetFilters}
                  hasActiveFilters={hasActiveFilters}
                />

                <ChallengesResultsPanel
                  loading={isLoading}
                  sections={languageSections}
                  languageFilter={languageFilter}
                  flatChallenges={filteredChallenges}
                  progressBySlug={progressBySlug}
                  allChallenges={challenges}
                  showContinue={!hasActiveFilters}
                  onResetFilters={resetFilters}
                />
              </div>
            )}

            {!isLoading && challenges.length === 0 && data && (
              <div className="mt-8 rounded-2xl border border-dashed border-border bg-muted/30 px-6 py-20 text-center dark:border-slate-700/60 dark:bg-slate-900/30">
                <p className="text-lg font-medium text-foreground">No challenges yet</p>
                <p className="mt-2 text-sm text-muted-foreground">
                  Check back soon or ask an admin to publish exercises.
                </p>
                {isAdmin && (
                  <Link to="/challenges/new" className="mt-6 inline-block no-underline">
                    <Button className="gap-2 bg-emerald-600 text-white hover:bg-emerald-500">
                      <Plus className="size-4" aria-hidden />
                      Create the first challenge
                    </Button>
                  </Link>
                )}
              </div>
            )}
          </>
        )}
      </div>
    </AppLayout>
  );
}
