import { BulbOutlined, RobotOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import { Alert, Button, List, Segmented, Space, Spin, Tag, Typography } from "antd";
import CoachMarkdown from "./CoachMarkdown";
import CtlCard from "./ui/CtlCard";
import { useMemo, useState } from "react";
import { apiFetch, ApiError } from "../api/client";
import type {
  AlternativesResponse,
  ExplainResponse,
  FeedbackItem,
  ReportResponse,
} from "../api/types";
import {
  ApiPaths,
  FeedbackCategory,
  FeedbackStatus,
  type FeedbackCategoryValue,
} from "../domain/constants";
import { countFeedback } from "../utils/reportStats";

const CATEGORY_LABELS: Record<FeedbackCategoryValue, string> = {
  [FeedbackCategory.CORRECTNESS]: "Correctness",
  [FeedbackCategory.COVERAGE]: "Test coverage",
  [FeedbackCategory.STYLE]: "Code style",
  [FeedbackCategory.SECURITY]: "Security",
  [FeedbackCategory.READABILITY]: "Readability",
};

type FilterKey = "all" | "attention" | "passed";

type Props = {
  report: ReportResponse;
  challengeSlug: string;
  onReportUpdate: (report: ReportResponse) => void;
};

function feedbackSortOrder(status: string): number {
  if (status === FeedbackStatus.fail) {
    return 0;
  }
  if (status === FeedbackStatus.warn) {
    return 1;
  }
  return 2;
}

function statusTagColor(status: string): string {
  if (status === FeedbackStatus.pass) {
    return "success";
  }
  if (status === FeedbackStatus.warn) {
    return "warning";
  }
  return "error";
}

export default function AiCoachPanel({ report, challengeSlug, onReportUpdate }: Props) {
  const [filter, setFilter] = useState<FilterKey>("attention");
  const [alternatives, setAlternatives] = useState<string | null>(null);
  const [explainingId, setExplainingId] = useState<string | null>(null);
  const [explainError, setExplainError] = useState<string | null>(null);

  const counts = countFeedback(report);

  const sortedFeedback = useMemo(
    () =>
      [...report.feedback].sort(
        (a, b) => feedbackSortOrder(a.status) - feedbackSortOrder(b.status),
      ),
    [report.feedback],
  );

  const visibleFeedback = useMemo(() => {
    if (filter === "passed") {
      return sortedFeedback.filter((i) => i.status === FeedbackStatus.pass);
    }
    if (filter === "attention") {
      return sortedFeedback.filter((i) => i.status !== FeedbackStatus.pass);
    }
    return sortedFeedback;
  }, [sortedFeedback, filter]);

  const explainMutation = useMutation({
    mutationFn: (itemId: string) =>
      apiFetch<ExplainResponse>(ApiPaths.feedbackExplain(itemId), { method: "POST" }),
    onSuccess: (data, itemId) => {
      const updated: ReportResponse = {
        ...report,
        feedback: report.feedback.map((item) =>
          item.id === itemId ? { ...item, aiExplanation: data.explanation } : item,
        ),
      };
      onReportUpdate(updated);
      setExplainingId(null);
    },
    onError: (e) => {
      setExplainingId(null);
      setExplainError(
        e instanceof ApiError ? e.message : "Could not reach AI coach",
      );
    },
  });

  const alternativesMutation = useMutation({
    mutationFn: () =>
      apiFetch<AlternativesResponse>(ApiPaths.challengeAlternatives(challengeSlug), {
        method: "POST",
      }),
    onSuccess: (data) => setAlternatives(data.alternatives),
  });

  return (
    <CtlCard
      title={
        <Space>
          <RobotOutlined className="text-emerald-400" />
          <span>AI coach</span>
        </Space>
      }
      extra={
        <Tag color={report.blocked ? "error" : "success"}>
          {report.blocked ? "Keep practicing" : "Challenge passed"}
        </Tag>
      }
    >
      <Typography.Paragraph className="!text-slate-400 !mb-3">
        {counts.fail > 0
          ? `${counts.fail} check(s) need fixes before you pass. `
          : counts.warn > 0
            ? `${counts.warn} warning(s) to consider. `
            : "All automated checks passed. "}
        Use <strong>Ask AI coach</strong> to analyze your submitted code: hints, alternatives, and short
        code samples that illustrate patterns — not full solutions.
      </Typography.Paragraph>

      <Segmented
        className="!mb-4"
        value={filter}
        onChange={(v) => setFilter(v as FilterKey)}
        options={[
          { label: `Needs attention (${counts.fail + counts.warn})`, value: "attention" },
          { label: `Passed (${counts.pass})`, value: "passed" },
          { label: "All", value: "all" },
        ]}
      />

      {explainError && (
        <Alert
          className="!mb-4"
          type="error"
          showIcon
          message="AI coach error"
          description={explainError}
          closable
          onClose={() => setExplainError(null)}
        />
      )}

      <List
        dataSource={visibleFeedback}
        locale={{
          emptyText:
            filter === "attention"
              ? "Nothing blocking — check Passed or run tests again."
              : "No items in this view.",
        }}
        renderItem={(item: FeedbackItem) => (
          <List.Item className="!border-slate-700 !px-0">
            <div className="w-full space-y-2">
              <Space wrap>
                <Tag color={statusTagColor(item.status)}>{item.status}</Tag>
                <Typography.Text className="!text-slate-200 font-medium">
                  {CATEGORY_LABELS[item.category as FeedbackCategoryValue]
                    ?? item.category}
                </Typography.Text>
              </Space>
              <Typography.Text className="!text-slate-300 block">{item.message}</Typography.Text>
              {item.aiExplanation ? (
                <Alert
                  type="info"
                  showIcon
                  icon={<BulbOutlined />}
                  message="Coach insight"
                  description={<CoachMarkdown text={item.aiExplanation} />}
                />
              ) : (
                <Button
                  type="primary"
                  size="small"
                  icon={<RobotOutlined />}
                  loading={explainingId === item.id}
                  onClick={() => {
                    setExplainError(null);
                    setExplainingId(item.id);
                    explainMutation.mutate(item.id);
                  }}
                >
                  Ask AI coach
                </Button>
              )}
            </div>
          </List.Item>
        )}
      />

      {!report.blocked && (
        <div className="mt-4 border-t border-slate-700 pt-4">
          <Button
            icon={<BulbOutlined />}
            loading={alternativesMutation.isPending}
            onClick={() => alternativesMutation.mutate()}
          >
            Suggest other approaches
          </Button>
          {alternativesMutation.isPending && (
            <div className="mt-3 flex justify-center">
              <Spin />
            </div>
          )}
          {alternatives && (
            <Alert
              className="mt-3"
              type="success"
              showIcon
              message="Alternative approaches"
              description={<CoachMarkdown text={alternatives} />}
            />
          )}
        </div>
      )}
    </CtlCard>
  );
}
