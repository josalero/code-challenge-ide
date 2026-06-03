import { difficultyColor } from "@/utils/difficulty";

const DIFFICULTY_BADGE: Record<string, string> = {
  green: "border-emerald-500/40 text-emerald-700 dark:text-emerald-400",
  orange: "border-amber-500/40 text-amber-700 dark:text-amber-400",
  red: "border-red-500/40 text-red-700 dark:text-red-400",
  default: "border-slate-500/40 text-slate-600 dark:text-slate-400",
};

export function difficultyColorClass(difficulty: string): string {
  const color = difficultyColor(difficulty);
  return DIFFICULTY_BADGE[color] ?? DIFFICULTY_BADGE.default;
}
