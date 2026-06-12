import type { ReactNode } from "react";
import { BookOpen, Table2 } from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import type { ChallengeDetail } from "@/api/types";
import {
  examplesUseReferenceLayout,
  resolveChallengeExamples,
  stripExamplesFromDescription,
} from "@/utils/challengeExamples";
import { formatRuntimeLabel } from "@/utils/languageRuntimes";
import { cn } from "@/lib/utils";
import ChallengeDescriptionMarkdown from "./ChallengeDescriptionMarkdown";
import WorkspacePanelHeader from "./WorkspacePanelHeader";

function ProblemStatTile({
  label,
  tooltip,
  children,
}: {
  label: string;
  tooltip: string;
  children: ReactNode;
}) {
  return (
    <Tooltip>
      <TooltipTrigger
        render={
          <div
            tabIndex={0}
            className={cn(
              "rounded-lg border border-slate-700/50 bg-slate-800/40 px-2 py-2 text-center",
              "cursor-help outline-none transition-colors hover:border-slate-600/70 hover:bg-slate-800/70",
              "focus-visible:ring-2 focus-visible:ring-emerald-500/50",
            )}
            aria-label={`${label}: ${tooltip}`}
          />
        }
      >
        {children}
        <p className="text-[10px] uppercase tracking-wide text-slate-500">{label}</p>
      </TooltipTrigger>
      <TooltipContent side="bottom" className="max-w-[220px] text-center">
        {tooltip}
      </TooltipContent>
    </Tooltip>
  );
}

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
  const referenceLayout = examplesUseReferenceLayout(exampleRows, challenge.language);
  const showPublicTestsReference =
    exampleRows.length === 0 && challenge.publicTests.length > 0;
  const descriptionMd =
    exampleRows.length > 0
      ? stripExamplesFromDescription(challenge.descriptionMd)
      : challenge.descriptionMd;

  const timedLimit = challenge.sessionDurationMinutes;
  const levelTooltip =
    timedLimit > 0
      ? `${challenge.difficulty} challenge — ${timedLimit}-minute timed attempt after you press Start test (see Guide on the right).`
      : `${challenge.difficulty} challenge — sets expectations for complexity and test strictness.`;

  return (
    <aside
      className="flex h-full min-h-0 w-full flex-1 flex-col bg-slate-900/40"
      aria-label="Problem instructions"
      data-learner-tour="problem-panel"
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
            <ProblemStatTile
              label="Public"
              tooltip="Visible test cases from the problem and Examples. Run executes these so you can debug with known inputs and outputs."
            >
              <p className="text-base font-semibold text-slate-100">
                {challenge.publicTests.length}
              </p>
            </ProblemStatTile>
            <ProblemStatTile
              label="Hidden"
              tooltip="Extra tests run on Submit only. Their inputs and expected outputs are not shown — they verify edge cases and full correctness."
            >
              <p className="text-base font-semibold text-slate-100">
                {challenge.hiddenTestCount}
              </p>
            </ProblemStatTile>
            <ProblemStatTile label="Level" tooltip={levelTooltip}>
              <p className="truncate text-xs font-semibold text-emerald-400">
                {challenge.difficulty}
              </p>
            </ProblemStatTile>
          </div>

          {exampleRows.length > 0 && (
            <section aria-labelledby="examples-heading">
              <h2
                id="examples-heading"
                className="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-slate-300"
              >
                <Table2 className="size-3.5 text-sky-400/90" aria-hidden />
                {referenceLayout ? "Public checks (reference)" : "Examples"}
              </h2>
              <div className="overflow-x-auto rounded-lg border border-slate-700/60">
                <table className="w-full min-w-[20rem] border-collapse text-left text-sm">
                  <thead>
                    <tr className="border-b border-slate-700/60 bg-slate-800/50 text-[10px] font-semibold uppercase tracking-wide text-slate-400">
                      <th className="px-3 py-2 font-medium">
                        {referenceLayout ? "Check" : "Input"}
                      </th>
                      <th className="px-3 py-2 font-medium">
                        {referenceLayout ? "Run verifies" : "Output"}
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {exampleRows.map((row) => (
                      <tr
                        key={`${row.input}-${row.output}-${row.label ?? ""}`}
                        className="border-b border-slate-800/80 last:border-0"
                      >
                        <td className="break-all px-3 py-2 font-mono text-xs text-slate-200">
                          {row.label ? (
                            <span>
                              <span className="font-sans text-slate-400">{row.label}: </span>
                              {row.input}
                            </span>
                          ) : (
                            row.input
                          )}
                        </td>
                        <td className="break-all px-3 py-2 font-mono text-xs text-emerald-300/90">
                          {row.output}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <p className="mt-2 text-xs text-slate-500">
                {referenceLayout
                  ? "Run executes these public checks against your query on the sample schema. Submit adds hidden cases."
                  : challenge.slug === "anagram-groups"
                    ? "Group and word order may vary; tests compare normalized groups. Hidden tests may use other inputs."
                    : "Sample cases from the problem statement. Hidden tests may use other inputs."}
              </p>
            </section>
          )}

          {showPublicTestsReference && (
            <section aria-labelledby="public-tests-ref-heading">
              <h2
                id="public-tests-ref-heading"
                className="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-slate-300"
              >
                <Table2 className="size-3.5 text-sky-400/90" aria-hidden />
                Public tests (reference)
              </h2>
              <ul className="m-0 list-none space-y-2 rounded-lg border border-slate-700/60 bg-slate-800/30 p-3">
                {challenge.publicTests.map((test) => (
                  <li
                    key={test.name}
                    className="border-b border-slate-800/80 pb-2 text-sm last:border-0 last:pb-0"
                  >
                    <p className="mb-0.5 font-mono text-xs font-medium text-slate-200">
                      {test.name}
                    </p>
                    {test.description ? (
                      <p className="mb-0 text-xs leading-relaxed text-slate-400">
                        {test.description}
                      </p>
                    ) : (
                      <p className="mb-0 text-xs italic text-slate-500">
                        Visible when you Run — checks your solution against the seeded database.
                      </p>
                    )}
                  </li>
                ))}
              </ul>
              <p className="mt-2 text-xs text-slate-500">
                No worked input/output examples for this problem. Use the checks above as a guide;
                hidden tests on Submit may assert stricter result sets.
              </p>
            </section>
          )}

          <ChallengeDescriptionMarkdown markdown={descriptionMd} />
        </div>
      </ScrollArea>
    </aside>
  );
}
