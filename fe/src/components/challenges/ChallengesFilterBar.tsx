import type { ReactNode } from "react";
import { Search, SlidersHorizontal, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import type { ProgressFilter } from "@/utils/challengeProgress";
import type { LanguageFilter } from "@/utils/challengeByLanguage";
import { formatLanguageLabel } from "@/utils/languageRuntimes";

const PROGRESS_OPTIONS: { value: ProgressFilter; label: string }[] = [
  { value: "all", label: "All" },
  { value: "not_started", label: "New" },
  { value: "active", label: "In progress" },
  { value: "passed", label: "Passed" },
];

type Props = {
  search: string;
  onSearchChange: (value: string) => void;
  languageFilter: LanguageFilter;
  onLanguageFilterChange: (value: LanguageFilter) => void;
  progressFilter: ProgressFilter;
  onProgressFilterChange: (value: ProgressFilter) => void;
  languages: string[];
  languageCounts: Map<string, number>;
  visibleCount: number;
  totalCount: number;
  onReset: () => void;
  hasActiveFilters: boolean;
};

export default function ChallengesFilterBar({
  search,
  onSearchChange,
  languageFilter,
  onLanguageFilterChange,
  progressFilter,
  onProgressFilterChange,
  languages,
  languageCounts,
  visibleCount,
  totalCount,
  onReset,
  hasActiveFilters,
}: Props) {
  return (
    <aside
      className="ctl-challenges-filters lg:sticky lg:top-[4.5rem] lg:max-h-[calc(100dvh-5rem)] lg:self-start lg:overflow-y-auto"
      role="search"
      aria-label="Filter challenges"
      data-learner-tour="challenge-filters"
    >
      <div className="rounded-xl border border-border bg-card p-4 shadow-sm dark:border-slate-600/40 dark:bg-slate-800/35 dark:shadow-none">
        <div className="mb-4 flex items-center justify-between gap-2">
          <div className="flex items-center gap-2 text-sm font-medium text-foreground">
            <SlidersHorizontal className="size-4 text-emerald-600 dark:text-emerald-400/90" aria-hidden />
            Filters
          </div>
          {hasActiveFilters && (
            <Button
              type="button"
              variant="ghost"
              size="sm"
              className="h-7 shrink-0 text-emerald-600 hover:text-emerald-700 dark:text-emerald-400 dark:hover:text-emerald-300"
              onClick={onReset}
            >
              Reset
            </Button>
          )}
        </div>

        <label className="relative mb-5 block">
          <span className="sr-only">Search challenges</span>
          <Search
            className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
            aria-hidden
          />
          <input
            type="search"
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search title or slug…"
            className="h-10 w-full rounded-lg border border-input bg-background py-2 pl-10 pr-10 text-sm text-foreground placeholder:text-muted-foreground focus:border-emerald-500/50 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 dark:border-slate-700/60 dark:bg-slate-900/80 dark:text-slate-100"
          />
          {search && (
            <button
              type="button"
              onClick={() => onSearchChange("")}
              className="absolute right-2 top-1/2 flex size-7 -translate-y-1/2 items-center justify-center rounded-md text-muted-foreground hover:bg-muted hover:text-foreground dark:hover:bg-slate-800 dark:hover:text-slate-200"
              aria-label="Clear search"
            >
              <X className="size-3.5" aria-hidden />
            </button>
          )}
        </label>

        <FilterSection label="Progress">
          <div
            className="flex flex-col gap-1"
            role="group"
            aria-label="Filter by progress"
          >
            {PROGRESS_OPTIONS.map(({ value, label }) => (
              <FilterChip
                key={value}
                active={progressFilter === value}
                onClick={() => onProgressFilterChange(value)}
                block
              >
                {label}
              </FilterChip>
            ))}
          </div>
        </FilterSection>

        <FilterSection label="Language" className="mt-5">
          <div className="ctl-challenges-filter-scroll flex max-h-[min(50vh,320px)] flex-col gap-1 overflow-y-auto pr-1">
            <FilterChip
              active={languageFilter === "all"}
              onClick={() => onLanguageFilterChange("all")}
              block
            >
              All languages
            </FilterChip>
            {languages.map((lang) => (
              <FilterChip
                key={lang}
                active={languageFilter === lang}
                onClick={() => onLanguageFilterChange(lang)}
                block
              >
                <span className="truncate">{formatLanguageLabel(lang)}</span>
                <span className="ml-auto shrink-0 text-muted-foreground group-aria-pressed:text-foreground">
                  {languageCounts.get(lang) ?? 0}
                </span>
              </FilterChip>
            ))}
          </div>
        </FilterSection>

        <div className="mt-5 rounded-lg border border-border bg-muted/50 px-3 py-2.5 text-sm dark:border-slate-600/40 dark:bg-slate-800/40">
          <p className="mb-0 text-muted-foreground">
            Showing{" "}
            <span className="font-semibold text-foreground">{visibleCount}</span>
            <span> / {totalCount}</span>
          </p>
        </div>
      </div>
    </aside>
  );
}

function FilterSection({
  label,
  children,
  className,
}: {
  label: string;
  children: ReactNode;
  className?: string;
}) {
  return (
    <div className={className}>
      <p className="mb-2 text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
        {label}
      </p>
      {children}
    </div>
  );
}

function FilterChip({
  active,
  onClick,
  children,
  block,
}: {
  active: boolean;
  onClick: () => void;
  children: ReactNode;
  block?: boolean;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "group rounded-md font-medium transition-colors",
        block
          ? "flex w-full items-center gap-2 px-3 py-2 text-left text-sm"
          : "shrink-0 px-3 py-2 text-sm",
        active
          ? "border border-border bg-muted text-foreground shadow-sm ring-1 ring-border/80"
          : "border border-transparent text-muted-foreground hover:border-border/60 hover:bg-muted/70 hover:text-foreground",
      )}
      aria-pressed={active}
    >
      {children}
    </button>
  );
}
