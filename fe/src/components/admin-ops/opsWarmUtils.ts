import type { LanguageWarmStatus } from "@/api/types";

export const WARM_LANGUAGES = [
  "java",
  "python",
  "go",
  "node",
  "typescript",
  "csharp",
  "rust",
  "cpp",
  "react",
  "vue",
  "angular",
  "sql",
] as const;

/** Languages warmed via runner image only (no LSP / IntelliSense). */
export const RUNNER_ONLY_WARM_LANGUAGES = new Set<string>(["sql"]);

export type LanguageWarmChipState = "ready" | "partial" | "cold" | "missing";

export type LanguageWarmGroup = {
  language: string;
  runtimes: LanguageWarmStatus[];
  state: LanguageWarmChipState;
};

export const JOB_TYPE_LABELS: Record<string, string> = {
  INFRA_WARM: "Warm runners & editor",
  RUNNER_POOL_WARM: "Runner pool",
  MAVEN_WARM: "Maven cache",
  LSP_WARM: "Editor language servers",
};

export function languageWarmChipState(
  rows: LanguageWarmStatus[],
  language: string,
): LanguageWarmChipState {
  const langRows = rows.filter((row) => row.language === language);
  if (langRows.length === 0) {
    return "missing";
  }
  if (langRows.every((row) => row.ready)) {
    return "ready";
  }
  if (langRows.some((row) => row.runnerReady || row.editorReady)) {
    return "partial";
  }
  return "cold";
}

export function groupLanguagesByWarmState(rows: LanguageWarmStatus[]): LanguageWarmGroup[] {
  return WARM_LANGUAGES.map((language) => ({
    language,
    runtimes: rows.filter((row) => row.language === language),
    state: languageWarmChipState(rows, language),
  }));
}

export function languageStateLabel(state: LanguageWarmChipState): string {
  switch (state) {
    case "ready":
      return "Ready";
    case "partial":
      return "Partially warmed";
    case "missing":
      return "No runtime configured";
    default:
      return "Not warmed";
  }
}

export function languageStateSummary(group: LanguageWarmGroup): string {
  const { runtimes, state } = group;
  if (runtimes.length === 0) {
    return "No active runtime in the catalog for this language.";
  }
  const readyCount = runtimes.filter((r) => r.ready).length;
  const runnerOnly = RUNNER_ONLY_WARM_LANGUAGES.has(group.language);
  if (state === "ready") {
    return runnerOnly
      ? `All ${runtimes.length} runtime${runtimes.length === 1 ? "" : "s"} ready for Run tests (no IntelliSense for SQL).`
      : `All ${runtimes.length} runtime${runtimes.length === 1 ? "" : "s"} ready for Run tests and IntelliSense.`;
  }
  if (state === "partial") {
    return `${readyCount} of ${runtimes.length} runtime${runtimes.length === 1 ? "" : "s"} fully ready — warm again or check missing Docker images.`;
  }
  if (state === "missing") {
    return "Add an active runtime version in the database to enable warm-up.";
  }
  return "First Run tests or IntelliSense for this language will be slower until you warm it.";
}
