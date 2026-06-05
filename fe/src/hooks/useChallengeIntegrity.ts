import { useCallback, useEffect, useRef } from "react";
import { apiFetch } from "../api/client";
import type { ChallengeSessionResponse } from "../api/types";
import { ApiPaths } from "../domain/constants";
import type { IntegrityEventPayload } from "../utils/monacoClipboardGuard";
import { toWireCheckpoint } from "../utils/sessionCheckpoints";

type PendingEvent = IntegrityEventPayload & { occurredAt: string };

export function useChallengeIntegrity(slug: string, enabled: boolean) {
  const bufferRef = useRef<PendingEvent[]>([]);
  const flushingRef = useRef(false);

  const flush = useCallback(async () => {
    if (flushingRef.current || bufferRef.current.length === 0 || !slug) {
      return;
    }
    flushingRef.current = true;
    const batch = bufferRef.current.splice(0, bufferRef.current.length);
    try {
      await apiFetch<ChallengeSessionResponse>(ApiPaths.challengeSessionSync(slug), {
        method: "POST",
        body: JSON.stringify({
          clientMs: Date.now(),
          marks: batch.map(toWireCheckpoint),
        }),
      });
    } catch {
      bufferRef.current.unshift(...batch);
    } finally {
      flushingRef.current = false;
    }
  }, [slug]);

  const recordEvent = useCallback(
    (payload: IntegrityEventPayload) => {
      if (!enabled) {
        return;
      }
      bufferRef.current.push({
        ...payload,
        occurredAt: new Date().toISOString(),
      });
      if (bufferRef.current.length >= 8) {
        void flush();
      }
    },
    [enabled, flush],
  );

  useEffect(() => {
    if (!enabled) {
      void flush();
      return;
    }
    const interval = window.setInterval(() => {
      void flush();
    }, 12_000);
    return () => {
      window.clearInterval(interval);
      void flush();
    };
  }, [enabled, flush]);

  return { recordEvent, flush };
}

export function useTabVisibilityIntegrity(
  enabled: boolean,
  recordEvent: (payload: IntegrityEventPayload) => void,
) {
  const awayStartedAtRef = useRef<number | null>(null);

  useEffect(() => {
    if (!enabled) {
      awayStartedAtRef.current = null;
      return;
    }

    const markAway = (eventType: "TAB_HIDDEN" | "WINDOW_BLUR") => {
      if (awayStartedAtRef.current == null) {
        awayStartedAtRef.current = Date.now();
      }
      recordEvent({ eventType });
    };

    const markBack = (eventType: "TAB_VISIBLE" | "WINDOW_FOCUS") => {
      const awayMs =
        awayStartedAtRef.current == null ? undefined : Date.now() - awayStartedAtRef.current;
      awayStartedAtRef.current = null;
      recordEvent({ eventType, awayMs });
    };

    const onVisibilityChange = () => {
      if (document.visibilityState === "hidden") {
        markAway("TAB_HIDDEN");
      } else {
        markBack("TAB_VISIBLE");
      }
    };
    const onBlur = () => markAway("WINDOW_BLUR");
    const onFocus = () => markBack("WINDOW_FOCUS");

    document.addEventListener("visibilitychange", onVisibilityChange);
    window.addEventListener("blur", onBlur);
    window.addEventListener("focus", onFocus);
    return () => {
      document.removeEventListener("visibilitychange", onVisibilityChange);
      window.removeEventListener("blur", onBlur);
      window.removeEventListener("focus", onFocus);
    };
  }, [enabled, recordEvent]);
}
