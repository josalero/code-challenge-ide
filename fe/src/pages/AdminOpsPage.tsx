import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button } from "antd";
import { RefreshCw } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { apiFetch, ApiError } from "../api/client";
import type { RunnerOpsJob, RunnerOpsStatus } from "../api/types";
import LanguageWarmList from "../components/admin-ops/LanguageWarmList";
import OpsImageInventory from "../components/admin-ops/OpsImageInventory";
import OpsJobPanel from "../components/admin-ops/OpsJobPanel";
import OpsQuickStart from "../components/admin-ops/OpsQuickStart";
import OpsReadinessBanner from "../components/admin-ops/OpsReadinessBanner";
import {
  groupLanguagesByWarmState,
  WARM_LANGUAGES,
} from "../components/admin-ops/opsWarmUtils";
import AppLayout from "../components/AppLayout";
import CtlCard from "../components/ui/CtlCard";
import PageHeader from "../components/ui/PageHeader";
import { ApiPaths } from "../domain/constants";

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

  const warmInfra = useMutation({
    mutationFn: ({ force, only }: { force: boolean; only?: string[] }) =>
      apiFetch<RunnerOpsJob>(ApiPaths.OPS_RUNNERS_WARM_INFRA, {
        method: "POST",
        body: JSON.stringify({ force, only: only ?? [] }),
      }),
    onSuccess: (job) => {
      setActiveJobId(job.id);
      message.info("Warm-up started");
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
  const warmActionPending = warmInfra.isPending;

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
  const busy = warmInfra.isPending || job?.status === "RUNNING";
  const dockerReady = Boolean(status?.dockerAvailable && status.dockerEnabled);

  const languageGroups = useMemo(
    () => groupLanguagesByWarmState(status?.languages ?? []),
    [status?.languages],
  );

  const readyLanguageCount = languageGroups.filter((g) => g.state === "ready").length;

  return (
    <AppLayout>
      <PageHeader
        title="Infrastructure warm-up"
        description="Prepare Docker runners and editor language servers so learners get fast Run tests and IntelliSense. Use this after deploy or make runners."
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
        <>
          <OpsReadinessBanner
            status={status}
            groups={languageGroups}
            dockerReady={dockerReady}
            jobRunning={Boolean(jobRunning)}
          />
          <OpsQuickStart status={status} dockerReady={dockerReady} />
        </>
      )}

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(300px,380px)]">
        <div className="space-y-6">
          <CtlCard title="Languages" className="!border-slate-800/80">
            {status ? (
              <LanguageWarmList
                groups={languageGroups}
                disabled={busy || !dockerReady}
                warmPending={warmInfra.isPending}
                onWarmAll={(force) => warmInfra.mutate({ force })}
                onWarmLanguage={(language) =>
                  warmInfra.mutate({ force: false, only: [language] })
                }
              />
            ) : (
              <p className="text-sm text-slate-500">Loading language status…</p>
            )}
            {status && !status.lspScriptsAvailable && (
              <Alert
                className="mt-4"
                type="info"
                showIcon
                message="Editor warm needs scripts/lsp_warm.py on the API host (CTL_REPO_ROOT). Runner warm still runs."
              />
            )}
          </CtlCard>

          {status && (
            <OpsImageInventory
              status={status}
              languageRows={status.languages}
              readyLanguageCount={readyLanguageCount}
              totalLanguages={WARM_LANGUAGES.length}
            />
          )}
        </div>

        <aside className="xl:sticky xl:top-4 xl:self-start">
          <OpsJobPanel job={job} busy={busy} />
        </aside>
      </div>
    </AppLayout>
  );
}
