import { BookOpen, ListChecks } from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { ChallengeDetail } from "@/api/types";
import { formatRuntimeLabel } from "@/utils/languageRuntimes";
import WorkspacePanelHeader from "./WorkspacePanelHeader";

type Props = {
  challenge: ChallengeDetail;
  runtimeVersion: string;
};

export default function ChallengeInstructionsPanel({
  challenge,
  runtimeVersion,
}: Props) {
  return (
    <aside
      className="flex h-full min-h-0 w-full flex-1 flex-col bg-slate-900/40"
      aria-label="Problem instructions"
    >
      <WorkspacePanelHeader
        icon={<BookOpen className="size-3.5" aria-hidden />}
        title="Problem"
        subtitle={formatRuntimeLabel(challenge.language, runtimeVersion)}
        className="shrink-0 border-b border-slate-800/80 bg-slate-900/60"
      />

      <ScrollArea className="min-h-0 flex-1">
        <div className="space-y-4 px-4 py-4">
          <div className="grid grid-cols-3 gap-2">
            <div className="rounded-lg border border-slate-700/50 bg-slate-800/40 px-2 py-2 text-center">
              <p className="text-base font-semibold text-slate-100">
                {challenge.publicTests.length}
              </p>
              <p className="text-[10px] uppercase tracking-wide text-slate-500">Public</p>
            </div>
            <div className="rounded-lg border border-slate-700/50 bg-slate-800/40 px-2 py-2 text-center">
              <p className="text-base font-semibold text-slate-100">
                {challenge.hiddenTestCount}
              </p>
              <p className="text-[10px] uppercase tracking-wide text-slate-500">Hidden</p>
            </div>
            <div className="rounded-lg border border-slate-700/50 bg-slate-800/40 px-2 py-2 text-center">
              <p className="truncate text-xs font-semibold text-emerald-400">
                {challenge.difficulty}
              </p>
              <p className="text-[10px] uppercase tracking-wide text-slate-500">Level</p>
            </div>
          </div>

          <div className="ctl-workspace-prose whitespace-pre-wrap">{challenge.descriptionMd}</div>

          {challenge.publicTests.length > 0 && (
            <section aria-labelledby="public-tests-heading">
              <h2
                id="public-tests-heading"
                className="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-slate-400"
              >
                <ListChecks className="size-3.5 text-emerald-400/80" aria-hidden />
                Public tests ({challenge.publicTests.length})
              </h2>
              <ul className="space-y-2">
                {challenge.publicTests.map((test, index) => (
                  <li key={test.name} className="ctl-workspace-test-card">
                    <div className="flex items-start gap-2">
                      <span className="mt-0.5 flex size-5 shrink-0 items-center justify-center rounded-full bg-slate-800 text-[10px] font-medium text-slate-400 ring-1 ring-slate-600/60">
                        {index + 1}
                      </span>
                      <div className="min-w-0">
                        <p className="font-medium text-slate-100">{test.name}</p>
                        {test.description ? (
                          <p className="mt-1 text-sm leading-relaxed text-slate-400">
                            {test.description}
                          </p>
                        ) : null}
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </section>
          )}
        </div>
      </ScrollArea>
    </aside>
  );
}
