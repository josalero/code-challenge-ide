export type AuthResponse = {
  accessToken: string;
  userId: string;
  email: string;
};

export type MeResponse = {
  id: string;
  email: string;
};

export type ProgressEntry = {
  challengeSlug: string;
  state: string;
};

export type ChallengeSummary = {
  slug: string;
  title: string;
  difficulty: string;
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

export type ChallengeDetail = {
  slug: string;
  title: string;
  descriptionMd: string;
  starterCode: string;
  difficulty: string;
  gatingConfig: string;
  publicTestNames: string[];
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

export type CustomTestsResponse = {
  code: string;
};

export type TestResultEvent = {
  name: string;
  status: string;
  message?: string;
};
