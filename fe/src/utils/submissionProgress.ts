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

/** Last segment of a runner/JUnit name (e.g. com.foo.Bar.testMethod → testMethod). */
export function normalizeRunnerTestName(name: string): string {
  const trimmed = name.trim();
  const dot = trimmed.lastIndexOf(".");
  return dot >= 0 ? trimmed.slice(dot + 1) : trimmed;
}

function trackedTestKey(name: string): string {
  if (name.startsWith("hidden:")) {
    return name;
  }
  return normalizeRunnerTestName(name);
}

export function testNamesMatch(trackedName: string, eventName: string): boolean {
  if (trackedName === eventName) {
    return true;
  }
  if (trackedName === "hidden:pending") {
    return false;
  }
  if (trackedName.startsWith("hidden:")) {
    return trackedName.slice(7) === normalizeRunnerTestName(eventName);
  }
  const eventKey = normalizeRunnerTestName(eventName);
  if (trackedName === eventKey) {
    return true;
  }
  return eventName.endsWith("." + trackedName);
}

function isHiddenRunnerName(eventName: string): boolean {
  return eventName.includes(".hidden.") || eventName.toLowerCase().includes("hidden");
}

function storageNameForEvent(trackedName: string, eventName: string): string {
  if (trackedName !== "hidden:pending" && !trackedName.startsWith("hidden:")) {
    return trackedName;
  }
  if (isHiddenRunnerName(eventName)) {
    return `hidden:${normalizeRunnerTestName(eventName)}`;
  }
  return normalizeRunnerTestName(eventName);
}

export function applyTestResult(
  tests: TrackedTest[],
  event: TestResultEvent,
): TrackedTest[] {
  const status = mapTestStatus(event.status);
  const publicIdx = tests.findIndex((t) => testNamesMatch(t.name, event.name));
  if (publicIdx >= 0) {
    const next = [...tests];
    next[publicIdx] = {
      name: next[publicIdx].name,
      status,
      message: event.message,
    };
    return next;
  }

  let next = tests.filter((t) => t.name !== "hidden:pending");
  const storedName = isHiddenRunnerName(event.name)
    ? `hidden:${normalizeRunnerTestName(event.name)}`
    : normalizeRunnerTestName(event.name);

  const existingIdx = next.findIndex(
    (t) => t.name === storedName || testNamesMatch(t.name, event.name),
  );
  if (existingIdx >= 0) {
    next[existingIdx] = {
      name: storageNameForEvent(next[existingIdx].name, event.name),
      status,
      message: event.message,
    };
  } else {
    next = [...next, { name: storedName, status, message: event.message }];
  }
  return next;
}

/** Resolve leftover spinners when the submission stream finishes. */
export function finalizeTrackedTestsOnComplete(tests: TrackedTest[]): TrackedTest[] {
  const resolvedByKey = new Map<string, TrackedTest>();
  for (const test of tests) {
    if (test.name === "hidden:pending" || test.status === "pending") {
      continue;
    }
    resolvedByKey.set(trackedTestKey(test.name), test);
  }

  const output: TrackedTest[] = [];
  const seen = new Set<string>();

  for (const test of tests) {
    if (test.name === "hidden:pending") {
      continue;
    }
    const key = trackedTestKey(test.name);
    if (seen.has(key)) {
      continue;
    }
    seen.add(key);

    const resolved = resolvedByKey.get(key);
    if (test.status === "pending" && resolved) {
      output.push({
        ...resolved,
        name: test.name.includes(".") ? resolved.name : test.name,
      });
      continue;
    }

    if (test.status === "pending") {
      output.push({
        ...test,
        status: "fail",
        message: test.message ?? "No result reported",
      });
      continue;
    }

    output.push(test);
  }

  return output;
}
