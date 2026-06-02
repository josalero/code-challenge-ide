import { PlusOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Alert, Button, Empty, Segmented, Select, Spin, Typography } from "antd";
import { apiFetch } from "../api/client";
import type { ChallengeSummary, PageResponse, ProgressEntry } from "../api/types";
import { ApiPaths, ProgressState } from "../domain/constants";
import { useAuth } from "../auth/useAuth";
import AppLayout from "../components/AppLayout";
import ChallengeCard from "../components/ChallengeCard";
import CtlCard from "../components/ui/CtlCard";
import PageHeader from "../components/ui/PageHeader";
import {
  filterByLanguage,
  groupChallengesByLanguage,
  type LanguageFilter,
  uniqueLanguages,
} from "../utils/challengeByLanguage";
import {
  matchesProgressFilter,
  type ProgressFilter,
} from "../utils/challengeProgress";
import { formatLanguageLabel } from "../utils/languageRuntimes";

function ChallengeGrid({
  challenges,
  progressBySlug,
}: {
  challenges: ChallengeSummary[];
  progressBySlug: Map<string, string>;
}) {
  return (
    <ul className="m-0 grid list-none gap-4 p-0 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {challenges.map((c) => (
        <li key={c.slug} className="min-h-[168px]">
          <ChallengeCard challenge={c} progressState={progressBySlug.get(c.slug)} />
        </li>
      ))}
    </ul>
  );
}

function ProgressMetric({
  label,
  value,
  tone,
}: {
  label: string;
  value: number;
  tone: "neutral" | "success" | "active" | "danger";
}) {
  const toneClass = {
    neutral: "border-slate-700/70 bg-slate-900/70 text-slate-200",
    success: "border-emerald-500/25 bg-emerald-500/10 text-emerald-300",
    active: "border-sky-500/25 bg-sky-500/10 text-sky-300",
    danger: "border-rose-500/25 bg-rose-500/10 text-rose-300",
  }[tone];

  return (
    <div className={`rounded-lg border px-4 py-3 ${toneClass}`}>
      <p className="mb-1 text-2xl font-semibold leading-none">{value}</p>
      <p className="mb-0 text-xs font-medium uppercase tracking-wide text-slate-500">
        {label}
      </p>
    </div>
  );
}

