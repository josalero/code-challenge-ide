import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Table, Tabs, Tag, Typography } from "antd";
import type { ReactNode } from "react";
import {
  Box,
  CheckCircle2,
  CircleAlert,
  Database,
  Flame,
  Loader2,
  RefreshCw,
  Terminal,
  XCircle,
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { apiFetch, ApiError } from "../api/client";
import type { RunnerImageStatus, RunnerOpsJob, RunnerOpsStatus, LanguageWarmStatus } from "../api/types";
import AppLayout from "../components/AppLayout";
import CtlCard from "../components/ui/CtlCard";
import PageHeader from "../components/ui/PageHeader";
import { ApiPaths } from "../domain/constants";
import { cn } from "../lib/utils";

const WARM_LANGUAGES = [
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
] as const;

const JOB_TYPE_LABELS: Record<string, string> = {
  INFRA_WARM: "Runners & editor",
  RUNNER_POOL_WARM: "Runner pool smoke",
  MAVEN_WARM: "Maven cache",
  LSP_WARM: "Language servers",
};

function builtTag(present: boolean) {
  return present ? (
    <Tag color="success">Image ready</Tag>
  ) : (
    <Tag color="error">Missing</Tag>
  );
}

function warmTag(warmed: boolean | null | undefined) {
  if (warmed == null) {
    return <Tag color="default">—</Tag>;
  }
  return warmed ? (
    <Tag color="success">Preloaded</Tag>
  ) : (
    <Tag color="warning">Not preloaded</Tag>
  );
}

function readyTag(ready: boolean) {
  return ready ? (
    <Tag color="success">Ready</Tag>
  ) : (
    <Tag color="warning">Needs warm</Tag>
  );
}

function jobStatusTag(status: string) {
  if (status === "RUNNING") {
    return <Tag color="processing">Running</Tag>;
  }
  if (status === "COMPLETED") {
    return <Tag color="success">Completed</Tag>;
  }
  if (status === "FAILED") {
    return <Tag color="error">Failed</Tag>;
  }
  return <Tag>{status}</Tag>;
}

function HealthTile({
  icon,
  label,
  value,
  detail,
  tone = "neutral",
}: {
  icon: ReactNode;
  label: string;
  value: string;
  detail?: string;
  tone?: "ok" | "warn" | "bad" | "neutral";
}) {
  const toneClass =
    tone === "ok"
      ? "border-emerald-500/25 bg-emerald-500/5"
      : tone === "warn"
        ? "border-amber-500/25 bg-amber-500/5"
        : tone === "bad"
          ? "border-red-500/25 bg-red-500/5"
          : "border-slate-700/60 bg-slate-900/40";

  return (
    <div className={cn("rounded-lg border px-3 py-2.5", toneClass)}>
      <div className="flex items-center gap-2 text-[11px] font-medium uppercase tracking-wide text-slate-500">
        <span className="text-slate-400" aria-hidden>
          {icon}
        </span>
        {label}
      </div>
      <p className="mt-1 text-sm font-medium text-slate-100">{value}</p>
      {detail && <p className="mt-0.5 text-xs text-slate-500">{detail}</p>}
    </div>
  );
}

function WorkflowSection({
  icon,
  title,
  subtitle,
  affects,
  children,
}: {
  icon: ReactNode;
  title: string;
  subtitle: string;
  affects: string;
  children: ReactNode;
}) {
  return (
    <section className="rounded-lg border border-slate-800/80 bg-slate-950/30 p-4">
      <div className="mb-4 flex gap-3">
        <span
          className="mt-0.5 flex size-9 shrink-0 items-center justify-center rounded-lg bg-slate-800/80 text-sky-400"
          aria-hidden
        >
          {icon}
        </span>
        <div className="min-w-0">
          <h3 className="text-sm font-semibold text-slate-100">{title}</h3>
          <p className="mt-0.5 text-sm text-slate-400">{subtitle}</p>
          <p className="mt-1.5 text-xs text-slate-500">
            <span className="font-medium text-slate-400">Speeds up:</span> {affects}
          </p>
        </div>
      </div>
      {children}
    </section>
  );
}

type LanguageWarmChipState = "ready" | "partial" | "cold" | "missing";

function languageWarmChipState(
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

function LanguageChipGrid({
  labels,
  disabled,
  warmPending,
  readinessByLanguage,
  onWarmSelected,
}: {
  labels: readonly string[];
  disabled: boolean;
  warmPending?: boolean;
  readinessByLanguage?: Map<string, LanguageWarmChipState>;
  onWarmSelected: (selected: string[]) => void;
}) {
  const [selected, setSelected] = useState<Set<string>>(new Set());

  const selectedList = useMemo(() => [...selected].sort(), [selected]);

  const toggle = (label: string) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(label)) {
        next.delete(label);
      } else {
        next.add(label);
      }
      return next;
    });
  };

  const clear = () => setSelected(new Set());

  return (
    <div className="space-y-2">
      <div className="flex flex-wrap items-center gap-2">
        <Button
          type="primary"
          size="small"
          disabled={disabled || selectedList.length === 0}
          loading={warmPending}
          onClick={() => {
            onWarmSelected(selectedList);
            clear();
          }}
        >
          Warm selected
          {selectedList.length > 0 ? ` (${selectedList.length})` : ""}
        </Button>
        {selectedList.length > 0 && (
          <Button size="small" disabled={disabled || warmPending} onClick={clear}>
            Clear
          </Button>
        )}
        <span className="text-xs text-slate-500">
          Dots = warm state (green all runtimes, amber partial). Click chips to select for the next job.
        </span>
      </div>
      <div className="flex flex-wrap gap-1.5" role="group" aria-label="Select languages to warm">
        {labels.map((label) => {
          const isSelected = selected.has(label);
          const readiness = readinessByLanguage?.get(label) ?? "cold";
          const readinessLabel =
            readiness === "ready"
              ? "ready"
              : readiness === "partial"
                ? "partially warmed"
                : readiness === "missing"
                  ? "no runtime configured"
                  : "not warmed";
          return (
            <Button
              key={label}
              size="small"
              type={isSelected ? "primary" : "default"}
              disabled={disabled}
              className={cn(
                "!text-xs",
                isSelected && "!border-sky-500/50",
                !isSelected && readiness === "ready" && "!border-emerald-500/40",
                !isSelected && readiness === "partial" && "!border-amber-500/40",
              )}
              aria-pressed={isSelected}
              aria-label={`${label}, ${readinessLabel}`}
              onClick={() => toggle(label)}
            >
              <span
                className={cn(
                  "mr-1.5 inline-block size-1.5 shrink-0 rounded-full",
                  readiness === "ready" && "bg-emerald-400",
                  readiness === "partial" && "bg-amber-400",
                  readiness === "cold" && "bg-slate-600",
                  readiness === "missing" && "bg-red-400/80",
                )}
                aria-hidden
              />
              {label}
            </Button>
          );
        })}
      </div>
    </div>
  );
}

