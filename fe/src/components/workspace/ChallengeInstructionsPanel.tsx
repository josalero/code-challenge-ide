import { BookOpen, Table2 } from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { ChallengeDetail } from "@/api/types";
import {
  resolveChallengeExamples,
  stripExamplesFromDescription,
} from "@/utils/challengeExamples";
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
  const exampleRows = resolveChallengeExamples(
    challenge.descriptionMd,
    challenge.publicTests,
    challenge.slug,
  );
  const descriptionMd =
    exampleRows.length > 0
      ? stripExamplesFromDescription(challenge.descriptionMd)
      : challenge.descriptionMd;

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

          {exampleRows.length > 0 && (
            <section aria-labelledby="examples-heading">
              <h2
                id="examples-heading"
                className="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-slate-300"
              >
                <Table2 className="size-3.5 text-sky-400/90" aria-hidden />
                Examples
              </h2>
              <div className="overflow-hidden rounded-lg border border-slate-700/60">
                <table className="w-full border-collapse text-left text-sm">
                  <thead>
                    <tr className="border-b border-slate-700/60 bg-slate-800/50 text-[10px] font-semibold uppercase tracking-wide text-slate-400">
                      <th className="px-3 py-2 font-medium">Input</th>
                      <th className="px-3 py-2 font-medium">Output</th>
                    </tr>
                  </thead>
                  <tbody>
                    {exampleRows.map((row) => (
                      <tr
                        key={`${row.input}-${row.output}`}
                        className="border-b border-slate-800/80 last:border-0"
                      >
                        <td className="px-3 py-2 font-mono text-xs text-slate-200">
                          {row.input}
                        </td>
                        <td className="px-3 py-2 font-mono text-xs text-emerald-300/90">
                          {row.output}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <p className="mt-2 text-xs text-slate-500">
                {challenge.slug === "anagram-groups"
                  ? "Group and word order may vary; tests compare normalized groups. Hidden tests may use other inputs."
                  : "Sample cases from the problem statement. Hidden tests may use other inputs."}
              </p>
            </section>
          )}

          <div className="ctl-workspace-prose whitespace-pre-wrap text-slate-300">
            {descriptionMd}
          </div>
        </div>
      </ScrollArea>
    </aside>
  );
}
