import Editor from "@monaco-editor/react";
import { FileCode2, FlaskConical } from "lucide-react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  customTestsModelUri,
  editorLanguageFor,
  solutionModelUri,
} from "@/lsp/lspLanguageConfig";
import { monacoEditorAfterMount, monacoEditorBeforeMount } from "@/monacoEditorServices";
import { EDITOR_THEME } from "@/monacoTheme";
import LspMonacoEditor from "../LspMonacoEditor";
import MonacoServicesGate from "../MonacoServicesGate";
import {
  customTestsTabLabel,
  usesLsp,
} from "@/utils/monacoLanguage";
import {
  attachMonacoClipboardGuard,
  attachMonacoLargeEditGuard,
  type IntegrityEventPayload,
} from "@/utils/monacoClipboardGuard";
import { useCallback, useEffect, useRef, type MutableRefObject } from "react";
import type { editor as MonacoEditor } from "monaco-editor";

type Props = {
  slug: string;
  language: string;
  solutionCode: string;
  customTestsCode: string;
  onSolutionChange: (code: string) => void;
  onCustomTestsChange: (code: string) => void;
  workspaceTab: "solution" | "custom";
  onWorkspaceTabChange: (tab: "solution" | "custom") => void;
  readOnly?: boolean;
  /** Read-only preview of starter skeleton before Start test */
  previewStarter?: boolean;
  monitorIntegrity?: boolean;
  onIntegrityEvent?: (payload: IntegrityEventPayload) => void;
};

