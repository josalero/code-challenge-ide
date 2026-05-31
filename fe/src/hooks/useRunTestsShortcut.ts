import { useEffect } from "react";

/** Cmd/Ctrl + Enter triggers run when enabled. */
export function useRunTestsShortcut(enabled: boolean, onRun: () => void) {
  useEffect(() => {
    if (!enabled) {
      return;
    }
    const handler = (event: KeyboardEvent) => {
      const isMac = navigator.platform.toLowerCase().includes("mac");
      const mod = isMac ? event.metaKey : event.ctrlKey;
      if (mod && event.key === "Enter") {
        event.preventDefault();
        onRun();
      }
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [enabled, onRun]);
}
