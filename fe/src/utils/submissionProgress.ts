import type { TestResultEvent } from "../api/types";
import type { ChallengeDetail } from "../api/types";
import type { TrackedTest } from "../domain/runProgressTypes";
import { TestOutcomeStatus } from "../domain/constants";

export function buildInitialTrackedTests(challenge: ChallengeDetail): TrackedTest[] {
  const items: TrackedTest[] = challenge.publicTests.map((test) => ({
    name: test.name,
    status: "pending",
    message: test.description || undefined,
  }));
  if (challenge.hiddenTestCount > 0) {
    items.push({
      name: "hidden:pending",
      status: "pending",
    });
  }
  return items;
}

function mapTestStatus(status: string): TrackedTest["status"] {
  const normalized = status.toUpperCase();
  if (normalized === TestOutcomeStatus.PASS) {
    return "pass";
  }
  if (normalized === TestOutcomeStatus.FAIL) {
    return "fail";
  }
  if (normalized === TestOutcomeStatus.SKIP) {
    return "skip";
  }
  return "pending";
}

export function applyTestResult(
  tests: TrackedTest[],
  event: TestResultEvent,
): TrackedTest[] {
  const status = mapTestStatus(event.status);
  const publicIdx = tests.findIndex((t) => t.name === event.name);
  if (publicIdx >= 0) {
    const next = [...tests];
    next[publicIdx] = { name: event.name, status, message: event.message };
    return next;
  }

  let next = tests.filter((t) => t.name !== "hidden:pending");
  const existingIdx = next.findIndex((t) => t.name === event.name);
  if (existingIdx >= 0) {
    next[existingIdx] = { name: event.name, status, message: event.message };
  } else {
    next = [...next, { name: event.name, status, message: event.message }];
  }
  return next;
}
