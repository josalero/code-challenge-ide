import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  LoadingOutlined,
  MinusCircleOutlined,
} from "@ant-design/icons";
import { List, Space, Spin, Steps, Tag, Typography } from "antd";
import CtlCard from "./ui/CtlCard";
import { useEffect, useState } from "react";
import type { SubmissionStatusValue } from "../domain/constants";
import { SubmissionStatus } from "../domain/constants";

export type ActivityEntry = {
  id: string;
  at: number;
  message: string;
};

export type TrackedTest = {
  name: string;
  status: "pending" | "pass" | "fail" | "skip";
  message?: string;
};

type Props = {
  submissionStatus: SubmissionStatusValue | null;
  isSubmitting: boolean;
  streamConnected: boolean;
  streamReconnecting: boolean;
  activityLog: ActivityEntry[];
  trackedTests: TrackedTest[];
  hiddenTestCount: number;
  runtimeVersion: string;
  runStartedAt: number | null;
};

function stepIndex(
  status: SubmissionStatusValue | null,
  isSubmitting: boolean,
  hasTestResults: boolean,
): number {
  if (isSubmitting || status === SubmissionStatus.PENDING) {
    return 0;
  }
  if (status === SubmissionStatus.RUNNING && !hasTestResults) {
    return 1;
  }
  if (status === SubmissionStatus.RUNNING && hasTestResults) {
    return 2;
  }
  if (
    status === SubmissionStatus.COMPLETED
    || status === SubmissionStatus.FAILED
    || status === SubmissionStatus.CANCELLED
  ) {
    return 3;
  }
  return 0;
}

function testIcon(status: TrackedTest["status"]) {
  switch (status) {
    case "pass":
      return <CheckCircleOutlined className="text-green-500" />;
    case "fail":
      return <CloseCircleOutlined className="text-red-500" />;
    case "skip":
      return <MinusCircleOutlined className="text-slate-400" />;
    default:
      return <LoadingOutlined className="text-blue-400" />;
  }
}

