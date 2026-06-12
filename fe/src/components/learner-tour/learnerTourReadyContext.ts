import { createContext } from "react";

export type LearnerTourReadyContextValue = {
  catalogReady: boolean;
  workspaceReady: boolean;
  workspaceTimedSession: boolean;
  setCatalogReady: (ready: boolean) => void;
  setWorkspaceReady: (ready: boolean, options?: { hasTimedSession?: boolean }) => void;
};

export const LearnerTourReadyContext = createContext<LearnerTourReadyContextValue | null>(
  null,
);
