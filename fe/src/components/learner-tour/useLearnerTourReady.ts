import { useContext } from "react";
import { LearnerTourReadyContext } from "./learnerTourReadyContext";

export function useLearnerTourReady() {
  const context = useContext(LearnerTourReadyContext);
  if (!context) {
    throw new Error("useLearnerTourReady must be used within LearnerTourReadyProvider");
  }
  return context;
}

export function useOptionalLearnerTourReady() {
  return useContext(LearnerTourReadyContext);
}