export default function ChallengesPage() {
  const { isAdmin } = useAuth();
  const [filter, setFilter] = useState<ProgressFilter>("all");
  const [languageFilter, setLanguageFilter] = useState<LanguageFilter>("all");

  const { data, isLoading, error } = useQuery({
    queryKey: ["challenges"],
    queryFn: () =>
      apiFetch<PageResponse<ChallengeSummary>>(`${ApiPaths.CHALLENGES}?size=200`),
  });

  const progressQuery = useQuery({
    queryKey: ["progress"],
    queryFn: () => apiFetch<ProgressEntry[]>(ApiPaths.ME_PROGRESS),
  });

  const progressBySlug = useMemo(
    () =>
      new Map(
        (progressQuery.data ?? []).map((entry) => [entry.challengeSlug, entry.state]),
      ),
    [progressQuery.data],
  );

  const challenges = useMemo(() => data?.content ?? [], [data?.content]);
  const languages = useMemo(() => uniqueLanguages(challenges), [challenges]);

  const filteredChallenges = useMemo(
    () =>
      filterByLanguage(
        challenges.filter((c) =>
          matchesProgressFilter(progressBySlug.get(c.slug), filter),
        ),
        languageFilter,
      ),
    [challenges, filter, languageFilter, progressBySlug],
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

  const languageOptions = useMemo(
    () => [
      { label: "All languages", value: "all" as const },
      ...languages.map((lang) => {
        const count = challenges.filter((c) => c.language === lang).length;
        return {
          label: `${formatLanguageLabel(lang)} (${count})`,
          value: lang,
        };
      }),
    ],
    [languages, challenges],
  );

  const hasActiveFilters = filter !== "all" || languageFilter !== "all";

  return (
    <AppLayout>
      <PageHeader
        title="Challenges"
        description="Choose a language, continue active work, or revisit passed exercises from the same catalog view."
        extra={
          isAdmin ? (
            <Link to="/challenges/new" className="block w-full no-underline sm:w-auto">
              <Button
                type="primary"
                icon={<PlusOutlined aria-hidden />}
                className="w-full !bg-emerald-600 hover:!bg-emerald-500 sm:w-auto"
              >
                Create challenge
              </Button>
            </Link>
          ) : null
        }
      />

      {isLoading && (
        <div className="flex justify-center py-20" role="status" aria-live="polite">
          <Spin size="large" tip="Loading challenges…">
            <div className="min-h-[120px] w-full" aria-hidden />
          </Spin>
        </div>
      )}

      {error && (
        <Alert
          type="error"
          showIcon
          role="alert"
          message={(error as Error).message}
          className="mb-4"
        />
      )}

      {!isLoading && !error && challenges.length > 0 && (
        <>
          <div className="mb-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <ProgressMetric label="Total" value={progressStats.total} tone="neutral" />
            <ProgressMetric label="Passed" value={progressStats.passed} tone="success" />
            <ProgressMetric
              label="In progress"
              value={progressStats.attempted}
              tone="active"
            />
            <ProgressMetric label="Needs work" value={progressStats.failed} tone="danger" />
          </div>

          <div className="flex flex-col gap-6">
            <CtlCard
              title="Filters"
              extra={
                hasActiveFilters ? (
                  <Button
                    type="link"
                    size="small"
                    className="!px-0 !text-emerald-400"
                    onClick={() => {
                      setFilter("all");
                      setLanguageFilter("all");
                    }}
                  >
                    Reset
                  </Button>
                ) : null
              }
            >
              <div
                className="grid gap-4 lg:grid-cols-[minmax(220px,320px)_minmax(360px,1fr)_220px] lg:items-end"
                aria-label="Challenge filters"
              >
                <label className="flex flex-col gap-2">
                  <span className="text-xs font-medium uppercase tracking-wide text-slate-500">
                    Language
                  </span>
                  <Select
                    value={languageFilter}
                    onChange={(v) => setLanguageFilter(v)}
                    options={languageOptions}
                    aria-label="Filter by language"
                  />
                </label>

                <div className="ctl-segmented flex flex-col gap-2">
                  <span className="text-xs font-medium uppercase tracking-wide text-slate-500">
                    Progress
                  </span>
                  <Segmented
                    block
                    value={filter}
                    onChange={(v) => setFilter(v as ProgressFilter)}
                    options={[
                      { label: "All", value: "all" },
                      { label: "New", value: "not_started" },
                      { label: "Active", value: "active" },
                      { label: "Done", value: "passed" },
                    ]}
                  />
                </div>

                <div className="rounded-lg border border-slate-800/80 bg-slate-950/40 px-3 py-3">
                  <p className="mb-1 text-sm font-medium text-slate-200">
                    {filteredChallenges.length} visible
                  </p>
                  <p className="mb-0 text-xs leading-relaxed text-slate-500">
                    {progressStats.notStarted} not started.
                  </p>
                </div>
              </div>
            </CtlCard>

            <CtlCard
              title="Catalog"
              extra={
                <Typography.Text className="!text-slate-500 text-sm">
                  {filteredChallenges.length} of {challenges.length}
                </Typography.Text>
              }
            >
              {filteredChallenges.length === 0 ? (
                <Empty
                  className="py-10"
                  description={
                    <span className="text-slate-400">
                      No challenges match these filters.{" "}
                      <button
                        type="button"
                        className="cursor-pointer border-0 bg-transparent p-0 text-emerald-400 underline hover:text-emerald-300"
                        onClick={() => {
                          setFilter("all");
                          setLanguageFilter("all");
                        }}
                      >
                        Reset filters
                      </button>
                    </span>
                  }
                />
              ) : languageFilter === "all" ? (
                <div className="flex flex-col gap-10">
                  {languageSections.map(({ language, challenges: section }) => (
                    <section key={language} aria-labelledby={`lang-${language}`}>
                      <div className="mb-4 flex flex-wrap items-baseline gap-2 border-b border-slate-700/50 pb-2">
                        <Typography.Title
                          level={2}
                          id={`lang-${language}`}
                          className="!mb-0 !mt-0 !text-xl !font-semibold !text-slate-100"
                        >
                          {formatLanguageLabel(language)}
                        </Typography.Title>
                        <Typography.Text className="!text-slate-500 !text-sm">
                          {section.length} challenge{section.length === 1 ? "" : "s"}
                        </Typography.Text>
                      </div>
                      <ChallengeGrid challenges={section} progressBySlug={progressBySlug} />
                    </section>
                  ))}
                </div>
              ) : (
                <ChallengeGrid
                  challenges={filteredChallenges}
                  progressBySlug={progressBySlug}
                />
              )}
            </CtlCard>
          </div>
        </>
      )}

      {!isLoading && !error && challenges.length === 0 && data && (
        <CtlCard>
          <Empty
            className="py-16"
            description={
              <span className="text-slate-400">
                No challenges published yet. Check back soon or contact your admin.
              </span>
            }
          />
        </CtlCard>
      )}
    </AppLayout>
  );
}
