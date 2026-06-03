import type { LanguageRuntimeOption } from "../api/types";

export function activeLanguages(runtimes: LanguageRuntimeOption[]): string[] {
  const names = new Set<string>();
  for (const entry of runtimes) {
    if (entry.active) {
      names.add(entry.language);
    }
  }
  return [...names].sort(compareLanguages);
}

/** Ascending compare for dotted/numeric runtime versions (17 < 21 < 26, 5.7 < 5.10). */
export function compareRuntimeVersions(left: string, right: string): number {
  const leftParts = left.split(/[.\-_+]/);
  const rightParts = right.split(/[.\-_+]/);
  const length = Math.max(leftParts.length, rightParts.length);
  for (let index = 0; index < length; index++) {
    const leftPart = leftParts[index] ?? "";
    const rightPart = rightParts[index] ?? "";
    const leftNumber = parseVersionPart(leftPart);
    const rightNumber = parseVersionPart(rightPart);
    if (leftNumber != null && rightNumber != null && leftNumber !== rightNumber) {
      return leftNumber - rightNumber;
    }
    if (leftPart !== rightPart) {
      return leftPart.localeCompare(rightPart, undefined, { numeric: true, sensitivity: "base" });
    }
  }
  return left.localeCompare(right, undefined, { numeric: true, sensitivity: "base" });
}

function parseVersionPart(part: string): number | null {
  const match = /^(\d+)/.exec(part);
  if (!match) {
    return null;
  }
  const value = Number.parseInt(match[1], 10);
  return Number.isFinite(value) ? value : null;
}

export function sortRuntimesByVersionDesc<T extends { version: string }>(
  runtimes: T[],
): T[] {
  return [...runtimes].sort((left, right) =>
    compareRuntimeVersions(right.version, left.version),
  );
}

export function latestActiveRuntimeVersion(
  runtimes: { version: string; active: boolean }[],
  fallback = "",
): string {
  const newest = sortRuntimesByVersionDesc(runtimes.filter((runtime) => runtime.active))[0];
  return newest?.version ?? fallback;
}

export function activeRuntimesForLanguage(
  runtimes: LanguageRuntimeOption[],
  language: string,
): LanguageRuntimeOption[] {
  return sortRuntimesByVersionDesc(
    runtimes.filter((r) => r.active && r.language === language),
  );
}

export function formatLanguageLabel(language: string): string {
  if (!language) {
    return language;
  }
  if (language === "csharp") {
    return "C#";
  }
  if (language === "cpp") {
    return "C++";
  }
  if (language === "node") {
    return "Node.js";
  }
  if (language === "sql") {
    return "SQL";
  }
  return language.charAt(0).toUpperCase() + language.slice(1);
}

/** Stable order for challenge list sections (known languages first). */
const LANGUAGE_SORT_ORDER = [
  "java",
  "python",
  "sql",
  "typescript",
  "react",
  "vue",
  "angular",
  "go",
  "node",
  "csharp",
  "rust",
  "cpp",
];

export function compareLanguages(a: string, b: string): number {
  const ai = LANGUAGE_SORT_ORDER.indexOf(a);
  const bi = LANGUAGE_SORT_ORDER.indexOf(b);
  if (ai >= 0 && bi >= 0) {
    return ai - bi;
  }
  if (ai >= 0) {
    return -1;
  }
  if (bi >= 0) {
    return 1;
  }
  return a.localeCompare(b);
}

export function languageTagColor(language: string): string {
  switch (language) {
    case "java":
      return "blue";
    case "python":
      return "gold";
    case "sql":
      return "cyan";
    case "typescript":
      return "geekblue";
    case "go":
      return "cyan";
    case "node":
      return "green";
    case "csharp":
      return "purple";
    case "rust":
      return "volcano";
    case "cpp":
      return "orange";
    case "react":
      return "geekblue";
    case "vue":
      return "green";
    case "angular":
      return "red";
    default:
      return "default";
  }
}

export function formatRuntimeLabel(language: string, version: string): string {
  return `${formatLanguageLabel(language)} ${version}`;
}

const RUNNER_PIPELINE: Record<string, string> = {
  java: "compile · JUnit · coverage",
  python: "pytest · coverage",
  sql: "PostgreSQL · result sets",
  go: "go test · coverage",
  node: "node:test · coverage",
  typescript: "tsc · node:test · coverage",
  csharp: "dotnet test · coverage",
  rust: "cargo test · coverage",
  cpp: "CMake · Catch2",
  react: "Vitest · Testing Library",
  vue: "Vitest · Vue Test Utils",
  angular: "Vitest · pipes & services",
};

export function runnerPipelineLabel(language: string): string {
  return RUNNER_PIPELINE[language] ?? "tests · coverage · lint";
}

export function runnerWarmupHint(language: string): string {
  if (language === "java") {
    return "If the runner pool is cold, the first Java run may take longer while Docker starts the sandbox. Warm the pool from Admin → Ops for faster first runs.";
  }
  if (language === "csharp" || language === "cpp" || language === "rust") {
    return "Compiling native code in Docker can take a minute on a cold pool. Admin → Ops → Warm runner pool preloads this.";
  }
  return "Docker is starting the sandbox — warm the runner pool from Admin → Ops to skip this on first run.";
}
