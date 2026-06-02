/** Shared types for live submission progress (workspace activity log). */
export type ActivityKind =
  | "info"
  | "success"
  | "warning"
  | "error"
  | "test-pass"
  | "test-fail"
  | "test-skip";

export type ActivityEntry = {
  id: string;
  at: number;
  message: string;
  kind?: ActivityKind;
  /** Full runner/JUnit name when `message` is shortened (tests). */
  title?: string;
};

export type TrackedTest = {
  name: string;
  status: "pending" | "pass" | "fail" | "skip";
  message?: string;
};
