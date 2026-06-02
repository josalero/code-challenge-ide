import { ExperimentOutlined, RobotOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Button, Segmented, Space, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import { apiFetch, ApiError } from "../api/client";
import type {
  FeedbackActionResponse,
  FeedbackActionStatusValue,
  FeedbackActionTypeValue,
} from "../api/types";
import { ApiPaths } from "../domain/constants";
import CoachMarkdown from "./CoachMarkdown";
import CtlCard from "./ui/CtlCard";

type Props = {
  submissionId: string;
};

type ActionOption = {
  value: FeedbackActionTypeValue;
  label: string;
  description: string;
  available: boolean;
};

const ACTIONS: ActionOption[] = [
  {
    value: "COACH",
    label: "AI coach review",
    description:
      "LLM review of your submitted code: hints, alternative approaches, and short illustrative samples — not full solutions.",
    available: true,
  },
  {
    value: "SONAR",
    label: "Sonar scan",
    description: "Full SonarQube scan with quality gate (coming soon).",
    available: false,
  },
  {
    value: "COMPLEXITY",
    label: "Complexity report",
    description: "Cyclomatic and cognitive complexity per function (coming soon).",
    available: false,
  },
];

function statusColor(status: FeedbackActionStatusValue): string {
  switch (status) {
    case "COMPLETED":
      return "success";
    case "FAILED":
      return "error";
    case "RUNNING":
      return "processing";
    default:
      return "default";
  }
}

const TERMINAL_STATUSES: ReadonlySet<FeedbackActionStatusValue> = new Set([
  "COMPLETED",
  "FAILED",
]);

export default function FeedbackActionsPanel({ submissionId }: Props) {
  const queryClient = useQueryClient();
  const [selected, setSelected] = useState<FeedbackActionTypeValue>("COACH");
  const [submitError, setSubmitError] = useState<string | null>(null);

  const listKey = useMemo(() => ["feedback-actions", submissionId], [submissionId]);

  const listQuery = useQuery({
    queryKey: listKey,
    queryFn: () =>
      apiFetch<FeedbackActionResponse[]>(ApiPaths.submissionFeedbackActions(submissionId)),
    refetchInterval: (query) => {
      const data = query.state.data as FeedbackActionResponse[] | undefined;
      if (!data || data.length === 0) {
        return false;
      }
      const hasPending = data.some((a) => !TERMINAL_STATUSES.has(a.status));
      return hasPending ? 2_000 : false;
    },
  });

  const requestMutation = useMutation({
    mutationFn: (action: FeedbackActionTypeValue) =>
      apiFetch<FeedbackActionResponse>(ApiPaths.submissionFeedbackActions(submissionId), {
        method: "POST",
        body: JSON.stringify({ action }),
      }),
    onSuccess: () => {
      setSubmitError(null);
      queryClient.invalidateQueries({ queryKey: listKey });
    },
    onError: (e) => {
      setSubmitError(e instanceof ApiError ? e.message : "Could not request feedback");
    },
  });

  const selectedOption = ACTIONS.find((a) => a.value === selected);
  const actions = listQuery.data ?? [];

  return (
    <CtlCard
      title={
        <Space>
          <ExperimentOutlined className="text-indigo-400" />
          <span>Request deeper feedback</span>
        </Space>
      }
    >
      <Typography.Paragraph className="!text-slate-400 !mb-3">
        Submissions only run tests and coverage by default. Pick an analyzer below to get
        additional, on-demand feedback.
      </Typography.Paragraph>

      <Segmented
        className="!mb-3"
        block
        value={selected}
        onChange={(v) => setSelected(v as FeedbackActionTypeValue)}
        options={ACTIONS.map((a) => ({
          value: a.value,
          label: a.available ? a.label : `${a.label} (soon)`,
        }))}
      />

      {selectedOption && (
        <Typography.Paragraph className="!text-slate-300 !mb-3">
          {selectedOption.description}
        </Typography.Paragraph>
      )}

      <Button
        type="primary"
        icon={<RobotOutlined />}
        disabled={!selectedOption?.available}
        loading={requestMutation.isPending}
        onClick={() => requestMutation.mutate(selected)}
      >
        Request feedback
      </Button>

      {submitError && (
        <Alert className="!mt-3" type="error" showIcon message={submitError} closable />
      )}

      {actions.length > 0 && (
        <div className="mt-5 space-y-3">
          <Typography.Title level={5} className="!text-slate-200 !mb-2">
            Recent requests
          </Typography.Title>
          {actions.map((action) => (
            <div
              key={action.id}
              className="rounded border border-slate-700 bg-slate-900/60 p-3"
            >
              <Space>
                <Tag>{action.action}</Tag>
                <Tag color={statusColor(action.status)}>{action.status}</Tag>
                <Typography.Text className="!text-slate-400 text-xs">
                  {new Date(action.createdAt).toLocaleTimeString()}
                </Typography.Text>
              </Space>
              {action.errorMessage && (
                <Alert
                  className="!mt-2"
                  type="warning"
                  showIcon
                  message={action.errorMessage}
                />
              )}
              {action.result && (
                <div className="mt-2">
                  <CoachMarkdown text={action.result} />
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </CtlCard>
  );
}
