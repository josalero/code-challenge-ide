import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App } from "antd";
import { useCallback, useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { apiFetch, ApiError } from "../api/client";
import type {
  ChallengeDetail,
  CustomTestsResponse,
  ProgressEntry,
  ReportResponse,
  RunnerLogs,
  SubmissionResponse,
} from "../api/types";
import AppLayout from "../components/AppLayout";
import WorkspaceShell from "../components/workspace/WorkspaceShell";
import type { BottomPanelTab } from "../components/workspace/WorkspaceBottomPanel";
import type { AttemptRecord } from "../components/workspace/AttemptHistoryTab";
import type { ActivityEntry, ActivityKind, TrackedTest } from "../domain/runProgressTypes";
import { activityEntryFromTestResult } from "../utils/activityLog";
import {
  ApiPaths,
  JavaRuntimeVersion,
  SsePayloadKeys,
  SubmissionKind,
  SubmissionStatus,
} from "../domain/constants";
import { latestActiveRuntimeVersion } from "../utils/languageRuntimes";
import type { SubmissionKindValue, SubmissionStatusValue } from "../domain/constants";
import {
  deriveWorkspaceRunPhase,
} from "../domain/workspaceRunState";
import { useAutosaveDraft } from "../hooks/useAutosaveDraft";
import { useRunTestsShortcut } from "../hooks/useRunTestsShortcut";
import { useSubmissionEvents } from "../hooks/useSubmissionEvents";
import {
  applyTestResult,
  buildInitialTrackedTests,
  finalizeTrackedTestsOnComplete,
} from "../utils/submissionProgress";

export default function ChallengeWorkspacePage() {
  const { slug = "" } = useParams();
  const queryClient = useQueryClient();
  const { message } = App.useApp();

  const [solutionCode, setSolutionCode] = useState("");
  const [customTestsCode, setCustomTestsCode] = useState("");
  const [runtimeVersion, setRuntimeVersion] = useState<string>(JavaRuntimeVersion.DEFAULT);
  const [workspaceTab, setWorkspaceTab] = useState<"solution" | "custom">("solution");
  const [bottomTab, setBottomTab] = useState<BottomPanelTab>("tests");
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
  const [attempts, setAttempts] = useState<AttemptRecord[]>([]);
  const [activeSubmissionKind, setActiveSubmissionKind] =
    useState<SubmissionKindValue | null>(null);
  const [lastRunPassed, setLastRunPassed] = useState<boolean | null>(null);

  const appendActivity = useCallback((msg: string, kind: ActivityKind = "info") => {
    setActivityLog((prev) => [
      ...prev,
      { id: `${Date.now()}-${prev.length}`, at: Date.now(), message: msg, kind },
    ]);
  }, []);

  const challengeQuery = useQuery({
    queryKey: ["challenge", slug],
    queryFn: () => apiFetch<ChallengeDetail>(ApiPaths.challenge(slug)),
    enabled: Boolean(slug),
  });

  const progressQuery = useQuery({
    queryKey: ["me", "progress"],
    queryFn: () => apiFetch<ProgressEntry[]>(ApiPaths.ME_PROGRESS),
  });

  const exerciseLocked =
    progressQuery.data?.find((entry) => entry.challengeSlug === slug)?.submitted ?? false;

  const customTestsQuery = useQuery({
    queryKey: ["custom-tests", slug],
    queryFn: () =>
      apiFetch<CustomTestsResponse>(ApiPaths.challengeCustomTests(slug)),
    enabled: Boolean(slug),
  });

  const { status: autosaveStatus, loadDraft } = useAutosaveDraft(
    slug,
    solutionCode,
    Boolean(slug) && Boolean(challengeQuery.data),
  );

  useEffect(() => {
    return () => {
      initializedSlug.current = null;
    };
  }, [slug]);

  useEffect(() => {
    const detail = challengeQuery.data;
    if (!detail || detail.slug !== slug) {
      return;
    }
    if (initializedSlug.current === slug) {
      return;
    }
    initializedSlug.current = slug;
    const draft = loadDraft();
    setSolutionCode(draft ?? detail.starterCode);
    const defaultRuntime = latestActiveRuntimeVersion(
      detail.runtimes,
      JavaRuntimeVersion.DEFAULT,
    );
    setRuntimeVersion(defaultRuntime);
    setReport(null);
    setSubmissionStatus(null);
    setSubmitError(null);
    setAttempts([]);
    lastToastReportId.current = null;
  }, [slug, challengeQuery.data, loadDraft]);

  useEffect(() => {
    if (!customTestsQuery.data) {
      return;
    }
    setCustomTestsCode(customTestsQuery.data.code);
  }, [slug, customTestsQuery.data]);

  const scrollToCoach = useCallback(() => {
    setBottomTab("feedback");
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

  const beginExecution = useCallback(
    (kind: SubmissionKindValue) => {
      setActiveSubmissionKind(kind);
      setSubmitError(null);
      setReport(null);
      setReportLoading(false);
      setRunnerLogs(null);
      setActivityLog([]);
      setStreamConnected(false);
      setStreamReconnecting(false);
      setRunStartedAt(Date.now());
      setBottomTab("tests");
      if (kind === SubmissionKind.RUN) {
        setLastRunPassed(null);
      }
      if (challengeQuery.data) {
        setTrackedTests(buildInitialTrackedTests(challengeQuery.data));
      }
      setSubmissionStatus(SubmissionStatus.PENDING);
      appendActivity(
        kind === SubmissionKind.RUN
          ? "Starting practice run…"
          : "Submitting final solution…",
      );
    },
    [appendActivity, challengeQuery.data],
  );

  const submitMutation = useMutation({
    mutationFn: async (kind: SubmissionKindValue) =>
      apiFetch<SubmissionResponse>(ApiPaths.SUBMISSIONS, {
        method: "POST",
        headers: { "Idempotency-Key": crypto.randomUUID() },
        body: JSON.stringify({
          challengeSlug: slug,
          runtimeVersion,
          solutionCode,
          customTestsCode: customTestsCode.trim() || null,
          kind,
        }),
      }),
    onMutate: (kind) => {
      beginExecution(kind);
    },
    onSuccess: (submission) => {
      setActiveSubmissionId(submission.id);
      setSubmissionStatus(submission.status as SubmissionStatusValue);
      appendActivity("Submission created — connecting live stream…");
      setAttempts((prev) => [
        {
          id: submission.id,
          status: submission.status as SubmissionStatusValue,
          createdAt: submission.createdAt,
          passed: null,
        },
        ...prev,
      ]);
    },
    onError: (e) => {
      const msg = e instanceof ApiError ? e.message : "Execution failed";
      setSubmitError(msg);
      setSubmissionStatus(null);
      setActiveSubmissionId(null);
      setActiveSubmissionKind(null);
      message.error(msg);
    },
  });

  const redoMutation = useMutation({
    mutationFn: () =>
      apiFetch<void>(ApiPaths.challengeRedo(slug), { method: "POST" }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["me", "progress"] });
      setReport(null);
      setSubmitError(null);
      setSubmissionStatus(null);
      setActiveSubmissionId(null);
      setActiveSubmissionKind(null);
      setLastRunPassed(null);
      setTrackedTests([]);
      message.success("Exercise unlocked — you can edit and submit again");
    },
    onError: (e) =>
      message.error(e instanceof ApiError ? e.message : "Could not redo exercise"),
  });

  const loadReport = useCallback(
    async (reportId: string) => {
      setReportLoading(true);
      try {
        const loaded = await apiFetch<ReportResponse>(ApiPaths.report(reportId));
        setReport(loaded);
        setAttempts((prev) =>
          prev.map((a) =>
            a.id === loaded.submissionId
              ? {
                  ...a,
                  passed: !loaded.blocked,
                  summary: loaded.summary,
                }
              : a,
          ),
        );
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
      message.warning("Submission complete — review feedback for next steps");
    } else {
      message.success("Submitted — all checks passed!");
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
      setActivityLog((prev) => [...prev, activityEntryFromTestResult(test)]);
    },
    onDone: async (payload) => {
      setStreamConnected(false);
      setSubmissionStatus((prev) =>
        prev === SubmissionStatus.FAILED ? prev : SubmissionStatus.COMPLETED);
      setTrackedTests((prev) => finalizeTrackedTestsOnComplete(prev));
      const kind =
        (payload[SsePayloadKeys.KIND] as SubmissionKindValue | undefined)
        ?? activeSubmissionKind
        ?? SubmissionKind.SUBMIT;
      if (kind === SubmissionKind.RUN) {
        const passed = payload[SsePayloadKeys.PASSED] === "true";
        setLastRunPassed(passed);
        appendActivity(
          payload[SsePayloadKeys.MESSAGE]
            ?? (passed ? "All tests passed — keep editing or submit when ready" : "Some tests failed — fix your solution and run again"),
          passed ? "success" : "warning",
        );
        if (passed) {
          message.success("All tests passed — keep editing or submit when ready");
        } else {
          message.warning("Some tests failed — fix and run again, or submit for full feedback");
        }
        setActiveSubmissionId(null);
        setActiveSubmissionKind(null);
        setRunStartedAt(null);
        return;
      }
      appendActivity("Submit finished — loading coach report…");
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
        void queryClient.invalidateQueries({ queryKey: ["me", "progress"] });
        setBottomTab("feedback");
      } catch {
        // loadReport / pollSubmission already surface errors
      }
      setActiveSubmissionId(null);
      setActiveSubmissionKind(null);
      setRunStartedAt(null);
    },
    onError: (errorMessage, logs) => {
      setStreamConnected(false);
      setSubmissionStatus(SubmissionStatus.FAILED);
      setTrackedTests((prev) => finalizeTrackedTestsOnComplete(prev));
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
    if (!activeSubmissionId || streamConnected) {
      return;
    }
    const interval = window.setInterval(() => {
      void pollSubmission(activeSubmissionId);
    }, 3000);
    return () => window.clearInterval(interval);
  }, [activeSubmissionId, pollSubmission, streamConnected]);

  useEffect(() => {
    if (report?.runnerLogs) {
      setRunnerLogs(report.runnerLogs);
    }
  }, [report]);

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
    if (!isRunning && !exerciseLocked) {
      submitMutation.mutate(SubmissionKind.RUN);
    }
  }, [isRunning, exerciseLocked, submitMutation]);

  const submitSolution = useCallback(() => {
    if (!isRunning && !exerciseLocked) {
      submitMutation.mutate(SubmissionKind.SUBMIT);
    }
  }, [isRunning, exerciseLocked, submitMutation]);

  useRunTestsShortcut(Boolean(challenge) && !isRunning && !exerciseLocked, runTests);

  const showLiveRun = isRunning;
  const isTerminal =
    submissionStatus === SubmissionStatus.COMPLETED
    || submissionStatus === SubmissionStatus.FAILED
    || submissionStatus === SubmissionStatus.CANCELLED;

  const runPhase = deriveWorkspaceRunPhase({
    challengeLoading: challengeQuery.isLoading,
    isRunning,
    submissionStatus,
    submitError,
    report,
    trackedTests,
    runnerLogs,
    lastRunPassed,
    exerciseLocked,
  });

  return (
    <AppLayout variant="workspace">
      <div className="flex h-full min-h-0 flex-1 flex-col overflow-hidden">
      {challengeQuery.error && (
        <div
          className="shrink-0 border-b border-destructive/40 bg-destructive/10 px-4 py-2 text-sm text-red-200"
          role="alert"
        >
          {(challengeQuery.error as Error).message}
        </div>
      )}

      {challenge && (
        <div ref={coachRef} className="flex min-h-0 flex-1 flex-col overflow-hidden">
          <WorkspaceShell
            slug={slug}
            challenge={challenge}
            runtimeVersion={runtimeVersion}
            onRuntimeChange={setRuntimeVersion}
            solutionCode={solutionCode}
            customTestsCode={customTestsCode}
            onSolutionChange={setSolutionCode}
            onCustomTestsChange={setCustomTestsCode}
            workspaceTab={workspaceTab}
            onWorkspaceTabChange={setWorkspaceTab}
            isRunning={isRunning}
            runPhase={runPhase}
            autosaveStatus={autosaveStatus}
            onRunTests={runTests}
            onSubmit={submitSolution}
            exerciseLocked={exerciseLocked}
            onRedo={() => redoMutation.mutate()}
            redoLoading={redoMutation.isPending}
            onCancel={
              activeSubmissionId
                ? () => cancelMutation.mutate(activeSubmissionId)
                : undefined
            }
            cancelLoading={cancelMutation.isPending}
            showCancel={
              Boolean(activeSubmissionId)
              && (submissionStatus === SubmissionStatus.PENDING
                || submissionStatus === SubmissionStatus.RUNNING)
            }
            onResetStarter={() => setSolutionCode(challenge.starterCode)}
            onSaveCustomTests={() => saveCustomTests.mutate(customTestsCode)}
            saveCustomTestsLoading={saveCustomTests.isPending}
            bottomTab={bottomTab}
            onBottomTabChange={setBottomTab}
            submissionStatus={submissionStatus}
            isSubmitting={submitMutation.isPending}
            streamConnected={streamConnected}
            streamReconnecting={streamReconnecting}
            activityLog={activityLog}
            trackedTests={trackedTests}
            runStartedAt={runStartedAt}
            runnerLogs={runnerLogs}
            report={report}
            reportLoading={reportLoading}
            onReportUpdate={setReport}
            onScrollToCoach={scrollToCoach}
            attempts={attempts}
            showLiveRun={showLiveRun}
            isTerminal={isTerminal}
            submitError={submitError}
            loading={challengeQuery.isLoading}
          />
        </div>
      )}

      {!challenge && challengeQuery.isLoading && (
        <WorkspaceShell
          slug={slug}
          challenge={{
            slug,
            title: "Loading…",
            descriptionMd: "",
            starterCode: "",
            difficulty: "",
            language: "java",
            gatingConfig: "",
            publicTests: [],
            hiddenTestCount: 0,
            runtimes: [{ version: JavaRuntimeVersion.DEFAULT, active: true }],
          }}
          runtimeVersion={runtimeVersion}
          onRuntimeChange={setRuntimeVersion}
          solutionCode=""
          customTestsCode=""
          onSolutionChange={() => {}}
          onCustomTestsChange={() => {}}
          workspaceTab="solution"
          onWorkspaceTabChange={() => {}}
          isRunning={false}
          runPhase="loading"
          autosaveStatus="idle"
          onRunTests={() => {}}
          onSubmit={() => {}}
          showCancel={false}
          onResetStarter={() => {}}
          bottomTab="tests"
          onBottomTabChange={() => {}}
          submissionStatus={null}
          isSubmitting={false}
          streamConnected={false}
          streamReconnecting={false}
          activityLog={[]}
          trackedTests={[]}
          runStartedAt={null}
          runnerLogs={null}
          report={null}
          reportLoading={false}
          onReportUpdate={() => {}}
          onScrollToCoach={() => {}}
          attempts={[]}
          showLiveRun={false}
          isTerminal={false}
          submitError={null}
          loading
        />
      )}
      </div>
    </AppLayout>
  );
}
