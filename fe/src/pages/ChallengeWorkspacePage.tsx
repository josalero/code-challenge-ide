import Editor from "@monaco-editor/react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Alert, Breadcrumb, Spin, Tabs, Typography } from "antd";
import CtlCard from "../components/ui/CtlCard";
import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { apiFetch, ApiError } from "../api/client";
import type {
  ChallengeDetail,
  CustomTestsResponse,
  ReportResponse,
  RunnerLogs,
  SubmissionResponse,
} from "../api/types";
import AiCoachPanel from "../components/AiCoachPanel";
import AppLayout from "../components/AppLayout";
import ChallengeBriefCard from "../components/ChallengeBriefCard";
import JavaLspEditor from "../components/JavaLspEditor";
import RunProgressPanel, {
  type ActivityEntry,
  type TrackedTest,
} from "../components/RunProgressPanel";
import RunResultBanner from "../components/RunResultBanner";
import WorkspaceToolbar from "../components/WorkspaceToolbar";
import {
  ApiPaths,
  JavaRuntimeVersion,
  SsePayloadKeys,
  SubmissionStatus,
} from "../domain/constants";
import type { SubmissionStatusValue } from "../domain/constants";
import { useRunTestsShortcut } from "../hooks/useRunTestsShortcut";
import { useSubmissionEvents } from "../hooks/useSubmissionEvents";
import {
  applyTestResult,
  buildInitialTrackedTests,
} from "../utils/submissionProgress";

