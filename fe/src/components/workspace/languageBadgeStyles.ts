import { languageTagColor } from "@/utils/languageRuntimes";

const LANGUAGE_BADGE: Record<string, string> = {
  blue: "border-blue-500/35 bg-blue-500/10 text-blue-300",
  gold: "border-amber-500/35 bg-amber-500/10 text-amber-300",
  geekblue: "border-indigo-500/35 bg-indigo-500/10 text-indigo-300",
  cyan: "border-cyan-500/35 bg-cyan-500/10 text-cyan-300",
  green: "border-emerald-500/35 bg-emerald-500/10 text-emerald-300",
  purple: "border-purple-500/35 bg-purple-500/10 text-purple-300",
  volcano: "border-orange-500/35 bg-orange-500/10 text-orange-300",
  orange: "border-orange-500/35 bg-orange-500/10 text-orange-300",
  red: "border-red-500/35 bg-red-500/10 text-red-300",
  default: "border-slate-500/35 bg-slate-500/10 text-slate-300",
};

export function languageBadgeClass(language: string): string {
  return LANGUAGE_BADGE[languageTagColor(language)] ?? LANGUAGE_BADGE.default;
}
