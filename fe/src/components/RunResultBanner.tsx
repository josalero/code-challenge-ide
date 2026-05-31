import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  RobotOutlined,
  WarningOutlined,
} from "@ant-design/icons";
import { Alert, Button, Space, Statistic, Typography } from "antd";
import type { ReportResponse } from "../api/types";
import { countFeedback, parseReportSummary } from "../utils/reportStats";

type Props = {
  report: ReportResponse;
  onScrollToCoach: () => void;
};

export default function RunResultBanner({ report, onScrollToCoach }: Props) {
  const counts = countFeedback(report);
  const meta = parseReportSummary(report.summary);
  const passed = !report.blocked;

  return (
    <Alert
      className="mb-4 rounded-xl border-slate-700/80"
      type={passed ? "success" : "warning"}
      showIcon
      icon={passed ? <CheckCircleOutlined /> : <WarningOutlined />}
      message={
        <span className="font-medium">
          {passed ? "All gates passed — nice work!" : "Not quite there yet"}
        </span>
      }
      description={
        <div className="mt-2 space-y-3">
          <Typography.Text className="!text-slate-300">
            {passed
              ? "You met correctness, coverage, and style requirements for this challenge."
              : "Review the feedback below and run tests again after each change."}
          </Typography.Text>
          <Space wrap size="large" className="!text-slate-200">
            {meta.tests != null && (
              <Statistic
                title={<span className="text-slate-500 text-xs">Tests executed</span>}
                value={meta.tests}
                valueStyle={{ fontSize: "1.25rem", color: "#e2e8f0" }}
              />
            )}
            <Statistic
              title={<span className="text-slate-500 text-xs">Checks passed</span>}
              value={counts.pass}
              suffix={`/ ${counts.total}`}
              valueStyle={{ fontSize: "1.25rem", color: "#34d399" }}
            />
            {counts.fail > 0 && (
              <Statistic
                title={<span className="text-slate-500 text-xs">Must fix</span>}
                value={counts.fail}
                prefix={<CloseCircleOutlined className="text-red-400" />}
                valueStyle={{ fontSize: "1.25rem", color: "#f87171" }}
              />
            )}
            {counts.warn > 0 && (
              <Statistic
                title={<span className="text-slate-500 text-xs">Warnings</span>}
                value={counts.warn}
                valueStyle={{ fontSize: "1.25rem", color: "#fbbf24" }}
              />
            )}
          </Space>
          <Button type="primary" icon={<RobotOutlined />} onClick={onScrollToCoach}>
            View AI coach below
          </Button>
        </div>
      }
    />
  );
}