export default function RunProgressPanel({
  submissionStatus,
  isSubmitting,
  streamConnected,
  streamReconnecting,
  activityLog,
  trackedTests,
  hiddenTestCount,
  runtimeVersion,
  runStartedAt,
}: Props) {
  const [elapsedSec, setElapsedSec] = useState(0);
  const isActive =
    isSubmitting
    || submissionStatus === SubmissionStatus.PENDING
    || submissionStatus === SubmissionStatus.RUNNING;

  useEffect(() => {
    if (!runStartedAt || !isActive) {
      setElapsedSec(0);
      return;
    }
    const tick = () =>
      setElapsedSec(Math.floor((Date.now() - runStartedAt) / 1000));
    tick();
    const id = window.setInterval(tick, 1000);
    return () => window.clearInterval(id);
  }, [runStartedAt, isActive]);

  const hasTestResults = trackedTests.some((t) => t.status !== "pending");
  const currentStep = stepIndex(submissionStatus, isSubmitting, hasTestResults);

  const pendingHidden = trackedTests.filter(
    (t) => t.name.startsWith("hidden:") && t.status === "pending",
  ).length;

  const finishedTests = trackedTests.filter((t) => t.status !== "pending");
  const passedCount = finishedTests.filter((t) => t.status === "pass").length;
  const failedCount = finishedTests.filter((t) => t.status === "fail").length;

  return (
    <CtlCard title="Live run">
      <Space direction="vertical" size="middle" className="w-full">
        <Space wrap>
          {submissionStatus && (
            <Tag
              color={
                submissionStatus === SubmissionStatus.COMPLETED
                  ? "success"
                  : submissionStatus === SubmissionStatus.FAILED
                    ? "error"
                    : submissionStatus === SubmissionStatus.CANCELLED
                      ? "default"
                      : "processing"
              }
            >
              {submissionStatus}
            </Tag>
          )}
          <Tag color="blue">Java {runtimeVersion}</Tag>
          {isActive && (
            <Tag
              color={streamConnected ? "success" : streamReconnecting ? "warning" : "default"}
            >
              {streamConnected
                ? "Live stream"
                : streamReconnecting
                  ? "Reconnecting…"
                  : "Connecting stream…"}
            </Tag>
          )}
        </Space>

        <Steps
          size="small"
          current={currentStep}
          status={
            submissionStatus === SubmissionStatus.FAILED ? "error" : undefined
          }
          items={[
            { title: "Submitted", description: "Queued on RabbitMQ" },
            {
              title: "Sandbox",
              description: `Docker runner (Java ${runtimeVersion})`,
            },
            { title: "Tests", description: "JUnit + coverage + Checkstyle" },
            { title: "Report", description: "Coach feedback" },
          ]}
        />

        {activityLog.length > 0 && (
          <div>
            <Typography.Text className="!text-slate-400 mb-2 block text-xs uppercase tracking-wide">
              Activity
            </Typography.Text>
            <List
              size="small"
              className="max-h-36 overflow-y-auto rounded border border-slate-700 bg-slate-950/50 px-2"
              dataSource={activityLog}
              renderItem={(entry) => (
                <List.Item className="!border-slate-800 !py-1">
                  <Space size="small">
                    <ClockCircleOutlined className="text-slate-500" />
                    <Typography.Text className="!text-slate-300 text-xs">
                      {entry.message}
                    </Typography.Text>
                  </Space>
                </List.Item>
              )}
            />
          </div>
        )}

        {trackedTests.length > 0 && (
          <div>
            <Typography.Text className="!text-slate-400 mb-2 block text-xs uppercase tracking-wide">
              Tests
              {pendingHidden > 0 && hiddenTestCount > 0 && (
                <span className="ml-1 normal-case">
                  ({pendingHidden} hidden still running)
                </span>
              )}
            </Typography.Text>
            <List
              size="small"
              dataSource={trackedTests}
              renderItem={(test) => (
                <List.Item className="!border-slate-800">
                  <Space direction="vertical" size={0} className="w-full">
                    <Space>
                      {test.status === "pending" ? (
                        <Spin size="small" />
                      ) : (
                        testIcon(test.status)
                      )}
                      <Typography.Text
                        className={
                          test.status === "pending"
                            ? "!text-slate-400"
                            : "!text-slate-200"
                        }
                      >
                        {test.name === "hidden:pending"
                          ? `${hiddenTestCount} hidden test(s)`
                          : test.name.startsWith("hidden:")
                            ? `Hidden: ${test.name.slice(7)}`
                            : test.name}
                      </Typography.Text>
                      {test.status !== "pending" && (
                        <Tag
                          color={
                            test.status === "pass"
                              ? "success"
                              : test.status === "fail"
                                ? "error"
                                : "default"
                          }
                        >
                          {test.status.toUpperCase()}
                        </Tag>
                      )}
                    </Space>
                    {test.message && (
                      <Typography.Text className="!text-slate-500 text-xs">
                        {test.message}
                      </Typography.Text>
                    )}
                  </Space>
                </List.Item>
              )}
            />
          </div>
        )}

        {!isActive && finishedTests.length > 0 && (
          <Typography.Text className="!text-slate-300 text-sm">
            Tests:{" "}
            <span className="text-green-400">{passedCount} passed</span>
            {failedCount > 0 && (
              <>
                {" "}
                · <span className="text-red-400">{failedCount} failed</span>
              </>
            )}
          </Typography.Text>
        )}

        {isActive && (
          <Typography.Text className="!text-slate-400 text-sm">
            {elapsedSec > 0 && (
              <span className="mr-2 font-medium text-slate-300">
                Elapsed {elapsedSec}s
              </span>
            )}
            Docker runs Maven (compile + tests + coverage). First run often takes
            2–3 minutes; repeat runs use the shared Maven cache and are faster.
          </Typography.Text>
        )}
      </Space>
    </CtlCard>
  );
}
