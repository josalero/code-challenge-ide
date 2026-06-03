import { useCallback, useEffect, useMemo, useState } from "react";
import {
  clearSessionStartedAt,
  formatSessionCountdown,
  readSessionStartedAt,
  sessionDurationSeconds,
  sessionStorageKey,
  writeSessionStartedAt,
} from "../utils/challengeSession";

export type ChallengeSessionTimer = {
  /** Session clock started (first run/submit or restored from storage). */
  active: boolean;
  expired: boolean;
  limitSeconds: number;
  remainingSec: number;
  formattedRemaining: string;
  startSession: () => void;
  /** Clears the timer so the next Run/Submit starts a full new limit; exits focus mode. */
  abandonSession: () => void;
};

export function useChallengeSessionTimer(
  slug: string,
  sessionDurationMinutes: number,
  difficulty: string,
  userKey: string,
): ChallengeSessionTimer {
  const storageKey = slug ? sessionStorageKey(slug, userKey) : "";
  const limitSeconds = sessionDurationSeconds(sessionDurationMinutes, difficulty);

  const [startedAt, setStartedAt] = useState<number | null>(() =>
    storageKey ? readSessionStartedAt(storageKey) : null,
  );
  const [nowMs, setNowMs] = useState(() => Date.now());

  useEffect(() => {
    if (!storageKey) {
      setStartedAt(null);
      return;
    }
    setStartedAt(readSessionStartedAt(storageKey));
  }, [storageKey]);

  useEffect(() => {
    if (startedAt == null) {
      return;
    }
    const id = window.setInterval(() => setNowMs(Date.now()), 1000);
    return () => window.clearInterval(id);
  }, [startedAt]);

  const startSession = useCallback(() => {
    if (!storageKey) {
      return;
    }
    const existing = readSessionStartedAt(storageKey);
    if (existing != null) {
      setStartedAt(existing);
      return;
    }
    const at = Date.now();
    writeSessionStartedAt(storageKey, at);
    setStartedAt(at);
  }, [storageKey]);

  const abandonSession = useCallback(() => {
    if (storageKey) {
      clearSessionStartedAt(storageKey);
    }
    setStartedAt(null);
    setNowMs(Date.now());
  }, [storageKey]);

  const remainingSec = useMemo(() => {
    if (startedAt == null) {
      return limitSeconds;
    }
    const elapsed = Math.floor((nowMs - startedAt) / 1000);
    return Math.max(0, limitSeconds - elapsed);
  }, [startedAt, nowMs, limitSeconds]);

  const expired = startedAt != null && remainingSec <= 0;
  const active = startedAt != null;

  return {
    active,
    expired,
    limitSeconds,
    remainingSec,
    formattedRemaining: formatSessionCountdown(remainingSec),
    startSession,
    abandonSession,
  };
}
