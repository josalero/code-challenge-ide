import { Button, Tag, Tooltip } from "antd";
import { Flame, HelpCircle } from "lucide-react";
import { Fragment } from "react";
import type { LanguageWarmStatus } from "@/api/types";
import { cn } from "@/lib/utils";
import {
  languageStateLabel,
  languageStateSummary,
  RUNNER_ONLY_WARM_LANGUAGES,
  type LanguageWarmGroup,
} from "./opsWarmUtils";

function runtimeStatus(
  ready: boolean | null,
  present: boolean,
  kind: "runner" | "editor",
): { label: string; color: "success" | "warning" | "error" | "default" } {
  if (kind === "runner" && !present) {
    return { label: "Image missing", color: "error" };
  }
  if (ready === true) {
    return { label: "Ready", color: "success" };
  }
  if (ready === false) {
    return { label: "Cold", color: "warning" };
  }
  return { label: "N/A", color: "default" };
}

function RuntimeRow({
  row,
  runnerOnly,
}: {
  row: LanguageWarmStatus;
  runnerOnly: boolean;
}) {
  const runTests = runtimeStatus(row.runnerReady, row.runnerPresent, "runner");
  const editor = runnerOnly
    ? { label: "N/A", color: "default" as const }
    : runtimeStatus(row.editorReady, true, "editor");

  return (
    <tr className="border-t border-border">
      <td className="py-2 pl-10 pr-2 text-sm text-muted-foreground">{row.label}</td>
      <td className="px-2 py-2">
        <Tag color={runTests.color} className="!m-0 !text-[11px]">
          {runTests.label}
        </Tag>
      </td>
      <td className="px-2 py-2">
        <Tag color={editor.color} className="!m-0 !text-[11px]">
          {editor.label}
        </Tag>
      </td>
      <td className="py-2 pr-2 text-xs text-muted-foreground">
        {!row.runnerPresent && row.runnerImage ? (
          <span className="font-mono" title={row.runnerImage}>
            Run <code className="text-[10px]">make runners</code>
          </span>
        ) : (
          <span className="sr-only">—</span>
        )}
      </td>
    </tr>
  );
}

type Props = {
  groups: LanguageWarmGroup[];
  disabled: boolean;
  warmPending: boolean;
  onWarmLanguage: (language: string) => void;
  onWarmAll: (force: boolean) => void;
};

export default function LanguageWarmList({
  groups,
  disabled,
  warmPending,
  onWarmLanguage,
  onWarmAll,
}: Props) {
  return (
    <div className="space-y-4">
      <div className="rounded-lg border border-border bg-muted/30 p-4">
        <h2 className="text-sm font-semibold text-foreground">Warm up</h2>
        <p className="mt-1 text-sm text-muted-foreground">
          Preloads Docker containers so{" "}
          <strong className="font-medium text-foreground">Run tests</strong> and{" "}
          <strong className="font-medium text-foreground">IntelliSense</strong>{" "}
          respond quickly. One job runs runner smoke tests, then editor language servers.
          Java also warms the Maven cache when needed.
        </p>
        <div className="mt-4 flex flex-wrap gap-2">
          <Button
            type="primary"
            size="middle"
            icon={<Flame className="size-4" aria-hidden />}
            disabled={disabled}
            loading={warmPending}
            onClick={() => onWarmAll(false)}
          >
            Warm everything
          </Button>
          <Tooltip title="Use after make runners or when image tags changed. Re-runs warm even if already marked ready.">
            <Button
              disabled={disabled}
              loading={warmPending}
              onClick={() => onWarmAll(true)}
            >
              Re-warm everything
            </Button>
          </Tooltip>
        </div>
        <p className="mt-2 text-xs text-muted-foreground">
          <strong className="font-medium text-foreground">Warm everything</strong> skips
          languages already warm for the current image.{" "}
          <strong className="font-medium text-foreground">Re-warm</strong> forces a full pass.
          A running pool container counts as ready even if the saved image stamp is stale after{" "}
          <code className="text-[10px]">docker build</code>.
        </p>
      </div>

      <div className="overflow-hidden rounded-lg border border-border">
        <table className="w-full border-collapse text-left text-sm">
          <thead>
            <tr className="bg-muted text-[10px] font-semibold uppercase tracking-wide text-muted-foreground">
              <th className="px-3 py-2.5 font-medium">Language</th>
              <th className="px-2 py-2.5 font-medium">Run tests</th>
              <th className="px-2 py-2.5 font-medium">IntelliSense</th>
              <th className="py-2.5 pr-3 font-medium">
                <span className="inline-flex items-center gap-1">
                  Action
                  <Tooltip title="Warms every active runtime version for that language (runner + editor).">
                    <HelpCircle className="size-3 text-muted-foreground" aria-hidden />
                  </Tooltip>
                </span>
              </th>
            </tr>
          </thead>
          <tbody>
            {groups.map((group) => {
              const stateBorder =
                group.state === "ready"
                  ? "border-l-emerald-500/60"
                  : group.state === "partial"
                    ? "border-l-amber-500/60"
                    : group.state === "missing"
                      ? "border-l-red-500/40"
                      : "border-l-border";

              return (
                <Fragment key={group.language}>
                  <tr
                    className={cn(
                      "border-l-2 bg-muted/20",
                      stateBorder,
                    )}
                  >
                    <td className="px-3 py-3 align-top">
                      <div className="flex items-center gap-2">
                        <span
                          className={cn(
                            "size-2 shrink-0 rounded-full",
                            group.state === "ready" && "bg-emerald-500 dark:bg-emerald-400",
                            group.state === "partial" && "bg-amber-500 dark:bg-amber-400",
                            group.state === "cold" && "bg-muted-foreground/50",
                            group.state === "missing" && "bg-red-500/70",
                          )}
                          aria-hidden
                        />
                        <div>
                          <span className="font-medium capitalize text-foreground">
                            {group.language}
                          </span>
                          <p className="mt-0.5 text-xs text-muted-foreground">
                            {languageStateLabel(group.state)}
                            {group.runtimes.length > 0
                              ? ` · ${group.runtimes.length} runtime${group.runtimes.length === 1 ? "" : "s"}`
                              : ""}
                          </p>
                        </div>
                      </div>
                    </td>
                    <td colSpan={2} className="px-2 py-3 align-top text-xs text-muted-foreground">
                      {languageStateSummary(group)}
                    </td>
                    <td className="py-3 pr-3 align-top">
                      <Button
                        size="small"
                        disabled={disabled || group.state === "missing"}
                        onClick={() => onWarmLanguage(group.language)}
                      >
                        Warm
                      </Button>
                    </td>
                  </tr>
                  {group.runtimes.map((row) => (
                    <RuntimeRow
                      key={`${row.language}-${row.version ?? row.label}-${row.runnerImage ?? ""}`}
                      row={row}
                      runnerOnly={RUNNER_ONLY_WARM_LANGUAGES.has(group.language)}
                    />
                  ))}
                </Fragment>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
