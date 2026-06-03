import { ConfigProvider } from "antd";
import { useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { antThemeForMode } from "./antTheme";
import { ThemeContext } from "./theme-context";
import {
  readStoredTheme,
  resolveInitialTheme,
  storeTheme,
  themeColorMeta,
  type ThemeMode,
} from "./themeStorage";

function applyThemeClass(mode: ThemeMode) {
  const root = document.documentElement;
  root.classList.remove("light", "dark");
  root.classList.add(mode);
  root.style.colorScheme = mode;
  const meta = document.querySelector('meta[name="theme-color"]');
  if (meta) {
    meta.setAttribute("content", themeColorMeta(mode));
  }
}

type Props = {
  children: ReactNode;
};

export function ThemeProvider({ children }: Props) {
  const [mode, setModeState] = useState<ThemeMode>(() => {
    if (typeof document !== "undefined") {
      const stored = readStoredTheme();
      if (stored) {
        return stored;
      }
      return document.documentElement.classList.contains("light") ? "light" : "dark";
    }
    return resolveInitialTheme();
  });

  useEffect(() => {
    applyThemeClass(mode);
    storeTheme(mode);
  }, [mode]);

  const setMode = useCallback((next: ThemeMode) => {
    setModeState(next);
  }, []);

  const toggleMode = useCallback(() => {
    setModeState((current) => (current === "dark" ? "light" : "dark"));
  }, []);

  const value = useMemo(
    () => ({ mode, setMode, toggleMode }),
    [mode, setMode, toggleMode],
  );

  const antConfig = useMemo(() => antThemeForMode(mode), [mode]);

  return (
    <ThemeContext.Provider value={value}>
      <ConfigProvider theme={antConfig}>{children}</ConfigProvider>
    </ThemeContext.Provider>
  );
}
