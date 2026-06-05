/** Mirrors backend enums — use these instead of string literals in UI code. */

export const SubmissionKind = {
  RUN: "RUN",
  SUBMIT: "SUBMIT",
} as const;

export type SubmissionKindValue = (typeof SubmissionKind)[keyof typeof SubmissionKind];

export const SubmissionStatus = {
  PENDING: "PENDING",
  RUNNING: "RUNNING",
  COMPLETED: "COMPLETED",
  FAILED: "FAILED",
  CANCELLED: "CANCELLED",
} as const;

export type SubmissionStatusValue =
  (typeof SubmissionStatus)[keyof typeof SubmissionStatus];

export const ProgressState = {
  NOT_STARTED: "NOT_STARTED",
  ATTEMPTED: "ATTEMPTED",
  PASSED: "PASSED",
  FAILED: "FAILED",
} as const;

export type ProgressStateValue = (typeof ProgressState)[keyof typeof ProgressState];

export const TestOutcomeStatus = {
  PASS: "PASS",
  FAIL: "FAIL",
  SKIP: "SKIP",
} as const;

export type TestOutcomeStatusValue =
  (typeof TestOutcomeStatus)[keyof typeof TestOutcomeStatus];

export const FeedbackCategory = {
  CORRECTNESS: "CORRECTNESS",
  COVERAGE: "COVERAGE",
  STYLE: "STYLE",
  SECURITY: "SECURITY",
  READABILITY: "READABILITY",
} as const;

export type FeedbackCategoryValue =
  (typeof FeedbackCategory)[keyof typeof FeedbackCategory];

export const FeedbackStatus = {
  pass: "pass",
  warn: "warn",
  fail: "fail",
} as const;

export const SubmissionEventType = {
  STATUS: "status",
  TEST_RESULT: "test_result",
  DONE: "done",
  ERROR: "error",
} as const;

/** SSE JSON payload field names (mirror backend SsePayloadKeys). */
export const SsePayloadKeys = {
  STATUS: "status",
  SUBMISSION_ID: "submission_id",
  REPORT_ID: "report_id",
  KIND: "kind",
  PASSED: "passed",
  MESSAGE: "message",
  STDOUT: "stdout",
  STDERR: "stderr",
} as const;

export const ApiPaths = {
  AUTH_LOGIN: "/api/v1/auth/login",
  AUTH_REGISTER: "/api/v1/auth/register",
  AUTH_REGISTRATION_INFO: "/api/v1/auth/registration-info",
  AUTH_ACCESS_REQUEST: "/api/v1/auth/access-request",
  AUTH_PASSWORD_REQUIREMENTS: "/api/v1/auth/password-requirements",
  ADMIN_USERS: "/api/v1/admin/users",
  adminUserChallengeReport: (userId: string) =>
    `/api/v1/admin/users/${userId}/challenge-report`,
  adminUserChallengeDetail: (userId: string, challengeSlug: string) =>
    `/api/v1/admin/users/${userId}/challenges/${encodeURIComponent(challengeSlug)}/detail`,
  adminUserDeactivate: (userId: string) => `/api/v1/admin/users/${userId}/deactivate`,
  adminUserChallengeQuota: (userId: string) => `/api/v1/admin/users/${userId}/challenge-quota`,
  adminUserIntegrityMonitoring: (userId: string) =>
    `/api/v1/admin/users/${userId}/integrity-monitoring`,
  ADMIN_DASHBOARD: "/api/v1/admin/dashboard",
  ADMIN_ACCESS_REQUESTS: "/api/v1/admin/access-requests",
  ME_PASSWORD: "/api/v1/me/password",
  CHALLENGES: "/api/v1/challenges",
  CHALLENGES_VALIDATE: "/api/v1/challenges/validate",
  LANGUAGES: "/api/v1/languages",
  ME: "/api/v1/me",
  ME_PROGRESS: "/api/v1/me/progress",
  ME_METRICS: "/api/v1/me/metrics",
  SUBMISSIONS: "/api/v1/submissions",
  LSP_JAVA: "/api/v1/lsp/java",
  lsp: (language: string) => `/api/v1/lsp/${language}`,
  OPS_DEAD_LETTER_SUBMISSIONS: "/api/v1/ops/dead-letter-submissions",
  OPS_DEAD_LETTER_REPLAY: "/api/v1/ops/dead-letter-submissions/replay",
  OPS_RUNNERS_STATUS: "/api/v1/ops/runners/status",
  OPS_RUNNERS_WARM_MAVEN: "/api/v1/ops/runners/warm/maven",
  OPS_RUNNERS_WARM_LSP: "/api/v1/ops/runners/warm/lsp",
  OPS_RUNNERS_WARM_POOL: "/api/v1/ops/runners/warm/pool",
  OPS_RUNNERS_WARM_INFRA: "/api/v1/ops/runners/warm/infra",
  opsRunnerJob: (jobId: string) => `/api/v1/ops/runners/jobs/${jobId}`,
  challenge: (slug: string) => `/api/v1/challenges/${slug}`,
  challengeRedo: (slug: string) => `/api/v1/challenges/${slug}/redo`,
  challengeSession: (slug: string) => `/api/v1/challenges/${slug}/session`,
  challengeSessionStart: (slug: string) => `/api/v1/challenges/${slug}/session/start`,
  challengeSessionAbandon: (slug: string) => `/api/v1/challenges/${slug}/session/abandon`,
  challengeSessionSync: (slug: string) => `/api/v1/challenges/${slug}/session/sync`,
  challengeCustomTests: (slug: string) => `/api/v1/challenges/${slug}/custom-tests`,
  challengeAlternatives: (slug: string) => `/api/v1/challenges/${slug}/alternatives`,
  submission: (id: string) => `/api/v1/submissions/${id}`,
  submissionEvents: (id: string) => `/api/v1/submissions/${id}/events`,
  report: (id: string) => `/api/v1/reports/${id}`,
  feedbackExplain: (itemId: string) => `/api/v1/feedback/${itemId}/explain`,
  submissionFeedbackActions: (submissionId: string) =>
    `/api/v1/submissions/${submissionId}/feedback-actions`,
  feedbackAction: (actionId: string) => `/api/v1/feedback-actions/${actionId}`,
} as const;

export const JavaRuntimeVersion = {
  DEFAULT: "26",
  SUPPORTED: ["25", "26"] as const,
} as const;
