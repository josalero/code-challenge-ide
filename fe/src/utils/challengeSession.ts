/** Fallback when API does not send a configured limit (legacy data). */
export const EASY_SESSION_FALLBACK_MINUTES = 30;
export const STANDARD_SESSION_FALLBACK_MINUTES = 60;

export function sessionDurationSeconds(
  sessionDurationMinutes: number,
  difficulty?: string,
): number {
  if (sessionDurationMinutes > 0) {
    return sessionDurationMinutes * 60;
  }
  const fallbackMinutes =
    difficulty?.trim().toLowerCase() === "easy"
      ? EASY_SESSION_FALLBACK_MINUTES
      : STANDARD_SESSION_FALLBACK_MINUTES;
  return fallbackMinutes * 60;
}

export function sessionStorageKey(slug: string, userKey: string): string {
  return `ctl:challenge-session:${userKey}:${slug}`;
}

export function formatSessionLimitMinutes(minutes: number): string {
  const clamped = Math.max(0, minutes);
  return `${clamped}:00`;
}

export function formatSessionCountdown(remainingSec: number): string {
  const clamped = Math.max(0, remainingSec);
  const minutes = Math.floor(clamped / 60);
  const seconds = clamped % 60;
  return `${minutes}:${seconds.toString().padStart(2, "0")}`;
}

export function readSessionStartedAt(key: string): number | null {
  const raw = sessionStorage.getItem(key);
  if (!raw) {
    return null;
  }
  const parsed = Number(raw);
  return Number.isFinite(parsed) ? parsed : null;
}

export function writeSessionStartedAt(key: string, startedAt: number): void {
  sessionStorage.setItem(key, String(startedAt));
}

export function clearSessionStartedAt(key: string): void {
  sessionStorage.removeItem(key);
}
