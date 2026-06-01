/** VS Code Default Dark+ — standard language syntax colors for all grammars. */
export const EDITOR_THEME = "Default Dark+";

/** Classic VS Code dark editor surface. */
export const EDITOR_COLORS = {
  background: "#1e1e1e",
  foreground: "#d4d4d4",
  gutter: "#1e1e1e",
  lineHighlight: "#2a2d2e",
  lineNumber: "#858585",
  lineNumberActive: "#c6c6c6",
} as const;

/** Minimal user settings — let the theme JSON drive token colors. */
export function buildEditorUserConfiguration(): string {
  return JSON.stringify({
    "workbench.colorTheme": EDITOR_THEME,
    "editor.semanticHighlighting.enabled": true,
    "editor.bracketPairColorization.enabled": true,
    "editor.fontSize": 14,
  });
}