export default function CodeEditorPanel({
  slug,
  language,
  solutionCode,
  customTestsCode,
  onSolutionChange,
  onCustomTestsChange,
  workspaceTab,
  onWorkspaceTabChange,
  readOnly = false,
  previewStarter = false,
  monitorIntegrity = false,
  onIntegrityEvent,
}: Props) {
  const plainEditorRef = useRef<MonacoEditor.IStandaloneCodeEditor | null>(null);
  const customEditorRef = useRef<MonacoEditor.IStandaloneCodeEditor | null>(null);
  const solutionGuardRef = useRef<{ dispose: () => void } | null>(null);
  const customGuardRef = useRef<{ dispose: () => void } | null>(null);
  const solutionLargeEditRef = useRef<{ dispose: () => void } | null>(null);
  const customLargeEditRef = useRef<{ dispose: () => void } | null>(null);

  const syncGuards = useCallback(
    (
      editor: MonacoEditor.IStandaloneCodeEditor | null,
      clipboardRef: MutableRefObject<{ dispose: () => void } | null>,
      largeEditRef: MutableRefObject<{ dispose: () => void } | null>,
      editorSurface: "SOLUTION" | "CUSTOM_TESTS",
    ) => {
      clipboardRef.current?.dispose();
      clipboardRef.current = null;
      largeEditRef.current?.dispose();
      largeEditRef.current = null;
      if (!editor || !onIntegrityEvent) {
        return;
      }
      clipboardRef.current = attachMonacoClipboardGuard(editor, {
        enabled: monitorIntegrity,
        editorSurface,
        onViolation: onIntegrityEvent,
      });
      largeEditRef.current = attachMonacoLargeEditGuard(editor, {
        enabled: monitorIntegrity,
        editorSurface,
        onLargeEdit: onIntegrityEvent,
      });
    },
    [onIntegrityEvent, monitorIntegrity],
  );

  useEffect(() => {
    syncGuards(plainEditorRef.current, solutionGuardRef, solutionLargeEditRef, "SOLUTION");
    syncGuards(customEditorRef.current, customGuardRef, customLargeEditRef, "CUSTOM_TESTS");
    const solutionClipboard = solutionGuardRef;
    const customClipboard = customGuardRef;
    const solutionLarge = solutionLargeEditRef;
    const customLarge = customLargeEditRef;
    return () => {
      solutionClipboard.current?.dispose();
      customClipboard.current?.dispose();
      solutionLarge.current?.dispose();
      customLarge.current?.dispose();
    };
  }, [monitorIntegrity, onIntegrityEvent, syncGuards]);

  const editorLanguage = editorLanguageFor(language);
  const lspEnabled = usesLsp(language) && !previewStarter;
  const solutionPath = solutionModelUri(language);
  const customTestsPath = customTestsModelUri(language);

  const editorOptions = {
    minimap: { enabled: false },
    fontSize: 14,
    lineHeight: 22,
    fontFamily: "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace",
    padding: { top: 8, bottom: 8 },
    automaticLayout: true,
    readOnly,
    scrollBeyondLastLine: false,
    renderLineHighlight: "line" as const,
    contextmenu: true,
  };

  return (
    <section
      className="flex h-full min-h-0 w-full flex-1 flex-col bg-[#1e1e1e]"
      aria-label="Code editor"
    >
      <Tabs
        value={workspaceTab}
        onValueChange={(v) => onWorkspaceTabChange(v as "solution" | "custom")}
        className="flex h-full min-h-0 w-full flex-col gap-0"
      >
        <div className="flex shrink-0 items-center justify-between border-b border-slate-800/90 bg-[#161b22] px-2 py-0.5">
          <TabsList className="h-8 gap-0.5 bg-transparent p-0">
            <TabsTrigger
              value="solution"
              className="gap-1.5 rounded-md border border-transparent px-3 text-xs data-[state=active]:border-slate-600/60 data-[state=active]:bg-slate-800 data-[state=active]:text-slate-100"
            >
              <FileCode2 className="size-3.5 opacity-70" aria-hidden />
              Solution
            </TabsTrigger>
            <TabsTrigger
              value="custom"
              disabled={previewStarter}
              className="gap-1.5 rounded-md border border-transparent px-3 text-xs data-[state=active]:border-slate-600/60 data-[state=active]:bg-slate-800 data-[state=active]:text-slate-100 disabled:opacity-40"
            >
              <FlaskConical className="size-3.5 opacity-70" aria-hidden />
              {customTestsTabLabel(language)}
            </TabsTrigger>
          </TabsList>
          {readOnly && (
            <span className="pr-2 text-[10px] uppercase tracking-wide text-slate-500">
              {previewStarter ? "Starter skeleton" : "Read-only"}
            </span>
          )}
        </div>

        <TabsContent
          value="solution"
          className="mt-0 flex h-full min-h-0 flex-1 flex-col overflow-hidden data-[state=inactive]:hidden"
        >
          <div className="ctl-editor-frame h-full min-h-0 w-full">
            <MonacoServicesGate>
              {lspEnabled ? (
                <LspMonacoEditor
                  key={`${slug}-${language}`}
                  language={language}
                  value={solutionCode}
                  onChange={onSolutionChange}
                  lspEnabled={lspEnabled}
                  readOnly={readOnly}
                  monitorIntegrity={monitorIntegrity}
                  onIntegrityEvent={onIntegrityEvent}
                />
              ) : (
                <Editor
                  key={`${slug}-${editorLanguage}`}
                  path={solutionPath}
                  height="100%"
                  language={editorLanguage}
                  theme={EDITOR_THEME}
                  beforeMount={monacoEditorBeforeMount}
                  onMount={(editor, m) => {
                    plainEditorRef.current = editor;
                    monacoEditorAfterMount(editor, m);
                    syncGuards(editor, solutionGuardRef, solutionLargeEditRef, "SOLUTION");
                  }}
                  value={solutionCode}
                  onChange={(v) => onSolutionChange(v ?? "")}
                  options={{
                    ...editorOptions,
                    ariaLabel: "Solution editor",
                  }}
                />
              )}
            </MonacoServicesGate>
          </div>
        </TabsContent>

        <TabsContent
          value="custom"
          className="mt-0 flex h-full min-h-0 flex-1 flex-col overflow-hidden data-[state=inactive]:hidden"
        >
          <div className="ctl-editor-frame h-full min-h-0 w-full">
            <MonacoServicesGate>
              <Editor
                path={customTestsPath}
                height="100%"
                language={editorLanguage}
                theme={EDITOR_THEME}
                beforeMount={monacoEditorBeforeMount}
                onMount={(editor, m) => {
                  customEditorRef.current = editor;
                  monacoEditorAfterMount(editor, m);
                  syncGuards(editor, customGuardRef, customLargeEditRef, "CUSTOM_TESTS");
                }}
                value={customTestsCode}
                onChange={(v) => onCustomTestsChange(v ?? "")}
                options={{
                  ...editorOptions,
                  ariaLabel: "Custom tests editor",
                }}
              />
            </MonacoServicesGate>
          </div>
        </TabsContent>
      </Tabs>
    </section>
  );
}
