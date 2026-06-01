/** Shared types for live submission progress (workspace tests tab). */
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
