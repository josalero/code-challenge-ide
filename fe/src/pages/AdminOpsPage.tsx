import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Table, Tag, Typography } from "antd";
import { Flame, Loader2, RefreshCw, Server } from "lucide-react";
import { useEffect, useState } from "react";
import { apiFetch, ApiError } from "../api/client";
import type { RunnerOpsJob, RunnerOpsStatus } from "../api/types";
import AppLayout from "../components/AppLayout";
import CtlCard from "../components/ui/CtlCard";
import PageHeader from "../components/ui/PageHeader";
import { ApiPaths } from "../domain/constants";
import { cn } from "../lib/utils";

const LSP_LABELS = ["java", "python", "go", "typescript", "csharp", "rust", "cpp"] as const;

function statusTag(present: boolean) {
  return present ? (
    <Tag color="success">Built</Tag>
  ) : (
    <Tag color="error">Missing</Tag>
  );
}

function warmTag(warmed: boolean | null | undefined) {
  if (warmed == null) {
    return <Tag color="default">N/A</Tag>;
  }
  return warmed ? (
    <Tag color="success">Warm</Tag>
  ) : (
    <Tag color="warning">Cold</Tag>
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

export default function AdminOpsPage() {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const [activeJobId, setActiveJobId] = useState<string | null>(null);

  const statusQuery = useQuery({
    queryKey: ["ops", "runners", "status"],
    queryFn: () => apiFetch<RunnerOpsStatus>(ApiPaths.OPS_RUNNERS_STATUS),
    refetchInterval: activeJobId ? 3000 : false,
  });

  const jobQuery = useQuery({
    queryKey: ["ops", "runners", "job", activeJobId],
    queryFn: () => apiFetch<RunnerOpsJob>(ApiPaths.opsRunnerJob(activeJobId!)),
    enabled: Boolean(activeJobId),
    refetchInterval: (query) =>
      query.state.data?.status === "RUNNING" ? 1500 : false,
  });

  useEffect(() => {
    const serverJobId = statusQuery.data?.activeJobId;
    if (serverJobId && !activeJobId) {
      setActiveJobId(serverJobId);
    }
  }, [statusQuery.data?.activeJobId, activeJobId]);

  useEffect(() => {
    const job = jobQuery.data;
    if (!job || job.status === "RUNNING") {
      return;
    }
    void queryClient.invalidateQueries({ queryKey: ["ops", "runners", "status"] });
  }, [jobQuery.data, queryClient]);

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

  const status = statusQuery.data;
  const job = jobQuery.data;
  const busy = warmMaven.isPending || warmLsp.isPending || job?.status === "RUNNING";

  return (
    <AppLayout>
      <PageHeader
        title="Admin ops"
        description="Warm runner and language-server images on demand instead of waiting for a full make runners during startup."
        extra={
          <Button
            icon={<RefreshCw className="size-4" aria-hidden />}
            onClick={() => void statusQuery.refetch()}
            loading={statusQuery.isFetching}
          >
            Refresh status
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

      {status && (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <CtlCard title="Docker">
            <Typography.Paragraph className="!mb-0 !text-slate-300">
              {status.dockerAvailable ? "CLI reachable" : "Unavailable on API host"}
            </Typography.Paragraph>
            <Typography.Text className="text-xs text-slate-500">
              Integration {status.dockerEnabled ? "enabled" : "disabled"}
            </Typography.Text>
          </CtlCard>
          <CtlCard title="Maven cache">
            <Typography.Paragraph className="!mb-2 !text-slate-300">
              Volume: {status.mavenCacheVolume}
            </Typography.Paragraph>
            {status.mavenCacheWarm ? <Tag color="success">Warm</Tag> : <Tag color="warning">Cold</Tag>}
          </CtlCard>
          <CtlCard title="LSP scripts">
            <Typography.Paragraph className="!mb-2 !text-slate-300">
              {status.lspScriptsAvailable ? "warm script found" : "script not found on API host"}
            </Typography.Paragraph>
            {status.lspWarmStampPresent ? (
              <Tag color="success">Stamp present</Tag>
            ) : (
              <Tag color="default">Not warmed yet</Tag>
            )}
          </CtlCard>
          <CtlCard title="Repo root">
            <Typography.Paragraph className="!mb-0 break-all !text-xs !text-slate-400">
              {status.repoRoot}
            </Typography.Paragraph>
          </CtlCard>
        </div>
      )}

      <section className="mt-6 grid gap-4 xl:grid-cols-[minmax(0,1.2fr)_minmax(0,0.8fr)]">
        <CtlCard
          title="Warm actions"
          extra={
            busy ? (
              <span className="inline-flex items-center gap-1.5 text-xs text-slate-400">
                <Loader2 className="size-3.5 animate-spin" aria-hidden />
                Job running…
              </span>
            ) : null
          }
        >
          <div className="flex flex-wrap gap-2">
            <Button
              type="primary"
              icon={<Flame className="size-4" aria-hidden />}
              disabled={busy || !status?.dockerAvailable}
              loading={warmMaven.isPending}
              onClick={() => warmMaven.mutate(false)}
            >
              Warm Maven cache
            </Button>
            <Button
              disabled={busy || !status?.dockerAvailable}
              loading={warmMaven.isPending}
              onClick={() => warmMaven.mutate(true)}
            >
              Force Maven warm
            </Button>
            <Button
              type="primary"
              icon={<Server className="size-4" aria-hidden />}
              disabled={busy || !status?.dockerAvailable || !status?.lspScriptsAvailable}
              loading={warmLsp.isPending}
              onClick={() => warmLsp.mutate({ force: false })}
            >
              Warm all LSP
            </Button>
            <Button
              disabled={busy || !status?.dockerAvailable || !status?.lspScriptsAvailable}
              loading={warmLsp.isPending}
              onClick={() => warmLsp.mutate({ force: true })}
            >
              Force all LSP
            </Button>
          </div>

          <Typography.Title level={5} className="!mb-3 !mt-6 !text-slate-200">
            Warm one LSP
          </Typography.Title>
          <div className="flex flex-wrap gap-2">
            {LSP_LABELS.map((label) => (
              <Button
                key={label}
                size="small"
                disabled={busy || !status?.dockerAvailable || !status?.lspScriptsAvailable}
                onClick={() => warmLsp.mutate({ force: false, only: [label] })}
              >
                {label}
              </Button>
            ))}
          </div>

          {!status?.lspScriptsAvailable && (
            <Alert
              className="mt-4"
              type="info"
              showIcon
              message="LSP warm requires scripts/lsp_warm.py on the API host (local dev with ./gradlew :be:bootRun). Set CTL_REPO_ROOT if needed."
            />
          )}
        </CtlCard>

        <CtlCard title="Active job">
          {!job ? (
            <Typography.Paragraph className="!mb-0 !text-slate-400">
              No warm job selected. Start Maven or LSP warm to see live logs.
            </Typography.Paragraph>
          ) : (
            <div className="space-y-3">
              <div className="flex flex-wrap items-center gap-2">
                {jobStatusTag(job.status)}
                <Typography.Text className="text-xs text-slate-400">{job.type}</Typography.Text>
              </div>
              <Typography.Paragraph className="!mb-0 !text-sm !text-slate-300">
                {job.message}
              </Typography.Paragraph>
              {job.logTail && (
                <pre
                  className={cn(
                    "max-h-80 overflow-auto rounded-md border border-slate-800/80",
                    "bg-slate-950/80 p-3 text-xs leading-relaxed text-slate-300",
                  )}
                >
                  {job.logTail}
                </pre>
              )}
            </div>
          )}
        </CtlCard>
      </section>

      {status && (
        <section className="mt-6 grid gap-4 xl:grid-cols-2">
          <CtlCard title="Runner images">
            <Typography.Paragraph className="!mb-3 !text-xs !text-slate-500">
              Built = image exists locally. Warm (Java only) = shared Maven cache preloaded.
            </Typography.Paragraph>
            <Table
              size="small"
              pagination={false}
              rowKey="image"
              dataSource={status.runnerImages}
              columns={[
                { title: "Label", dataIndex: "label", key: "label" },
                { title: "Docker image", dataIndex: "image", key: "image", ellipsis: true },
                {
                  title: "Built",
                  key: "present",
                  render: (_, row) => statusTag(row.present),
                },
                {
                  title: "Warm",
                  key: "warmed",
                  render: (_, row) => warmTag(row.warmed),
                },
              ]}
            />
          </CtlCard>
          <CtlCard title="LSP images">
            <Typography.Paragraph className="!mb-3 !text-xs !text-slate-500">
              Built = image exists locally. Warm = LSP initialize handshake completed (see stamp file).
            </Typography.Paragraph>
            <Table
              size="small"
              pagination={false}
              rowKey="image"
              dataSource={status.lspImages}
              columns={[
                { title: "Label", dataIndex: "label", key: "label" },
                { title: "Docker image", dataIndex: "image", key: "image", ellipsis: true },
                {
                  title: "Built",
                  key: "present",
                  render: (_, row) => statusTag(row.present),
                },
                {
                  title: "Warm",
                  key: "warmed",
                  render: (_, row) => warmTag(row.warmed),
                },
              ]}
            />
          </CtlCard>
        </section>
      )}
    </AppLayout>
  );
}