function imageTableColumns(kind: "runner" | "lsp") {
  return [
    { title: "Runtime", dataIndex: "label", key: "label", width: 140 },
    {
      title: "Docker image",
      dataIndex: "image",
      key: "image",
      ellipsis: true,
      render: (image: string) => (
        <code className="text-xs text-slate-400">{image}</code>
      ),
    },
    {
      title: "Image",
      key: "present",
      width: 110,
      render: (_: unknown, row: RunnerImageStatus) => builtTag(row.present),
    },
    {
      title: kind === "runner" ? "Smoke warm" : "LSP warm",
      key: "warmed",
      width: 120,
      render: (_: unknown, row: RunnerImageStatus) => warmTag(row.warmed),
    },
  ];
}

export default function AdminOpsPage() {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const [activeJobId, setActiveJobId] = useState<string | null>(null);

  const jobQuery = useQuery({
    queryKey: ["ops", "runners", "job", activeJobId],
    queryFn: () => apiFetch<RunnerOpsJob>(ApiPaths.opsRunnerJob(activeJobId!)),
    enabled: Boolean(activeJobId),
    refetchInterval: (query) =>
      query.state.data?.status === "RUNNING" ? 1500 : false,
  });

  const warmMaven = useMutation({
    mutationFn: (force: boolean) =>
      apiFetch<RunnerOpsJob>(`${ApiPaths.OPS_RUNNERS_WARM_MAVEN}?force=${force}`, {
        method: "POST",
      }),
    onSuccess: (job) => {
      setActiveJobId(job.id);
      message.info("Maven cache warm started");
    },
    onError: (error) =>
      message.error(error instanceof ApiError ? error.message : "Could not start Maven warm"),
  });

  const warmInfra = useMutation({
    mutationFn: ({ force, only }: { force: boolean; only?: string[] }) =>
      apiFetch<RunnerOpsJob>(ApiPaths.OPS_RUNNERS_WARM_INFRA, {
        method: "POST",
        body: JSON.stringify({ force, only: only ?? [] }),
      }),
    onSuccess: (job) => {
      setActiveJobId(job.id);
      message.info("Warm-up started (runners + editor)");
      void queryClient.refetchQueries({ queryKey: ["ops", "runners", "status"] });
    },
    onSettled: () => {
      void queryClient.refetchQueries({ queryKey: ["ops", "runners", "status"] });
    },
    onError: (error) =>
      message.error(error instanceof ApiError ? error.message : "Could not start warm-up"),
  });

  const job = jobQuery.data;
  const jobRunning = job?.status === "RUNNING";
  const warmActionPending = warmMaven.isPending || warmInfra.isPending;

  const statusQuery = useQuery({
    queryKey: ["ops", "runners", "status"],
    queryFn: () => apiFetch<RunnerOpsStatus>(ApiPaths.OPS_RUNNERS_STATUS),
    staleTime: 0,
    refetchOnWindowFocus: true,
    refetchInterval: jobRunning || warmActionPending ? 2000 : 5000,
  });

  useEffect(() => {
    const serverJobId = statusQuery.data?.activeJobId;
    if (serverJobId && !activeJobId) {
      setActiveJobId(serverJobId);
    }
  }, [statusQuery.data?.activeJobId, activeJobId]);

  useEffect(() => {
    if (!job || job.status === "RUNNING") {
      return;
    }
    void queryClient.refetchQueries({ queryKey: ["ops", "runners", "status"] });
  }, [job?.status, job?.id, queryClient]);

  const status = statusQuery.data;
  const busy = warmMaven.isPending || warmInfra.isPending || job?.status === "RUNNING";

  const dockerReady = Boolean(status?.dockerAvailable && status.dockerEnabled);
  const languageStats = useMemo(() => {
    const rows = status?.languages ?? [];
    const readyLanguages = WARM_LANGUAGES.filter((lang) => {
      const langRows = rows.filter((row) => row.language === lang);
      if (langRows.length === 0) {
        return false;
      }
      return langRows.every((row) => row.ready);
    });
    const partialLanguages = WARM_LANGUAGES.filter((lang) => {
      const state = languageWarmChipState(rows, lang);
      return state === "partial";
    });
    return {
      total: WARM_LANGUAGES.length,
      ready: readyLanguages.length,
      partial: partialLanguages.length,
      rows,
    };
  }, [status?.languages]);

  const readinessByLanguage = useMemo(() => {
    const rows = status?.languages ?? [];
    return new Map(
      WARM_LANGUAGES.map((lang) => [lang, languageWarmChipState(rows, lang)] as const),
    );
  }, [status?.languages]);

  return (
    <AppLayout>
      <PageHeader
        title="Infrastructure warm-up"
        description={
          <>
            Preload <strong className="text-slate-300">Run tests</strong> runners and editor IntelliSense together —
            one warm-up per language, one combined status.
          </>
        }
        extra={
          <Button
            icon={<RefreshCw className="size-4" aria-hidden />}
            onClick={() => void statusQuery.refetch()}
            loading={statusQuery.isFetching}
          >
            Refresh
          </Button>
        }
      />

      {statusQuery.error && (
        <Alert
          type="error"
          showIcon
          className="mb-4"
          message={(statusQuery.error as Error).message}
        />
      )}

      {status && !dockerReady && (
        <Alert
          type="warning"
          showIcon
          className="mb-4"
          message="Docker is not ready on the API host"
          description={
            status.dockerEnabled
              ? "The API cannot reach the Docker CLI. Warm jobs will fail until docker.sock is accessible."
              : "Docker integration is disabled (CTL_DOCKER_ENABLED=false)."
          }
        />
      )}

      {status && (
        <section aria-label="Host status" className="mb-6 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          <HealthTile
            icon={<Terminal className="size-3.5" />}
            label="Docker"
            value={status.dockerAvailable ? "Reachable" : "Unavailable"}
            detail={status.dockerEnabled ? "Integration enabled" : "Integration disabled"}
            tone={status.dockerAvailable && status.dockerEnabled ? "ok" : "bad"}
          />
          <HealthTile
            icon={<Flame className="size-3.5" />}
            label="Languages"
            value={
              languageStats.total > 0
                ? `${languageStats.ready} / ${languageStats.total} ready`
                : "—"
            }
            detail={
              languageStats.partial > 0
                ? `${languageStats.partial} partially warmed — status refreshes every few seconds`
                : "Runner + editor both preloaded on every runtime"
            }
            tone={
              languageStats.ready === languageStats.total && languageStats.total > 0
                ? "ok"
                : languageStats.ready > 0 || languageStats.partial > 0
                  ? "warn"
                  : "neutral"
            }
          />
          <HealthTile
            icon={<Database className="size-3.5" />}
            label="Java Maven cache"
            value={status.mavenCacheWarm ? "Preloaded" : "Cold"}
            detail={status.mavenCacheVolume}
            tone={status.mavenCacheWarm ? "ok" : "warn"}
          />
        </section>
      )}

      <div className="mb-4 rounded-lg border border-sky-500/20 bg-sky-500/5 px-4 py-3">
        <p className="text-sm text-slate-400">
          <strong className="font-medium text-sky-200">Warm languages</strong> runs submission-runner smoke{" "}
          <em>and</em> editor (LSP) preload in a single job. Java also triggers Maven cache when needed.
        </p>
      </div>

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(280px,360px)]">
        <div className="space-y-4">
          <CtlCard title="Warm languages" className="!border-slate-800/80">
            <WorkflowSection
              icon={<Flame className="size-4" />}
              title="Runners + editor"
              subtitle="One job per selection: pooled runner smoke, then LSP warm for the matching editor stack."
              affects="Run tests and IntelliSense for each language"
            >
              <div className="flex flex-wrap gap-2">
                <Button
                  type="primary"
                  icon={<Flame className="size-4" aria-hidden />}
                  disabled={busy || !dockerReady}
                  loading={warmInfra.isPending}
                  onClick={() => warmInfra.mutate({ force: false })}
                >
                  Warm all languages
                </Button>
                <Button
                  disabled={busy || !dockerReady}
                  loading={warmInfra.isPending}
                  onClick={() => warmInfra.mutate({ force: true })}
                >
                  Force re-warm all
                </Button>
              </div>
              <LanguageChipGrid
                labels={WARM_LANGUAGES}
                disabled={busy || !dockerReady}
                warmPending={warmInfra.isPending}
                readinessByLanguage={readinessByLanguage}
                onWarmSelected={(only) => warmInfra.mutate({ force: false, only })}
              />
              {!status?.lspScriptsAvailable && (
                <Alert
                  className="mt-4"
                  type="info"
                  showIcon
                  message="LSP step needs scripts/lsp_warm.py on the API host (CTL_REPO_ROOT). Runner warm still runs."
                />
              )}
            </WorkflowSection>

            <div className="my-5 border-t border-slate-800/80" />

            <WorkflowSection
              icon={<Database className="size-4" />}
              title="Advanced: Maven cache only"
              subtitle="Optional — combined warm already runs this before Java when the cache is cold."
              affects="Java compile downloads"
            >
              <div className="flex flex-wrap gap-2">
                <Button
                  disabled={busy || !dockerReady}
                  loading={warmMaven.isPending}
                  onClick={() => warmMaven.mutate(false)}
                >
                  Warm Maven cache
                </Button>
                <Button
                  disabled={busy || !dockerReady}
                  loading={warmMaven.isPending}
                  onClick={() => warmMaven.mutate(true)}
                >
                  Force re-warm
                </Button>
              </div>
            </WorkflowSection>
          </CtlCard>

          {status && (
            <CtlCard title="Image inventory">
              <div className="mb-4 flex flex-wrap gap-4 rounded-md border border-slate-800/60 bg-slate-950/40 px-3 py-2 text-xs text-slate-500">
                <span className="inline-flex items-center gap-1.5">
                  <CheckCircle2 className="size-3.5 text-emerald-400" aria-hidden />
                  Image ready = docker image exists locally
                </span>
                <span className="inline-flex items-center gap-1.5">
                  <Box className="size-3.5 text-amber-400" aria-hidden />
                  Preloaded = warm job completed for current image tag
                </span>
              </div>
              <Tabs
                defaultActiveKey="languages"
                items={[
                  {
                    key: "languages",
                    label: `By runtime (${languageStats.ready}/${languageStats.total} languages)`,
                    children: (
                      <Table
                        size="small"
                        pagination={false}
                        rowKey={(row) => `${row.language}-${row.version ?? "none"}-${row.runnerImage ?? row.label}`}
                        dataSource={languageStats.rows}
                        columns={[
                          {
                            title: "Language",
                            dataIndex: "language",
                            key: "language",
                            width: 100,
                            render: (lang: string) => (
                              <span className="font-medium capitalize text-slate-200">{lang}</span>
                            ),
                          },
                          {
                            title: "Runtime",
                            dataIndex: "label",
                            key: "label",
                            width: 120,
                            render: (label: string) => (
                              <span className="text-slate-300">{label}</span>
                            ),
                          },
                          {
                            title: "Run tests",
                            dataIndex: "runnerReady",
                            key: "runnerReady",
                            width: 120,
                            render: (v: boolean | null, row: LanguageWarmStatus) =>
                              row.runnerPresent ? warmTag(v) : <Tag color="error">No image</Tag>,
                          },
                          {
                            title: "Editor",
                            dataIndex: "editorReady",
                            key: "editorReady",
                            width: 120,
                            render: (v: boolean | null) => warmTag(v),
                          },
                          {
                            title: "Status",
                            dataIndex: "ready",
                            key: "ready",
                            width: 110,
                            render: (ready: boolean) => readyTag(ready),
                          },
                        ]}
                      />
                    ),
                  },
                  {
                    key: "runners",
                    label: "Runner images",
                    children: (
                      <Table
                        size="small"
                        pagination={false}
                        rowKey="image"
                        dataSource={status.runnerImages}
                        columns={imageTableColumns("runner")}
                      />
                    ),
                  },
                  {
                    key: "lsp",
                    label: "LSP images",
                    children: (
                      <Table
                        size="small"
                        pagination={false}
                        rowKey={(row) => `${row.label}-${row.image}`}
                        dataSource={status.lspImages}
                        columns={imageTableColumns("lsp")}
                      />
                    ),
                  },
                ]}
              />
            </CtlCard>
          )}
        </div>

        <aside className="xl:sticky xl:top-4 xl:self-start">
          <CtlCard
            title="Job log"
            extra={
              busy ? (
                <span className="inline-flex items-center gap-1.5 text-xs text-slate-400">
                  <Loader2 className="size-3.5 animate-spin" aria-hidden />
                  Running…
                </span>
              ) : null
            }
          >
            {!job ? (
              <div className="flex flex-col items-center gap-3 py-8 text-center">
                <CircleAlert className="size-8 text-slate-600" aria-hidden />
                <p className="text-sm text-slate-400">
                  Start a warm-up job to see live progress here.
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                <div className="flex flex-wrap items-center gap-2">
                  {jobStatusTag(job.status)}
                  <Typography.Text className="text-xs text-slate-400">
                    {JOB_TYPE_LABELS[job.type] ?? job.type}
                  </Typography.Text>
                </div>
                <p className="text-sm text-slate-300">{job.message}</p>
                {job.status === "COMPLETED" && (
                  <p className="inline-flex items-center gap-1.5 text-xs text-emerald-400">
                    <CheckCircle2 className="size-3.5" aria-hidden />
                    Job finished — chips and inventory tables sync from the server automatically.
                  </p>
                )}
                {job.status === "FAILED" && (
                  <p className="inline-flex items-center gap-1.5 text-xs text-red-400">
                    <XCircle className="size-3.5" aria-hidden />
                    Job failed — languages that passed smoke before the error may still show amber (partial).
                  </p>
                )}
                {job.logTail ? (
                  <pre
                    className={cn(
                      "max-h-[min(24rem,50vh)] overflow-auto rounded-md border border-slate-800/80",
                      "bg-slate-950/80 p-3 font-mono text-[11px] leading-relaxed text-slate-300",
                    )}
                  >
                    {job.logTail}
                  </pre>
                ) : (
                  <p className="text-xs text-slate-500">No log output yet.</p>
                )}
              </div>
            )}
          </CtlCard>

          {status && (
            <p className="mt-3 text-[10px] leading-relaxed text-slate-600">
              Warm inventory is persisted in Postgres (
              <code className="text-slate-500">runner_pool_warm_state</code>,{" "}
              <code className="text-slate-500">lsp_warm_state</code>). Legacy stamp files under{" "}
              <span className="break-all text-slate-500">{status.opsDataDir}</span> are imported once on startup.
            </p>
          )}
        </aside>
      </div>
    </AppLayout>
  );
}
