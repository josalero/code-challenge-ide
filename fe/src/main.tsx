import "@ant-design/v5-patch-for-react-19";
import "./monacoSetup";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntApp } from "antd";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { TooltipProvider } from "@/components/ui/tooltip";
import { ThemeProvider } from "@/theme/ThemeProvider";
import App from "./App";
import { AuthProvider } from "./auth/AuthProvider";
import LearnerGuidedTour from "./components/learner-tour/LearnerGuidedTour";
import { LearnerTourReadyProvider } from "./components/learner-tour/LearnerTourReadyProvider";
import ScrollToTop from "./components/ScrollToTop";
import "./index.css";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, staleTime: 30_000 },
  },
});

if (typeof window !== "undefined" && "scrollRestoration" in window.history) {
  window.history.scrollRestoration = "manual";
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ThemeProvider>
      <QueryClientProvider client={queryClient}>
        <TooltipProvider>
          <AntApp>
            <BrowserRouter>
              <ScrollToTop />
              <AuthProvider>
                <LearnerTourReadyProvider>
                  <App />
                  <LearnerGuidedTour />
                </LearnerTourReadyProvider>
              </AuthProvider>
            </BrowserRouter>
          </AntApp>
        </TooltipProvider>
      </QueryClientProvider>
    </ThemeProvider>
  </StrictMode>,
);
