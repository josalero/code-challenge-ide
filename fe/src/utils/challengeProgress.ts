import { ProgressState } from "../domain/constants";

export type ProgressFilter = "all" | "active" | "passed" | "not_started";

export function matchesProgressFilter(
  state: string | undefined,
  filter: ProgressFilter,
): boolean {
  if (filter === "all") {
    return true;
  }
  if (filter === "passed") {
    return state === ProgressState.PASSED;
  }
  if (filter === "not_started") {
    return !state || state === ProgressState.NOT_STARTED;
  }
  if (filter === "active") {
    return (
      state === ProgressState.ATTEMPTED
      || state === ProgressState.FAILED
    );
  }
  return true;
}
