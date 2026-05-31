import { useEffect, useRef } from "react";
import { getAccessToken } from "../auth/authStorage";
import type { RunnerLogs, TestResultEvent } from "../api/types";
import { ApiPaths, SubmissionEventType } from "../domain/constants";

export type SubmissionEventsHandlers = {
  onStatus?: (status: string, message?: string) => void;
  onTestResult?: (test: TestResultEvent) => void;
  onDone?: (payload: Record<string, string | undefined>) => void;
  onError?: (message: string, logs?: RunnerLogs | null) => void;
  onStreamOpen?: () => void;
  onStreamReconnecting?: () => void;
};

const MAX_SSE_RETRIES = 8;
const INITIAL_RETRY_MS = 1000;
const MAX_RETRY_MS = 10_000;

export function useSubmissionEvents(
  submissionId: string | null,
  enabled: boolean,
  handlers: SubmissionEventsHandlers,
) {
  const handlersRef = useRef(handlers);
  handlersRef.current = handlers;

  useEffect(() => {
    if (!enabled || !submissionId) {
      return;
    }
    const token = getAccessToken();
    if (!token) {
      return;
    }

    const url = `${ApiPaths.submissionEvents(submissionId)}?access_token=${encodeURIComponent(token)}`;
    let cancelled = false;
    let source: EventSource | null = null;
    let retryAttempt = 0;
    let retryTimer: ReturnType<typeof setTimeout> | undefined;
    let terminal = false;

    const parse = <T,>(event: MessageEvent): T => JSON.parse(event.data as string) as T;

    const attachListeners = (es: EventSource) => {
      es.addEventListener(SubmissionEventType.STATUS, (event) => {
        const data = parse<{ status: string; message?: string }>(event);
        handlersRef.current.onStatus?.(data.status, data.message);
      });

      es.addEventListener(SubmissionEventType.TEST_RESULT, (event) => {
        handlersRef.current.onTestResult?.(parse<TestResultEvent>(event));
      });

      es.addEventListener(SubmissionEventType.DONE, (event) => {
        terminal = true;
        handlersRef.current.onDone?.(
          parse<{ submission_id?: string; report_id?: string; reportId?: string }>(
            event,
          ),
        );
      });

      es.addEventListener(SubmissionEventType.ERROR, (event) => {
        if (event instanceof MessageEvent && event.data) {
          terminal = true;
          const data = parse<{
            message?: string;
            stdout?: string;
            stderr?: string;
          }>(event);
          const stdout = data.stdout;
          const stderr = data.stderr;
          const logs =
            stdout || stderr
              ? {
                  stdoutTruncated: stdout ?? "",
                  stderrTruncated: stderr ?? "",
                }
              : null;
          handlersRef.current.onError?.(data.message ?? "Submission failed", logs);
        }
      });
    };

    const connect = () => {
      if (cancelled || terminal) {
        return;
      }
      source?.close();
      source = new EventSource(url);
      attachListeners(source);

      source.onopen = () => {
        retryAttempt = 0;
        handlersRef.current.onStreamOpen?.();
      };

      source.onerror = () => {
        source?.close();
        if (cancelled || terminal) {
          return;
        }
        if (retryAttempt < MAX_SSE_RETRIES) {
          handlersRef.current.onStreamReconnecting?.();
          const delay = Math.min(
            INITIAL_RETRY_MS * 2 ** retryAttempt,
            MAX_RETRY_MS,
          );
          retryAttempt += 1;
          retryTimer = setTimeout(connect, delay);
          return;
        }
        handlersRef.current.onError?.("Lost connection to submission stream");
      };
    };

    connect();

    return () => {
      cancelled = true;
      if (retryTimer) {
        clearTimeout(retryTimer);
      }
      source?.close();
    };
  }, [submissionId, enabled]);
}
