import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntApp, ConfigProvider, theme } from "antd";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { AuthProvider } from "./auth/AuthProvider";
import "./index.css";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, staleTime: 30_000 },
  },
});

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ConfigProvider
      theme={{
        algorithm: theme.darkAlgorithm,
        token: {
          colorPrimary: "#34d399",
          colorBgBase: "#020617",
          colorBgContainer: "#0f172a",
          colorBgElevated: "#111827",
          colorBorder: "#334155",
          colorText: "#f1f5f9",
          colorTextSecondary: "#94a3b8",
          borderRadius: 8,
          fontFamily:
            'ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
          controlHeight: 36,
        },
        components: {
          Card: { paddingLG: 20 },
          Button: { primaryShadow: "none" },
          Table: { headerBg: "#0f172a", rowHoverBg: "rgba(30, 41, 59, 0.5)" },
        },
      }}
    >
      <QueryClientProvider client={queryClient}>
        <AntApp>
          <BrowserRouter>
            <AuthProvider>
              <App />
            </AuthProvider>
          </BrowserRouter>
        </AntApp>
      </QueryClientProvider>
    </ConfigProvider>
  </StrictMode>,
);
