export type ThemeMode = "light" | "dark";

const STORAGE_KEY = "ctl-theme";

export function readStoredTheme(): ThemeMode | null {
  try {
    const value = localStorage.getItem(STORAGE_KEY);
    if (value === "light" || value === "dark") {
      return value;
    }
  } catch {
    /* private mode / blocked storage */
  }
  return null;
}

export function storeTheme(mode: ThemeMode): void {
  try {
    localStorage.setItem(STORAGE_KEY, mode);
  } catch {
    /* ignore */
  }
}

export function resolveInitialTheme(): ThemeMode {
  const stored = readStoredTheme();
  if (stored) {
    return stored;
  }
  return "dark";
}

export function themeColorMeta(mode: ThemeMode): string {
  return mode === "light" ? "#f8fafc" : "#0f172a";
}
