import type { TourProps } from "antd";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";
import CtlGuidedTour from "../guided-tour/CtlGuidedTour";
import { learnerCatalogTourSteps, learnerWorkspaceTourSteps } from "./learnerTourSteps";
import {
  clearLearnerTourProgress,
  isLearnerTourCompleted,
  markLearnerTourCompleted,
  readLearnerTourProgress,
  writeLearnerTourProgress,
  type LearnerTourPhase,
} from "./learnerTourStorage";
import { useOptionalLearnerTourReady } from "./useLearnerTourReady";

const CATALOG_PATH = "/challenges";
const WORKSPACE_PATH = /^\/challenges\/[^/]+$/;

function isWorkspacePath(pathname: string): boolean {
  return WORKSPACE_PATH.test(pathname) && pathname !== "/challenges/new";
}

export default function LearnerGuidedTour() {
  const ready = useOptionalLearnerTourReady();
  const catalogReady = ready?.catalogReady ?? false;
  const workspaceReady = ready?.workspaceReady ?? false;
  const workspaceTimedSession = ready?.workspaceTimedSession ?? false;

  const { user, isAdmin } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [phase, setPhase] = useState<LearnerTourPhase>("catalog");
  const [current, setCurrent] = useState(0);

  const userId = user?.id;
  const isLearner = Boolean(user && !isAdmin);
  const blocked = Boolean(user?.mustChangePassword);

  const steps = useMemo<TourProps["steps"]>(
    () =>
      phase === "catalog"
        ? learnerCatalogTourSteps()
        : learnerWorkspaceTourSteps({ hasTimedSession: workspaceTimedSession }),
    [phase, workspaceTimedSession],
  );

  const finishTour = useCallback(() => {
    if (userId) {
      markLearnerTourCompleted(userId);
    }
    setOpen(false);
  }, [userId]);

  const openTour = useCallback((nextPhase: LearnerTourPhase, nextStep: number) => {
    setPhase(nextPhase);
    setCurrent(nextStep);
    window.setTimeout(() => setOpen(true), 150);
  }, []);

  useEffect(() => {
    if (!userId || blocked || !isLearner) {
      setOpen(false);
      return;
    }

    const params = new URLSearchParams(location.search);
    const forceTour = params.get("tour") === "1";

    if (forceTour) {
      clearLearnerTourProgress(userId);

      if (isWorkspacePath(location.pathname)) {
        if (workspaceReady) {
          params.delete("tour");
          navigate(
            { pathname: location.pathname, search: params.toString() ? `?${params}` : "" },
            { replace: true },
          );
          openTour("workspace", 0);
        }
        return;
      }

      if (location.pathname !== CATALOG_PATH) {
        navigate(`${CATALOG_PATH}?tour=1`, { replace: true });
        return;
      }

      if (catalogReady) {
        params.delete("tour");
        navigate(
          { pathname: location.pathname, search: params.toString() ? `?${params}` : "" },
          { replace: true },
        );
        openTour("catalog", 0);
      }
      return;
    }

    if (isLearnerTourCompleted(userId)) {
      return;
    }

    const saved = readLearnerTourProgress(userId);
    const savedPhase = saved?.phase ?? "catalog";
    const savedStep = saved?.step ?? 0;

    if (savedPhase === "workspace") {
      if (isWorkspacePath(location.pathname) && workspaceReady) {
        openTour("workspace", savedStep);
      }
      return;
    }

    if (location.pathname === CATALOG_PATH && catalogReady) {
      openTour("catalog", savedStep);
    }
  }, [
    blocked,
    catalogReady,
    isLearner,
    location.pathname,
    location.search,
    navigate,
    openTour,
    userId,
    workspaceReady,
  ]);

  useEffect(() => {
    const onRestart = () => {
      if (!userId) {
        return;
      }
      clearLearnerTourProgress(userId);
      if (isWorkspacePath(location.pathname) && workspaceReady) {
        openTour("workspace", 0);
        return;
      }
      if (location.pathname === CATALOG_PATH && catalogReady) {
        openTour("catalog", 0);
        return;
      }
      navigate(`${CATALOG_PATH}?tour=1`);
    };
    window.addEventListener("ctl-learner-tour-restart", onRestart);
    return () => window.removeEventListener("ctl-learner-tour-restart", onRestart);
  }, [catalogReady, location.pathname, navigate, openTour, userId, workspaceReady]);

  const handleChange = (next: number) => {
    setCurrent(next);
    if (userId) {
      writeLearnerTourProgress(userId, { completed: false, phase, step: next });
    }
  };

  const handleClose = () => {
    finishTour();
  };

  const handleFinish = () => {
    if (phase === "catalog") {
      if (userId) {
        writeLearnerTourProgress(userId, { completed: false, phase: "workspace", step: 0 });
      }
      setOpen(false);
      return;
    }
    finishTour();
  };

  if (!isLearner || blocked) {
    return null;
  }

  return (
    <CtlGuidedTour
      open={open}
      current={current}
      steps={steps}
      onChange={handleChange}
      onClose={handleClose}
      onFinish={handleFinish}
    />
  );
}
