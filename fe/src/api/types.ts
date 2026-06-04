export type UserRole = "ADMIN" | "USER";

export type AuthResponse = {
  accessToken: string;
  userId: string;
  email: string;
  role: UserRole;
  mustChangePassword: boolean;
};

export type MeResponse = {
  id: string;
  email: string;
  fullName: string | null;
  role: UserRole;
  mustChangePassword: boolean;
};

export type PasswordRequirementsResponse = {
  requirements: string[];
};

export type CreateUserRequest = {
  email: string;
  fullName: string;
  temporaryPassword: string;
  role: UserRole;
};

export type CreateUserResponse = {
  id: string;
  email: string;
  fullName: string;
  temporaryPassword: string;
  role: UserRole;
};

export type ChangePasswordRequest = {
  currentPassword: string;
  newPassword: string;
};

export type RegistrationInfoResponse = {
  registrationOpen: boolean;
  bootstrap: boolean;
};

export type ProgressEntry = {
  challengeSlug: string;
  state: string;
  submitted: boolean;
};

export type MetricsBreakdownRow = {
  label: string;
  total: number;
  passed: number;
  inProgress: number;
  notStarted: number;
};

export type MeMetricsResponse = {
  catalogTotal: number;
  notStarted: number;
  attempted: number;
  passed: number;
  failed: number;
  completionPercent: number;
  submissionsTotal: number;
  practiceRuns: number;
  gradedSubmits: number;
  submissionsCompleted: number;
  submissionsFailed: number;
  byLanguage: MetricsBreakdownRow[];
  byDifficulty: MetricsBreakdownRow[];
};

export type ChallengeSummary = {
  slug: string;
  title: string;
  difficulty: string;
  language: string;
};

export type ChallengeTestPayload = {
  name: string;
  source: string;
};

export type LanguageRuntimeOption = {
  language: string;
  version: string;
  active: boolean;
};

export type CreateChallengeRequest = {
  slug: string;
  title: string;
  descriptionMd: string;
  difficulty: string;
  language: string;
  defaultRuntimeVersion: string;
  starterCode: string;
  lineCoveragePercent: number;
  sessionDurationMinutes?: number | null;
  publicTests: ChallengeTestPayload[];
  hiddenTests: ChallengeTestPayload[];
};

/** Matches Spring Data PagedModel (pageSerializationMode = VIA_DTO). */
export type PageMetadata = {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
};

export type PageResponse<T> = {
  content: T[];
  page: PageMetadata;
};

export type RuntimeOption = {
  version: string;
  active: boolean;
};

export type PublicTestInfo = {
  name: string;
  description: string;
};

export type ChallengeDetail = {
  slug: string;
  title: string;
  descriptionMd: string;
  starterCode: string;
  difficulty: string;
  language: string;
  /** Allotted workspace time in minutes (from challenge.yml limits or DB). */
  sessionDurationMinutes: number;
  gatingConfig: string;
  publicTests: PublicTestInfo[];
  hiddenTestCount: number;
  runtimes: RuntimeOption[];
};

export type SubmissionResponse = {
  id: string;
  status: string;
  kind: string;
  reportId: string | null;
  createdAt: string;
};

export type FeedbackItem = {
  id: string;
  category: string;
  status: string;
  message: string;
  aiExplanation: string | null;
};

export type RunnerLogs = {
  stdoutTruncated: string;
  stderrTruncated: string;
};

export type ReportResponse = {
  id: string;
  submissionId: string;
  blocked: boolean;
  summary: string;
  feedback: FeedbackItem[];
  runnerLogs: RunnerLogs | null;
};

export type ExplainResponse = {
  explanation: string;
};

export type AlternativesResponse = {
  alternatives: string;
};

export const FeedbackActionTypes = ["COACH", "SONAR", "COMPLEXITY"] as const;
export type FeedbackActionTypeValue = (typeof FeedbackActionTypes)[number];

export type FeedbackActionStatusValue = "QUEUED" | "RUNNING" | "COMPLETED" | "FAILED";

export type FeedbackActionResponse = {
  id: string;
  submissionId: string;
  action: FeedbackActionTypeValue;
  status: FeedbackActionStatusValue;
  result: string | null;
  errorMessage: string | null;
  createdAt: string;
  updatedAt: string;
};

export type CustomTestsResponse = {
  code: string;
};

export type RunnerImageStatus = {
  label: string;
  image: string;
  present: boolean;
  imageId: string | null;
  /** true = warmed, false = cold, null = warm step not applicable */
  warmed: boolean | null;
};

export type LanguageWarmStatus = {
  language: string;
  version: string | null;
  label: string;
  runnerImage: string | null;
  runnerPresent: boolean;
  runnerReady: boolean | null;
  editorReady: boolean | null;
  ready: boolean;
};

export type RunnerOpsStatus = {
  dockerAvailable: boolean;
  dockerEnabled: boolean;
  mavenCacheWarm: boolean;
  lspScriptsAvailable: boolean;
  lspWarmStampPresent: boolean;
  runnerPoolWarmStampPresent: boolean;
  repoRoot: string;
  opsDataDir: string;
  mavenCacheVolume: string;
  runnerImages: RunnerImageStatus[];
  lspImages: RunnerImageStatus[];
  languages: LanguageWarmStatus[];
  activeJobId: string | null;
};

export type RunnerOpsJob = {
  id: string;
  type: string;
  status: string;
  startedAt: string | null;
  finishedAt: string | null;
  message: string;
  logTail: string;
};

export type TestResultEvent = {
  name: string;
  status: string;
  message?: string;
};
