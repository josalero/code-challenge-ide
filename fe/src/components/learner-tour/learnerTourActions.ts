import { clearLearnerTourProgress } from "./learnerTourStorage";

export function restartLearnerTour(userId: string): void {
  clearLearnerTourProgress(userId);
  window.dispatchEvent(new CustomEvent("ctl-learner-tour-restart"));
}
