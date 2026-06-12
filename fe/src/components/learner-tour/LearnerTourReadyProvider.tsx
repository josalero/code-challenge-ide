import { useCallback, useMemo, useState, type ReactNode } from "react";
import { LearnerTourReadyContext } from "./learnerTourReadyContext";

export function LearnerTourReadyProvider({ children }: { children: ReactNode }) {
  const [catalogReady, setCatalogReadyState] = useState(false);
  const [workspaceReady, setWorkspaceReadyState] = useState(false);
  const [workspaceTimedSession, setWorkspaceTimedSession] = useState(false);

  const setCatalogReady = useCallback((ready: boolean) => {
    setCatalogReadyState(ready);
  }, []);

  const setWorkspaceReady = useCallback(
    (ready: boolean, options?: { hasTimedSession?: boolean }) => {
      setWorkspaceReadyState(ready);
      if (options?.hasTimedSession != null) {
        setWorkspaceTimedSession(options.hasTimedSession);
      }
    },
    [],
  );

  const value = useMemo(
    () => ({
      catalogReady,
      workspaceReady,
      workspaceTimedSession,
      setCatalogReady,
      setWorkspaceReady,
    }),
    [catalogReady, workspaceReady, workspaceTimedSession, setCatalogReady, setWorkspaceReady],
  );

  return (
    <LearnerTourReadyContext.Provider value={value}>{children}</LearnerTourReadyContext.Provider>
  );
}
