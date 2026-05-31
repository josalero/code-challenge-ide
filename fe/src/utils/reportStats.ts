import type { ReportResponse } from "../api/types";
import { FeedbackStatus } from "../domain/constants";

export type FeedbackCounts = {
  pass: number;
  warn: number;
  fail: number;
  total: number;
};

export function countFeedback(report: ReportResponse): FeedbackCounts {
  const counts = { pass: 0, warn: 0, fail: 0, total: report.feedback.length };
  for (const item of report.feedback) {
    if (item.status === FeedbackStatus.pass) {
      counts.pass += 1;
    } else if (item.status === FeedbackStatus.warn) {
      counts.warn += 1;
    } else {
      counts.fail += 1;
    }
  }
  return counts;
}

export function parseReportSummary(summary: string): { tests?: number; blocked?: boolean } {
  try {
    return JSON.parse(summary) as { tests?: number; blocked?: boolean };
  } catch {
    return {};
  }
}
