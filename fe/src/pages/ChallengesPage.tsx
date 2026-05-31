import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Alert, Empty, Segmented, Spin, Tag, Typography } from "antd";
import { apiFetch } from "../api/client";
import type { ChallengeSummary, PageResponse, ProgressEntry } from "../api/types";
import { ApiPaths, ProgressState } from "../domain/constants";
import AppLayout from "../components/AppLayout";
import ChallengeCard from "../components/ChallengeCard";
import CtlCard from "../components/ui/CtlCard";
import PageHeader from "../components/ui/PageHeader";
import {
  matchesProgressFilter,
  type ProgressFilter,
} from "../utils/challengeProgress";

export default function ChallengesPage() {
  const [filter, setFilter] = useState<ProgressFilter>("all");

  const { data, isLoading, error } = useQuery({
    queryKey: ["challenges"],
    queryFn: () =>
      apiFetch<PageResponse<ChallengeSummary>>(`${ApiPaths.CHALLENGES}?size=50`),
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

  const challenges = data?.content ?? [];
  const filteredChallenges = useMemo(
    () =>
      challenges.filter((c) =>
        matchesProgressFilter(progressBySlug.get(c.slug), filter),
      ),
    [challenges, filter, progressBySlug],
  );

  const progressStats = useMemo(() => {
    const entries = progressQuery.data ?? [];
    return {
      passed: entries.filter((e) => e.state === ProgressState.PASSED).length,
      attempted: entries.filter((e) => e.state === ProgressState.ATTEMPTED).length,
      failed: entries.filter((e) => e.state === ProgressState.FAILED).length,
      total: challenges.length,
    };
  }, [progressQuery.data, challenges.length]);

  const headerExtra =
    progressQuery.data && challenges.length > 0 ? (
      <div className="flex flex-wrap gap-2">
        <Tag color="success" className="!m-0 !px-3 !py-1">
          {progressStats.passed} passed
        </Tag>
        <Tag color="processing" className="!m-0 !px-3 !py-1">
          {progressStats.attempted} in progress
        </Tag>
        <Tag color="error" className="!m-0 !px-3 !py-1">
          {progressStats.failed} needs work
        </Tag>
      </div>
    ) : null;

  return (
    <AppLayout>
      <PageHeader
        title="Challenges"
        description="Pick a challenge, run tests in Docker, then review AI feedback at the bottom of the workspace."
        extra={headerExtra}
      />

      {isLoading && (
        <div className="flex justify-center py-20" role="status" aria-live="polite">
          <Spin size="large" tip="Loading challenges…" />
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
        <CtlCard
          title="Your challenges"
          extra={
            <Typography.Text className="!text-slate-500 text-sm">
              {filteredChallenges.length} of {challenges.length}
            </Typography.Text>
          }
        >
          <div className="ctl-segmented mb-6">
            <Segmented
              block
              className="max-w-xl"
              value={filter}
              onChange={(v) => setFilter(v as ProgressFilter)}
              options={[
                { label: "All", value: "all" },
                { label: "Not started", value: "not_started" },
                { label: "In progress", value: "active" },
                { label: "Passed", value: "passed" },
              ]}
            />
          </div>

          {filteredChallenges.length === 0 ? (
            <Empty
              className="py-10"
              description={
                <span className="text-slate-400">
                  No challenges match this filter.{" "}
                  <button
                    type="button"
                    className="text-emerald-400 hover:text-emerald-300 bg-transparent border-0 p-0 cursor-pointer underline"
                    onClick={() => setFilter("all")}
                  >
                    Show all
                  </button>
                </span>
              }
            />
          ) : (
            <ul className="m-0 grid list-none gap-4 p-0 sm:grid-cols-2 lg:grid-cols-3">
              {filteredChallenges.map((c) => (
                <li key={c.slug} className="min-h-[140px]">
                  <ChallengeCard
                    challenge={c}
                    progressState={progressBySlug.get(c.slug)}
                  />
                </li>
              ))}
            </ul>
          )}
        </CtlCard>
      )}

      {!isLoading && !error && challenges.length === 0 && data && (
        <Empty
          className="py-16"
          description={
            <span className="text-slate-400">
              No challenges published yet. Check back soon or contact your admin.
            </span>
          }
        />
      )}
    </AppLayout>
  );
}
