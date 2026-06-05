import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useMemo, useState } from "react";
import { apiFetch, ApiError } from "../api/client";
import type { ChallengeSessionResponse } from "../api/types";
import { ApiPaths } from "../domain/constants";
import { formatSessionCountdown, sessionDurationSeconds } from "../utils/challengeSession";

export function useServerChallengeSession(
  slug: string,
  sessionDurationMinutes: number,
  difficulty: string,
  enabled: boolean,
) {
  const queryClient = useQueryClient();
  const limitSeconds = sessionDurationSeconds(sessionDurationMinutes, difficulty);
  const [nowMs, setNowMs] = useState(() => Date.now());

  const sessionQuery = useQuery({
    queryKey: ["challenge-session", slug],
    queryFn: async () => {
      try {
        return await apiFetch<ChallengeSessionResponse>(ApiPaths.challengeSession(slug));
      } catch (error) {
        if (error instanceof ApiError && error.status === 404) {
          return null;
        }
        throw error;
      }
    },
    enabled: enabled && Boolean(slug),
  });

  const session = sessionQuery.data ?? null;

  useEffect(() => {
    if (!session || session.expired) {
      return;
    }
    const id = window.setInterval(() => setNowMs(Date.now()), 1000);
    return () => window.clearInterval(id);
  }, [session]);

  const remainingSec = useMemo(() => {
    if (!session) {
      return limitSeconds;
    }
    if (session.expired) {
      return 0;
    }
    const expiresAt = new Date(session.expiresAt).getTime();
    return Math.max(0, Math.floor((expiresAt - nowMs) / 1000));
  }, [session, nowMs, limitSeconds]);

  const startMutation = useMutation({
    mutationFn: () =>
      apiFetch<ChallengeSessionResponse>(ApiPaths.challengeSessionStart(slug), { method: "POST" }),
    onSuccess: (data) => {
      queryClient.setQueryData(["challenge-session", slug], data);
    },
  });

  const abandonMutation = useMutation({
    mutationFn: () =>
      apiFetch<void>(ApiPaths.challengeSessionAbandon(slug), { method: "POST" }),
    onSuccess: () => {
      queryClient.setQueryData(["challenge-session", slug], null);
      void queryClient.invalidateQueries({ queryKey: ["challenge-session", slug] });
    },
  });

  const startSession = useCallback(async () => {
    if (!enabled) {
      return;
    }
    await startMutation.mutateAsync();
  }, [enabled, startMutation]);

  const abandonSession = useCallback(async () => {
    if (!enabled) {
      return;
    }
    await abandonMutation.mutateAsync();
  }, [abandonMutation, enabled]);

  const active = session != null && !session.expired;
  const expired = session != null && (session.expired || remainingSec <= 0);

  return {
    active,
    expired,
    limitSeconds,
    remainingSec,
    formattedRemaining: formatSessionCountdown(remainingSec),
    startSession,
    abandonSession,
    isStarting: startMutation.isPending,
    isAbandoning: abandonMutation.isPending,
    sessionError: startMutation.error ?? abandonMutation.error ?? sessionQuery.error,
  };
}
