import { theme as antTheme } from "antd";
import type { ThemeConfig } from "antd";
import type { ThemeMode } from "./themeStorage";

const shared: ThemeConfig = {
  token: {
    colorPrimary: "#10b981",
    borderRadius: 8,
    fontFamily:
      'ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, sans-serif',
    controlHeight: 36,
  },
  components: {
    Card: { paddingLG: 20 },
    Button: { primaryShadow: "none" },
  },
};

const dark: ThemeConfig = {
  algorithm: antTheme.darkAlgorithm,
  token: {
    ...shared.token,
    colorBgBase: "#0f172a",
    colorBgContainer: "#1e293b",
    colorBgElevated: "#273449",
    colorBorder: "#475569",
    colorText: "#f1f5f9",
    colorTextSecondary: "#94a3b8",
  },
  components: {
    ...shared.components,
    Table: { headerBg: "#1e293b", rowHoverBg: "rgba(51, 65, 85, 0.45)" },
  },
};

const light: ThemeConfig = {
  algorithm: antTheme.defaultAlgorithm,
  token: {
    ...shared.token,
    colorBgBase: "#f8fafc",
    colorBgContainer: "#ffffff",
    colorBgElevated: "#ffffff",
    colorBorder: "#e2e8f0",
    colorText: "#0f172a",
    colorTextSecondary: "#64748b",
  },
  components: {
    ...shared.components,
    Table: { headerBg: "#f1f5f9", rowHoverBg: "rgba(241, 245, 249, 0.9)" },
  },
};

export function antThemeForMode(mode: ThemeMode): ThemeConfig {
  return mode === "light" ? light : dark;
}
