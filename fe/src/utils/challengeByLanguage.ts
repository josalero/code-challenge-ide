import type { ChallengeSummary } from "../api/types";
import { compareLanguages } from "./languageRuntimes";

export type LanguageFilter = "all" | string;

export function uniqueLanguages(challenges: ChallengeSummary[]): string[] {
  const names = new Set(challenges.map((c) => c.language).filter(Boolean));
  return [...names].sort(compareLanguages);
}

export function groupChallengesByLanguage(
  challenges: ChallengeSummary[],
): { language: string; challenges: ChallengeSummary[] }[] {
  const byLang = new Map<string, ChallengeSummary[]>();
  for (const challenge of challenges) {
    const lang = challenge.language || "unknown";
    const list = byLang.get(lang) ?? [];
    list.push(challenge);
    byLang.set(lang, list);
  }
  return [...byLang.entries()]
    .sort(([a], [b]) => compareLanguages(a, b))
    .map(([language, items]) => ({
      language,
      challenges: items.sort((a, b) => a.title.localeCompare(b.title)),
    }));
}

export function filterByLanguage(
  challenges: ChallengeSummary[],
  languageFilter: LanguageFilter,
): ChallengeSummary[] {
  if (languageFilter === "all") {
    return challenges;
  }
  return challenges.filter((c) => c.language === languageFilter);
}
