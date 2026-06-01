export type UserRole = "ADMIN" | "USER";

export type AuthResponse = {
  accessToken: string;
  userId: string;
  email: string;
  role: UserRole;
};

export type MeResponse = {
  id: string;
  email: string;
  role: UserRole;
};

export type RegistrationInfoResponse = {
  registrationOpen: boolean;
  bootstrap: boolean;
};

export type ProgressEntry = {
  challengeSlug: string;
  state: string;
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
  gatingConfig: string;
  publicTests: PublicTestInfo[];
  hiddenTestCount: number;
  runtimes: RuntimeOption[];
};

export type SubmissionResponse = {
  id: string;
  status: string;
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

export type RunnerOpsStatus = {
  dockerAvailable: boolean;
  dockerEnabled: boolean;
  mavenCacheWarm: boolean;
  lspScriptsAvailable: boolean;
  lspWarmStampPresent: boolean;
  repoRoot: string;
  mavenCacheVolume: string;
  runnerImages: RunnerImageStatus[];
  lspImages: RunnerImageStatus[];
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
