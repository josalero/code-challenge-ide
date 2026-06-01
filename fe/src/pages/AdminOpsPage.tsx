import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Table, Tabs, Tag, Typography } from "antd";
import type { ReactNode } from "react";
import {
  Box,
  CheckCircle2,
  CircleAlert,
  Container,
  Database,
  Flame,
  Loader2,
  RefreshCw,
  Sparkles,
  Terminal,
  XCircle,
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { apiFetch, ApiError } from "../api/client";
import type { RunnerImageStatus, RunnerOpsJob, RunnerOpsStatus } from "../api/types";
import AppLayout from "../components/AppLayout";
import CtlCard from "../components/ui/CtlCard";
import PageHeader from "../components/ui/PageHeader";
import { ApiPaths } from "../domain/constants";
import { cn } from "../lib/utils";

const LSP_LABELS = ["java", "python", "go", "typescript", "csharp", "rust", "cpp"] as const;
const RUNNER_WARM_LANGUAGES = [
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

function LanguageChipGrid({
  labels,
  disabled,
  onSelect,
}: {
  labels: readonly string[];
  disabled: boolean;
  onSelect: (label: string) => void;
}) {
  return (
    <div className="flex flex-wrap gap-1.5" role="group" aria-label="Warm one language">
      {labels.map((label) => (
        <Button
          key={label}
          size="small"
          disabled={disabled}
          className="!text-xs"
          onClick={() => onSelect(label)}
        >
          {label}
        </Button>
      ))}
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

  const warmLsp = useMutation({
    mutationFn: ({ force, only }: { force: boolean; only?: string[] }) =>
      apiFetch<RunnerOpsJob>(ApiPaths.OPS_RUNNERS_WARM_LSP, {
        method: "POST",
        body: JSON.stringify({ force, only: only ?? [] }),
      }),
    onSuccess: (job) => {
      setActiveJobId(job.id);
      message.info("LSP warm started");
    },
    onError: (error) =>
      message.error(error instanceof ApiError ? error.message : "Could not start LSP warm"),
  });

  const warmRunnerPool = useMutation({
    mutationFn: ({ force, only }: { force: boolean; only?: string[] }) =>
      apiFetch<RunnerOpsJob>(ApiPaths.OPS_RUNNERS_WARM_POOL, {
        method: "POST",
        body: JSON.stringify({ force, only: only ?? [] }),
      }),
    onSuccess: (job) => {
      setActiveJobId(job.id);
      message.info("Runner pool warm started");
    },
    onError: (error) =>
      message.error(
        error instanceof ApiError ? error.message : "Could not start runner pool warm",
      ),
  });

  const job = jobQuery.data;
  const jobRunning = job?.status === "RUNNING";
  const warmActionPending =
    warmMaven.isPending || warmLsp.isPending || warmRunnerPool.isPending;

  const statusQuery = useQuery({
    queryKey: ["ops", "runners", "status"],
    queryFn: () => apiFetch<RunnerOpsStatus>(ApiPaths.OPS_RUNNERS_STATUS),
    refetchInterval: jobRunning || warmActionPending ? 3000 : false,
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
    void queryClient.invalidateQueries({ queryKey: ["ops", "runners", "status"] });
  }, [job?.status, job?.id, queryClient]);

  const status = statusQuery.data;
  const busy =
    warmMaven.isPending
    || warmLsp.isPending
    || warmRunnerPool.isPending
    || job?.status === "RUNNING";

  const dockerReady = Boolean(status?.dockerAvailable && status.dockerEnabled);
  const runnerStats = useMemo(() => {
    const images = status?.runnerImages ?? [];
    const built = images.filter((r) => r.present);
    return {
      built: built.length,
      warm: built.filter((r) => r.warmed).length,
      total: images.length,
    };
  }, [status?.runnerImages]);

  const lspStats = useMemo(() => {
    const images = status?.lspImages ?? [];
    const built = images.filter((r) => r.present);
    return {
      built: built.length,
      warm: built.filter((r) => r.warmed).length,
      total: images.length,
    };
  }, [status?.lspImages]);

  return (
    <AppLayout>
      <PageHeader
        title="Infrastructure warm-up"
        description={
          <>
            Preload Docker runners and language servers so the first <strong className="text-slate-300">Run tests</strong>{" "}
            click and editor IntelliSense feel fast. Each workflow targets a different part of the stack.
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
            icon={<Container className="size-3.5" />}
            label="Submission runners"
            value={
              runnerStats.built > 0
                ? `${runnerStats.warm} / ${runnerStats.built} preloaded`
                : "No images built"
            }
            detail={`${runnerStats.built} of ${runnerStats.total} runner images local`}
            tone={
              runnerStats.built > 0 && runnerStats.warm === runnerStats.built
                ? "ok"
                : runnerStats.warm > 0
                  ? "warn"
                  : "neutral"
            }
          />
          <HealthTile
            icon={<Sparkles className="size-3.5" />}
            label="Editor (LSP)"
            value={
              lspStats.built > 0
                ? `${lspStats.warm} / ${lspStats.built} preloaded`
                : "No images built"
            }
            detail={status.lspScriptsAvailable ? "Warm script available" : "Warm script missing"}
            tone={lspStats.warm > 0 ? "ok" : status.lspScriptsAvailable ? "warn" : "bad"}
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
        <p className="text-sm font-medium text-sky-200">Recommended order</p>
        <ol className="mt-1.5 list-inside list-decimal space-y-0.5 text-sm text-slate-400">
          <li>
            <strong className="font-medium text-slate-300">Runner pool smoke</strong> — biggest win for Run tests (starts pool + fake submit).
          </li>
          <li>
            <strong className="font-medium text-slate-300">Maven cache</strong> — optional; runner warm auto-runs this for Java when needed.
          </li>
          <li>
            <strong className="font-medium text-slate-300">Language servers</strong> — editor autocomplete only; does not affect test runs.
          </li>
        </ol>
      </div>

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(280px,360px)]">
        <div className="space-y-4">
          <CtlCard title="Warm-up workflows" className="!border-slate-800/80">
            <WorkflowSection
              icon={<Flame className="size-4" />}
              title="1. Runner pool smoke"
              subtitle="Starts pooled containers and runs one smoke submission per language — same path as Run tests."
              affects="First Run tests click (all languages)"
            >
              <div className="flex flex-wrap gap-2">
                <Button
                  type="primary"
                  icon={<Flame className="size-4" aria-hidden />}
                  disabled={busy || !dockerReady}
                  loading={warmRunnerPool.isPending}
                  onClick={() => warmRunnerPool.mutate({ force: false })}
                >
                  Warm all runners
                </Button>
                <Button
                  disabled={busy || !dockerReady}
                  loading={warmRunnerPool.isPending}
                  onClick={() => warmRunnerPool.mutate({ force: true })}
                >
                  Force re-warm
                </Button>
              </div>
              <p className="mb-2 mt-4 text-xs font-medium uppercase tracking-wide text-slate-500">
                One language
              </p>
              <LanguageChipGrid
                labels={RUNNER_WARM_LANGUAGES}
                disabled={busy || !dockerReady}
                onSelect={(label) => warmRunnerPool.mutate({ force: false, only: [label] })}
              />
            </WorkflowSection>

            <div className="my-5 border-t border-slate-800/80" />

            <WorkflowSection
              icon={<Database className="size-4" />}
              title="2. Maven dependency cache"
              subtitle="Copies pre-baked JARs into the shared volume so Java compiles skip downloads."
              affects="Java runner cold start (helper — runner warm includes this when needed)"
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

            <div className="my-5 border-t border-slate-800/80" />

            <WorkflowSection
              icon={<Sparkles className="size-4" />}
              title="3. Language servers (LSP)"
              subtitle="Runs the LSP initialize handshake so Monaco autocomplete is ready in the editor."
              affects="Editor IntelliSense only — not test execution"
            >
              <div className="flex flex-wrap gap-2">
                <Button
                  disabled={busy || !dockerReady || !status?.lspScriptsAvailable}
                  loading={warmLsp.isPending}
                  onClick={() => warmLsp.mutate({ force: false })}
                >
                  Warm all LSP
                </Button>
                <Button
                  disabled={busy || !dockerReady || !status?.lspScriptsAvailable}
                  loading={warmLsp.isPending}
                  onClick={() => warmLsp.mutate({ force: true })}
                >
                  Force re-warm
                </Button>
              </div>
              <p className="mb-2 mt-4 text-xs font-medium uppercase tracking-wide text-slate-500">
                One language
              </p>
              <LanguageChipGrid
                labels={LSP_LABELS}
                disabled={busy || !dockerReady || !status?.lspScriptsAvailable}
                onSelect={(label) => warmLsp.mutate({ force: false, only: [label] })}
              />
              {!status?.lspScriptsAvailable && (
                <Alert
                  className="mt-4"
                  type="info"
                  showIcon
                  message="LSP warm needs scripts/lsp_warm.py on the API host. Set CTL_REPO_ROOT when using bootRun."
                />
              )}
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
                defaultActiveKey="runners"
                items={[
                  {
                    key: "runners",
                    label: `Runners (${runnerStats.warm}/${runnerStats.built})`,
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
                    label: `LSP (${lspStats.warm}/${lspStats.built})`,
                    children: (
                      <Table
                        size="small"
                        pagination={false}
                        rowKey="image"
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
                    Inventory updated — smoke warm state reflects the latest job.
                  </p>
                )}
                {job.status === "FAILED" && (
                  <p className="inline-flex items-center gap-1.5 text-xs text-red-400">
                    <XCircle className="size-3.5" aria-hidden />
                    Check the log below for details.
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
            <p className="mt-3 break-all text-[10px] leading-relaxed text-slate-600">
              Warm stamp dir: {status.opsDataDir}
            </p>
          )}
        </aside>
      </div>
    </AppLayout>
  );
}
