const STORAGE_PREFIX = "ctl.learner-tour.v1";

export type LearnerTourPhase = "catalog" | "workspace";

export type LearnerTourProgress = {
  completed: boolean;
  phase: LearnerTourPhase;
  step: number;
};

function storageKey(userId: string): string {
  return `${STORAGE_PREFIX}.${userId}`;
}

export function readLearnerTourProgress(userId: string): LearnerTourProgress | null {
  try {
    const raw = localStorage.getItem(storageKey(userId));
    if (!raw) {
      return null;
    }
    const parsed = JSON.parse(raw) as LearnerTourProgress;
    if (
      typeof parsed.completed !== "boolean"
      || typeof parsed.step !== "number"
      || (parsed.phase !== "catalog" && parsed.phase !== "workspace")
    ) {
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

export function writeLearnerTourProgress(userId: string, progress: LearnerTourProgress): void {
  localStorage.setItem(storageKey(userId), JSON.stringify(progress));
}

export function clearLearnerTourProgress(userId: string): void {
  localStorage.removeItem(storageKey(userId));
}

export function isLearnerTourCompleted(userId: string): boolean {
  return readLearnerTourProgress(userId)?.completed === true;
}

export function markLearnerTourCompleted(userId: string): void {
  writeLearnerTourProgress(userId, { completed: true, phase: "workspace", step: 0 });
}
