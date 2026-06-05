import Editor from "@monaco-editor/react";
import type { editor as MonacoEditor } from "monaco-editor";
import { useEffect, useMemo, useRef } from "react";
import { cn } from "../../lib/utils";
import { monaco } from "../../monacoSetup";
import { monacoEditorAfterMount, monacoEditorBeforeMount } from "../../monacoEditorServices";
import { EDITOR_THEME } from "../../monacoTheme";
import { useTheme } from "../../theme/useTheme";
import { monacoLanguageFor } from "../../utils/monacoLanguage";
import MonacoServicesGate from "../MonacoServicesGate";

export type SyntaxSummary = {
  errors: number;
  warnings: number;
};

type Props = {
  ariaLabel: string;
  language: string;
  modelId: string;
  value?: string;
  onChange?: (value: string) => void;
  onSyntaxChange?: (modelId: string, summary: SyntaxSummary) => void;
  height?: number;
  className?: string;
};

const MARKER_ERROR = 8;
const MARKER_WARNING = 4;
const LIGHT_EDITOR_THEME = "vs";

function extensionFor(language: string): string {
  switch (language.toLowerCase()) {
    case "java":
      return "java";
    case "python":
      return "py";
    case "go":
      return "go";
    case "node":
      return "js";
    case "csharp":
      return "cs";
    case "typescript":
    case "angular":
      return "ts";
    case "react":
      return "tsx";
    case "vue":
      return "vue";
    case "rust":
      return "rs";
    case "cpp":
      return "cpp";
    case "sql":
      return "sql";
    default:
      return "txt";
  }
}

function modelPath(language: string, modelId: string): string {
  const safeId = modelId.replace(/[^a-zA-Z0-9_.-]/g, "-");
  return `file:///challenge-create/${language || "plain"}/${safeId}.${extensionFor(language)}`;
}

export default function ChallengeCodeEditor({
  ariaLabel,
  language,
  modelId,
  value = "",
  onChange,
  onSyntaxChange,
  height = 280,
  className,
}: Props) {
  const { mode } = useTheme();
  const editorLanguage = monacoLanguageFor(language);
  const editorTheme = mode === "light" ? LIGHT_EDITOR_THEME : EDITOR_THEME;
  const monacoRef = useRef<typeof monaco | null>(null);
  const path = useMemo(() => modelPath(language, modelId), [language, modelId]);

  useEffect(() => {
    monacoRef.current?.editor.setTheme(editorTheme);
  }, [editorTheme]);

  return (
    <div
      className={cn(
        "overflow-hidden rounded-md border border-border bg-card shadow-sm dark:bg-[#1e1e1e]",
        className,
      )}
      style={{ height }}
    >
      <MonacoServicesGate label="Loading editor...">
        <Editor
          path={path}
          height="100%"
          language={editorLanguage}
          theme={editorTheme}
          beforeMount={monacoEditorBeforeMount}
          onMount={(editor, editorMonaco) => {
            monacoRef.current = editorMonaco;
            monacoEditorAfterMount(editor, editorMonaco);
            editorMonaco.editor.setTheme(editorTheme);
          }}
          onValidate={(markers: MonacoEditor.IMarker[]) => {
            onSyntaxChange?.(modelId, {
              errors: markers.filter((marker) => marker.severity === MARKER_ERROR).length,
              warnings: markers.filter((marker) => marker.severity === MARKER_WARNING).length,
            });
          }}
          value={value}
          onChange={(nextValue) => onChange?.(nextValue ?? "")}
          options={{
            minimap: { enabled: false },
            fontFamily: "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace",
            fontSize: 13,
            lineHeight: 21,
            padding: { top: 10, bottom: 10 },
            automaticLayout: true,
            scrollBeyondLastLine: false,
            renderLineHighlight: "line",
            wordWrap: "on",
            contextmenu: true,
            ariaLabel,
          }}
        />
      </MonacoServicesGate>
    </div>
  );
}
