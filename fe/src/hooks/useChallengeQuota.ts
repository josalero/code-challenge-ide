import { useQuery } from "@tanstack/react-query";
import { apiFetch } from "../api/client";
import type { MeMetricsResponse } from "../api/types";
import { ApiPaths } from "../domain/constants";
import { ProgressState } from "../domain/constants";
import type { ProgressEntry } from "../api/types";

export function useChallengeQuota(enabled: boolean) {
  return useQuery({
    queryKey: ["me", "metrics"],
    queryFn: () => apiFetch<MeMetricsResponse>(ApiPaths.ME_METRICS),
    enabled,
    staleTime: 30_000,
  });
}

export function isChallengeStarted(
  progress: ProgressEntry[] | undefined,
  challengeSlug: string,
): boolean {
  const state = progress?.find((entry) => entry.challengeSlug === challengeSlug)?.state;
  return state != null && state !== ProgressState.NOT_STARTED;
}

export function isQuotaBlockedForNewChallenge(
  metrics: MeMetricsResponse | undefined,
  progress: ProgressEntry[] | undefined,
  challengeSlug: string,
): boolean {
  if (!metrics?.maxStartedChallenges) {
    return false;
  }
  if (isChallengeStarted(progress, challengeSlug)) {
    return false;
  }
  return (metrics.challengesRemaining ?? 0) <= 0;
}

export function challengeQuotaMessage(metrics: MeMetricsResponse): string {
  const max = metrics.maxStartedChallenges ?? 0;
  return `You can work on up to ${max} exercises at a time. Finish or continue one of your in-progress challenges before starting a new one.`;
}
