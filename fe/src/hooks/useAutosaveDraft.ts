import { useCallback, useEffect, useRef, useState } from "react";

export type AutosaveStatus = "idle" | "pending" | "saved" | "error";

const STORAGE_PREFIX = "ctl:draft:";

export function useAutosaveDraft(slug: string, code: string, enabled: boolean) {
  const [status, setStatus] = useState<AutosaveStatus>("idle");
  const timerRef = useRef<number | null>(null);
  const lastSavedRef = useRef<string | null>(null);

  const storageKey = `${STORAGE_PREFIX}${slug}`;

  const loadDraft = useCallback((): string | null => {
    try {
      return localStorage.getItem(storageKey);
    } catch {
      return null;
    }
  }, [storageKey]);

  useEffect(() => {
    if (!enabled || !slug) {
      return;
    }
    if (timerRef.current) {
      window.clearTimeout(timerRef.current);
    }
    if (code === lastSavedRef.current) {
      return;
    }
    setStatus("pending");
    timerRef.current = window.setTimeout(() => {
      try {
        localStorage.setItem(storageKey, code);
        lastSavedRef.current = code;
        setStatus("saved");
      } catch {
        setStatus("error");
      }
    }, 800);
    return () => {
      if (timerRef.current) {
        window.clearTimeout(timerRef.current);
      }
    };
  }, [code, enabled, slug, storageKey]);

  const clearDraft = useCallback(() => {
    try {
      localStorage.removeItem(storageKey);
      lastSavedRef.current = null;
      setStatus("idle");
    } catch {
      setStatus("error");
    }
  }, [storageKey]);

  return { status, loadDraft, clearDraft };
}