export default function ChallengeWorkspacePage() {
  const { slug = "" } = useParams();
  const queryClient = useQueryClient();
  const { message } = App.useApp();

  const [solutionCode, setSolutionCode] = useState("");
  const [customTestsCode, setCustomTestsCode] = useState("");
  const [runtimeVersion, setRuntimeVersion] = useState<string>(JavaRuntimeVersion.DEFAULT);
  const [workspaceTab, setWorkspaceTab] = useState<"solution" | "custom">("solution");
  const [activeSubmissionId, setActiveSubmissionId] = useState<string | null>(null);
  const [submissionStatus, setSubmissionStatus] = useState<SubmissionStatusValue | null>(
    null,
  );
  const [report, setReport] = useState<ReportResponse | null>(null);
  const [reportLoading, setReportLoading] = useState(false);
  const coachRef = useRef<HTMLDivElement | null>(null);
  const lastToastReportId = useRef<string | null>(null);
  const initializedSlug = useRef<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [runnerLogs, setRunnerLogs] = useState<RunnerLogs | null>(null);
  const [activityLog, setActivityLog] = useState<ActivityEntry[]>([]);
  const [trackedTests, setTrackedTests] = useState<TrackedTest[]>([]);
  const [streamConnected, setStreamConnected] = useState(false);
  const [streamReconnecting, setStreamReconnecting] = useState(false);
  const [runStartedAt, setRunStartedAt] = useState<number | null>(null);
  const lspEnabled = true;

  const appendActivity = useCallback((msg: string) => {
    setActivityLog((prev) => [
      ...prev,
      { id: `${Date.now()}-${prev.length}`, at: Date.now(), message: msg },
    ]);
  }, []);

  const challengeQuery = useQuery({
    queryKey: ["challenge", slug],
    queryFn: () => apiFetch<ChallengeDetail>(ApiPaths.challenge(slug)),
    enabled: Boolean(slug),
  });

  const customTestsQuery = useQuery({
    queryKey: ["custom-tests", slug],
    queryFn: () =>
      apiFetch<CustomTestsResponse>(ApiPaths.challengeCustomTests(slug)),
    enabled: Boolean(slug),
  });

  useEffect(() => {
    if (!challengeQuery.data || initializedSlug.current === slug) {
      return;
    }
    initializedSlug.current = slug;
    setSolutionCode(challengeQuery.data.starterCode);
    const defaultRuntime =
      challengeQuery.data.runtimes.find((r) => r.active)?.version
        ?? JavaRuntimeVersion.DEFAULT;
    setRuntimeVersion(defaultRuntime);
    setReport(null);
    setSubmissionStatus(null);
    setSubmitError(null);
    lastToastReportId.current = null;
  }, [slug, challengeQuery.data]);

  useEffect(() => {
    if (!customTestsQuery.data) {
      return;
    }
    setCustomTestsCode(customTestsQuery.data.code);
  }, [slug, customTestsQuery.data?.code]);

  const scrollToCoach = useCallback(() => {
    coachRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
  }, []);

  const saveCustomTests = useMutation({
    mutationFn: (code: string) =>
      apiFetch<CustomTestsResponse>(ApiPaths.challengeCustomTests(slug), {
        method: "PUT",
        body: JSON.stringify({ code }),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["custom-tests", slug] });
      message.success("Custom tests saved");
    },
    onError: (e) =>
      message.error(e instanceof ApiError ? e.message : "Could not save custom tests"),
  });

  const submitMutation = useMutation({
    mutationFn: async () => {
      if (customTestsCode.trim()) {
        await saveCustomTests.mutateAsync(customTestsCode);
      }
      return apiFetch<SubmissionResponse>(ApiPaths.SUBMISSIONS, {
        method: "POST",
        headers: { "Idempotency-Key": crypto.randomUUID() },
        body: JSON.stringify({
          challengeSlug: slug,
          runtimeVersion,
          solutionCode,
          customTestsCode: customTestsCode.trim() || null,
        }),
      });
    },
    onMutate: () => {
      setSubmitError(null);
      setReport(null);
      setReportLoading(false);
      setRunnerLogs(null);
      setActivityLog([]);
      setStreamConnected(false);
      setStreamReconnecting(false);
      setRunStartedAt(Date.now());
      if (challengeQuery.data) {
        setTrackedTests(buildInitialTrackedTests(challengeQuery.data));
      }
      setSubmissionStatus(SubmissionStatus.PENDING);
      appendActivity("Submitting solution to the API…");
      message.info("Run started — sandbox is preparing");
    },
    onSuccess: (submission) => {
      setActiveSubmissionId(submission.id);
      setSubmissionStatus(submission.status as SubmissionStatusValue);
      appendActivity("Submission created — connecting live stream…");
    },
    onError: (e) => {
      const msg = e instanceof ApiError ? e.message : "Submit failed";
      setSubmitError(msg);
      setSubmissionStatus(null);
      setActiveSubmissionId(null);
      message.error(msg);
    },
  });

  const loadReport = useCallback(
    async (reportId: string) => {
      setReportLoading(true);
      try {
        const loaded = await apiFetch<ReportResponse>(ApiPaths.report(reportId));
        setReport(loaded);
      } catch (e) {
        const msg =
          e instanceof ApiError ? e.message : "Could not load coach report";
        setSubmitError(msg);
        message.error(msg);
      } finally {
        setReportLoading(false);
      }
    },
    [message],
  );

  useEffect(() => {
    if (!report || lastToastReportId.current === report.id) {
      return;
    }
    lastToastReportId.current = report.id;
    if (report.blocked) {
      message.warning("Run complete — some checks still need work");
    } else {
      message.success("Run complete — challenge passed!");
    }
  }, [report, message]);

  const pollSubmission = useCallback(
    async (submissionId: string) => {
      const submission = await apiFetch<SubmissionResponse>(
        ApiPaths.submission(submissionId),
      );
      setSubmissionStatus((prev) => {
        const next = submission.status as SubmissionStatusValue;
        if (prev !== next) {
          const labels: Record<string, string> = {
            PENDING: "Status: queued (polling)",
            RUNNING: "Status: running in Docker (polling)",
            COMPLETED: "Status: completed",
            FAILED: "Status: failed",
            CANCELLED: "Status: cancelled",
          };
          appendActivity(labels[next] ?? `Status: ${next}`);
        }
        return next;
      });
      if (
        (submission.status === SubmissionStatus.COMPLETED
          || submission.status === SubmissionStatus.FAILED)
        && submission.reportId
      ) {
        await loadReport(submission.reportId);
      }
    },
    [loadReport, appendActivity],
  );

  useSubmissionEvents(activeSubmissionId, Boolean(activeSubmissionId), {
    onStreamOpen: () => {
      setStreamConnected(true);
      setStreamReconnecting(false);
      appendActivity("Live stream connected");
    },
    onStreamReconnecting: () => {
      setStreamConnected(false);
      setStreamReconnecting(true);
      appendActivity("Stream interrupted — reconnecting…");
    },
    onStatus: (status, statusMessage) => {
      setSubmissionStatus(status as SubmissionStatusValue);
      if (statusMessage) {
        appendActivity(statusMessage);
      }
    },
    onTestResult: (test) => {
      setTrackedTests((prev) => applyTestResult(prev, test));
      appendActivity(`Test ${test.name}: ${test.status}`);
    },
    onDone: async (payload) => {
      setStreamConnected(false);
      setSubmissionStatus((prev) =>
        prev === SubmissionStatus.FAILED ? prev : SubmissionStatus.COMPLETED);
      appendActivity("Run finished — loading coach report…");
      const rawReportId =
        payload[SsePayloadKeys.REPORT_ID] ?? payload.reportId;
      const reportId =
        rawReportId != null && String(rawReportId).length > 0
          ? String(rawReportId)
          : undefined;
      try {
        if (reportId) {
          await loadReport(reportId);
        } else if (activeSubmissionId) {
          await pollSubmission(activeSubmissionId);
        }
      } catch {
        // loadReport / pollSubmission already surface errors
      }
      setActiveSubmissionId(null);
      setRunStartedAt(null);
    },
    onError: (errorMessage, logs) => {
      setStreamConnected(false);
      setSubmissionStatus(SubmissionStatus.FAILED);
      setSubmitError(errorMessage);
      appendActivity(errorMessage);
      message.error(errorMessage);
      if (logs) {
        setRunnerLogs(logs);
      }
      if (activeSubmissionId) {
        void pollSubmission(activeSubmissionId);
      }
      setRunStartedAt(null);
    },
  });

  useEffect(() => {
    if (!activeSubmissionId) {
      return;
    }
    const interval = window.setInterval(() => {
      void pollSubmission(activeSubmissionId);
    }, 1000);
    return () => window.clearInterval(interval);
  }, [activeSubmissionId, pollSubmission]);

  useEffect(() => {
    if (report?.runnerLogs) {
      setRunnerLogs(report.runnerLogs);
    }
  }, [report]);

  useEffect(() => {
    if (report) {
      scrollToCoach();
    }
  }, [report, scrollToCoach]);

  const cancelMutation = useMutation({
    mutationFn: (submissionId: string) =>
      apiFetch<void>(ApiPaths.submission(submissionId), { method: "DELETE" }),
    onSuccess: () => {
      setActiveSubmissionId(null);
      setSubmissionStatus(SubmissionStatus.CANCELLED);
      setSubmitError(null);
      message.info("Run cancelled");
    },
    onError: (e) =>
      message.error(
        e instanceof ApiError ? e.message : "Could not cancel submission",
      ),
  });

  const challenge = challengeQuery.data;
  const isRunning =
    submissionStatus === SubmissionStatus.PENDING
    || submissionStatus === SubmissionStatus.RUNNING
    || submitMutation.isPending;

  const runTests = useCallback(() => {
    if (!isRunning) {
      submitMutation.mutate();
    }
  }, [isRunning, submitMutation]);

  useRunTestsShortcut(Boolean(challenge) && !isRunning, runTests);

  const showLiveRun = isRunning;
  const isTerminal =
    submissionStatus === SubmissionStatus.COMPLETED
    || submissionStatus === SubmissionStatus.FAILED
    || submissionStatus === SubmissionStatus.CANCELLED;

  return (
    <AppLayout>
      <Breadcrumb
        className="ctl-breadcrumb mb-4"
        items={[
          { title: <Link to="/challenges">Challenges</Link> },
          { title: challenge?.title ?? slug },
        ]}
      />

      {challengeQuery.isLoading && (
        <div className="flex justify-center py-16">
          <Spin size="large" />
        </div>
      )}
      {challengeQuery.error && (
        <Alert
          type="error"
          showIcon
          role="alert"
          message={(challengeQuery.error as Error).message}
        />
      )}

      {challenge && (
        <>
          <WorkspaceToolbar
            challenge={challenge}
            runtimeVersion={runtimeVersion}
            onRuntimeChange={setRuntimeVersion}
            isRunning={isRunning}
            onRunTests={runTests}
            showCancel={
              Boolean(activeSubmissionId)
              && (submissionStatus === SubmissionStatus.PENDING
                || submissionStatus === SubmissionStatus.RUNNING)
            }
            onCancel={
              activeSubmissionId
                ? () => cancelMutation.mutate(activeSubmissionId)
                : undefined
            }
            cancelLoading={cancelMutation.isPending}
            onResetStarter={() => setSolutionCode(challenge.starterCode)}
            onSaveCustomTests={() => saveCustomTests.mutate(customTestsCode)}
            saveCustomTestsLoading={saveCustomTests.isPending}
            activeTab={workspaceTab}
          />

          <div className="grid gap-4 lg:grid-cols-[1fr_360px]">
            <div className="space-y-4">
              <ChallengeBriefCard
                challenge={challenge}
                runtimeVersion={runtimeVersion}
              />

              <CtlCard
                title="Workspace"
                extra={
                  <Typography.Text className="!text-slate-500 text-xs">
                    {workspaceTab === "solution" ? "Main solution" : "Optional JUnit tests"}
                  </Typography.Text>
                }
              >
                <Tabs
                  activeKey={workspaceTab}
                  onChange={(key) => setWorkspaceTab(key as "solution" | "custom")}
                  items={[
                    {
                      key: "solution",
                      label: "Solution",
                      children: (
                        <div className="ctl-editor-frame h-[min(520px,60vh)]">
                          <JavaLspEditor
                            height="100%"
                            value={solutionCode}
                            onChange={setSolutionCode}
                            lspEnabled={lspEnabled}
                          />
                        </div>
                      ),
                    },
                    {
                      key: "custom",
                      label: "Custom tests",
                      children: (
                        <div className="ctl-editor-frame h-[min(480px,55vh)]">
                          <Editor
                            height="100%"
                            language="java"
                            theme="vs-dark"
                            value={customTestsCode}
                            onChange={(v) => setCustomTestsCode(v ?? "")}
                            options={{
                              minimap: { enabled: false },
                              fontSize: 14,
                              automaticLayout: true,
                            }}
                          />
                        </div>
                      ),
                    },
                  ]}
                />
              </CtlCard>
            </div>

            <aside
              className="space-y-4 lg:sticky lg:top-[4.5rem] lg:max-h-[calc(100vh-6rem)] lg:overflow-y-auto lg:self-start"
              aria-label="Run status and logs"
            >
              {submitError && (
                <Alert type="error" showIcon role="alert" message={submitError} />
              )}
              {runnerLogs &&
                (runnerLogs.stdoutTruncated || runnerLogs.stderrTruncated) && (
                  <CtlCard title="Runner logs">
                    {runnerLogs.stderrTruncated && (
                      <pre className="mb-2 max-h-40 overflow-auto whitespace-pre-wrap text-xs text-red-300">
                        {runnerLogs.stderrTruncated}
                      </pre>
                    )}
                    {runnerLogs.stdoutTruncated && (
                      <pre className="max-h-40 overflow-auto whitespace-pre-wrap text-xs text-slate-300">
                        {runnerLogs.stdoutTruncated}
                      </pre>
                    )}
                  </CtlCard>
                )}
              {showLiveRun && (
                <RunProgressPanel
                  submissionStatus={submissionStatus}
                  isSubmitting={submitMutation.isPending}
                  streamConnected={streamConnected}
                  streamReconnecting={streamReconnecting}
                  activityLog={activityLog}
                  trackedTests={trackedTests}
                  hiddenTestCount={challenge.hiddenTestCount}
                  runtimeVersion={runtimeVersion}
                  runStartedAt={runStartedAt}
                />
              )}

              {!showLiveRun && !isTerminal && !submissionStatus && (
                <CtlCard title="Ready to run">
                  <Typography.Text className="!text-slate-400">
                    Press <strong>Run tests</strong> or <strong>⌘/Ctrl + Enter</strong>.
                    Live progress appears here; feedback loads at the bottom when the run
                    finishes.
                  </Typography.Text>
                </CtlCard>
              )}

              {isTerminal && !showLiveRun && report && (
                <CtlCard title="Last run">
                  <Typography.Text className="!text-slate-400 text-sm">
                    Run finished. Scroll down for the summary and AI coach, then refine
                    your solution and run again.
                  </Typography.Text>
                </CtlCard>
              )}
            </aside>
          </div>

          <section
            ref={coachRef}
            className="mt-6 space-y-4 border-t border-slate-800/80 pt-6"
            aria-label="Run feedback and AI coach"
          >
            {report && (
              <RunResultBanner report={report} onScrollToCoach={scrollToCoach} />
            )}

            {reportLoading && !report && (
              <CtlCard>
                <div className="flex items-center gap-3 py-4" role="status" aria-live="polite">
                  <Spin />
                  <Typography.Text className="!text-slate-300">
                    Loading AI coach report…
                  </Typography.Text>
                </div>
              </CtlCard>
            )}

            {report && (
              <AiCoachPanel
                report={report}
                challengeSlug={slug}
                onReportUpdate={setReport}
              />
            )}

            {isTerminal && !report && !reportLoading && (
              <Alert
                type="info"
                showIcon
                message="AI coach not available yet"
                description={
                  submissionStatus === SubmissionStatus.FAILED
                    ? "This run failed before a report was created. Check runner logs on the right, fix the issue, and run tests again."
                    : "Finish a successful test run to unlock feedback and Ask AI coach below."
                }
              />
            )}
          </section>
        </>
      )}
    </AppLayout>
  );
}
