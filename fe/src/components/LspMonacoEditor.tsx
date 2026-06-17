import { Loader2 } from "lucide-react";
import Editor from "@monaco-editor/react";
import { useEffect, useRef, useState } from "react";
import { getAccessToken } from "../auth/authStorage";
import { monaco } from "../monacoSetup";
import { attachMonacoClipboardGuard, attachMonacoLargeEditGuard, type IntegrityEventPayload } from "../utils/monacoClipboardGuard";
import { attachMonacoLanguageClient } from "../lsp/createMonacoLanguageClient";
import { encodeSolutionForLsp } from "../lsp/encodeSolution";
import { monacoEditorAfterMount, monacoEditorBeforeMount, refreshEditorLanguageModel } from "../monacoEditorServices";
import { EDITOR_THEME } from "../monacoTheme";
import {
  editorLanguageFor,
  lspConfigFor,
  solutionModelUri,
} from "../lsp/lspLanguageConfig";
import type { MonacoLanguageClient } from "monaco-languageclient";

type LspStatus =
  | "off"
  | "connecting"
  | "ready"
  | "unavailable";

type Props = {
  language: string;
  value: string;
  onChange: (value: string) => void;
  lspEnabled: boolean;
  readOnly?: boolean;
  monitorIntegrity?: boolean;
  onIntegrityEvent?: (payload: IntegrityEventPayload) => void;
};

const EDITOR_OPTIONS: monaco.editor.IStandaloneEditorConstructionOptions = {
  minimap: { enabled: false },
  fontSize: 14,
  lineHeight: 22,
  fontFamily: "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace",
  padding: { top: 8, bottom: 8 },
  automaticLayout: true,
  scrollBeyondLastLine: false,
  renderLineHighlight: "line",
  quickSuggestions: { other: true, comments: false, strings: false },
  suggestOnTriggerCharacters: true,
  tabCompletion: "on",
  wordBasedSuggestions: "off",
};

const LSP_CONNECT_TIMEOUT_MS = 90_000;

function formatStartError(error: unknown): string {
  if (import.meta.env.DEV && error instanceof Error && error.message) {
    return `Language client failed: ${error.message}`;
  }
  return "Language client failed to start.";
}

export default function LspMonacoEditor({
  language,
  value,
  onChange,
  lspEnabled,
  readOnly = false,
  monitorIntegrity = false,
  onIntegrityEvent,
}: Props) {
  const lspConfig = lspConfigFor(language);
  const modelUri = solutionModelUri(language);
  const monacoLanguage = editorLanguageFor(language);

  const clientRef = useRef<MonacoLanguageClient | null>(null);
  const socketRef = useRef<WebSocket | null>(null);
  const editorRef = useRef<monaco.editor.IStandaloneCodeEditor | null>(null);
  const modelRef = useRef<monaco.editor.ITextModel | null>(null);
  const latestCodeRef = useRef(value);
  const connectGenRef = useRef(0);
  const lspReadyRef = useRef(false);
  const clipboardGuardRef = useRef<{ dispose: () => void } | null>(null);
  const largeEditGuardRef = useRef<{ dispose: () => void } | null>(null);
  const [editorMounted, setEditorMounted] = useState(false);
  const [lspStatus, setLspStatus] = useState<LspStatus>("off");
  const [lspMessage, setLspMessage] = useState<string | null>(null);

  const handleMount = (
    editor: monaco.editor.IStandaloneCodeEditor,
    editorMonaco: typeof monaco,
  ) => {
    editorRef.current = editor;
    const uri = editorMonaco.Uri.parse(modelUri);
    let model = editorMonaco.editor.getModel(uri);
    if (!model) {
      model = editorMonaco.editor.createModel(value, monacoLanguage, uri);
    }
    refreshEditorLanguageModel(model, monacoLanguage);
    modelRef.current = model;
    if (editor.getModel()?.uri.toString() !== model.uri.toString()) {
      editor.setModel(model);
    }
    editor.updateOptions({ readOnly });
    monacoEditorAfterMount(editor, editorMonaco);
    clipboardGuardRef.current?.dispose();
    largeEditGuardRef.current?.dispose();
    if (onIntegrityEvent) {
      clipboardGuardRef.current = attachMonacoClipboardGuard(editor, {
        enabled: monitorIntegrity,
        editorSurface: "SOLUTION",
        onViolation: onIntegrityEvent,
      });
      largeEditGuardRef.current = attachMonacoLargeEditGuard(editor, {
        enabled: monitorIntegrity,
        editorSurface: "SOLUTION",
        onLargeEdit: onIntegrityEvent,
      });
    }
    setEditorMounted(true);
  };

  useEffect(() => {
    const editor = editorRef.current;
    if (!editor || !onIntegrityEvent) {
      return;
    }
    clipboardGuardRef.current?.dispose();
    largeEditGuardRef.current?.dispose();
    clipboardGuardRef.current = attachMonacoClipboardGuard(editor, {
      enabled: monitorIntegrity,
      editorSurface: "SOLUTION",
      onViolation: onIntegrityEvent,
    });
    largeEditGuardRef.current = attachMonacoLargeEditGuard(editor, {
      enabled: monitorIntegrity,
      editorSurface: "SOLUTION",
      onLargeEdit: onIntegrityEvent,
    });
    return () => {
      clipboardGuardRef.current?.dispose();
      largeEditGuardRef.current?.dispose();
    };
  }, [monitorIntegrity, onIntegrityEvent]);

  useEffect(() => {
    editorRef.current?.updateOptions({ readOnly });
  }, [readOnly]);

  useEffect(() => {
    latestCodeRef.current = value;
    const model = modelRef.current;
    if (!model || model.getValue() === value) {
      return;
    }
    model.setValue(value);
  }, [value]);

  useEffect(() => {
    if (!lspEnabled || !editorMounted || !lspConfig) {
      setLspStatus("off");
      setLspMessage(null);
      return;
    }

    const token = getAccessToken();
    if (!token) {
      setLspStatus("unavailable");
      setLspMessage(`Sign in to enable ${lspConfig.displayName} IntelliSense.`);
      return;
    }

    const generation = ++connectGenRef.current;
    let cancelled = false;
    let connectTimeoutId: ReturnType<typeof setTimeout> | undefined;

    const failConnect = (message: string) => {
      if (cancelled || generation !== connectGenRef.current || lspReadyRef.current) {
        return;
      }
      setLspStatus("unavailable");
      setLspMessage(message);
      socketRef.current?.close();
    };

    const connect = () => {
      setLspStatus("connecting");
      setLspMessage(null);
      lspReadyRef.current = false;

      clientRef.current?.stop();
      clientRef.current = null;
      socketRef.current?.close();

      const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
      const params = new URLSearchParams({
        access_token: token,
        solution: encodeSolutionForLsp(latestCodeRef.current),
      });
      const url = `${protocol}//${window.location.host}${lspConfig.lspPath}?${params.toString()}`;
      const socket = new WebSocket(url);
      socketRef.current = socket;

      connectTimeoutId = setTimeout(() => {
        failConnect(
          `${lspConfig.displayName} language server timed out. Rebuild LSP images: ./scripts/build-lsp-images.sh`,
        );
      }, LSP_CONNECT_TIMEOUT_MS);

      socket.onopen = () => {
        if (cancelled || generation !== connectGenRef.current) {
          socket.close();
          return;
        }
        void (async () => {
          try {
            const code =
              modelRef.current?.getValue()
              ?? editorRef.current?.getModel()?.getValue()
              ?? value;
            const uri = monaco.Uri.parse(modelUri);
            let model = modelRef.current ?? monaco.editor.getModel(uri);
            if (!model) {
              model = monaco.editor.createModel(code, monacoLanguage, uri);
            } else if (model.getValue() !== code) {
              model.setValue(code);
            }
            modelRef.current = model;
            editorRef.current?.setModel(model);
            clientRef.current = await attachMonacoLanguageClient(socket, lspConfig);
            if (!cancelled && generation === connectGenRef.current) {
              clearTimeout(connectTimeoutId);
              lspReadyRef.current = true;
              setLspStatus("ready");
              setLspMessage(null);
            }
          } catch (error) {
            clearTimeout(connectTimeoutId);
            if (!cancelled && generation === connectGenRef.current) {
              setLspStatus("unavailable");
              setLspMessage(formatStartError(error));
              if (import.meta.env.DEV) {
                console.error(`${lspConfig.displayName} LSP client start failed`, error);
              }
            }
          }
        })();
      };

      socket.onerror = () => {
        clearTimeout(connectTimeoutId);
        if (!cancelled && generation === connectGenRef.current && !lspReadyRef.current) {
          setLspStatus("unavailable");
          setLspMessage(
            `${lspConfig.displayName} language server unavailable. Run: ./scripts/build-lsp-images.sh and restart the API.`,
          );
        }
      };

      socket.onclose = (event) => {
        clearTimeout(connectTimeoutId);
        const wasReady = lspReadyRef.current;
        lspReadyRef.current = false;
        clientRef.current?.stop();
        clientRef.current = null;

        if (cancelled || generation !== connectGenRef.current) {
          return;
        }

        // Normal close from effect cleanup (e.g. React StrictMode remount in dev).
        if (event.code === 1000 && !wasReady) {
          return;
        }

        setLspStatus("unavailable");
        setLspMessage(
          wasReady
            ? "IntelliSense disconnected. Reconnecting…"
            : (event.reason || "Language server connection closed. Basic editing still works."),
        );

        if (wasReady || event.code !== 1000) {
          window.setTimeout(() => {
            if (!cancelled && generation === connectGenRef.current) {
              connect();
            }
          }, 2_000);
        }
      };
    };

    connect();

    return () => {
      cancelled = true;
      clearTimeout(connectTimeoutId);
      lspReadyRef.current = false;
      clientRef.current?.stop();
      clientRef.current = null;
      socketRef.current?.close();
      socketRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- connect once per mount/language, keep latest code via ref
  }, [lspEnabled, editorMounted, language]);

  const showStatus = lspEnabled && lspStatus !== "off" && lspStatus !== "ready";

  return (
    <div className="flex h-full min-h-0 w-full flex-col">
      {showStatus && (
        <div
          className={`flex shrink-0 items-center gap-2 border-b px-3 py-1.5 text-xs ${
            lspStatus === "unavailable"
              ? "border-amber-500/20 bg-amber-500/10 text-amber-100"
              : "border-sky-500/20 bg-sky-500/10 text-sky-200"
          }`}
          role="status"
        >
          {lspStatus === "connecting" && (
            <Loader2 className="size-3 shrink-0 animate-spin" aria-hidden />
          )}
          <span className="truncate">
            {lspStatus === "connecting"
              ? `Connecting ${lspConfig?.displayName ?? "language"} language server…`
              : (lspMessage ?? "IntelliSense unavailable")}
          </span>
        </div>
      )}
      <div className="min-h-0 flex-1">
        <Editor
          path={modelUri}
          height="100%"
          language={monacoLanguage}
          theme={EDITOR_THEME}
          beforeMount={monacoEditorBeforeMount}
          value={value}
          onChange={(v) => onChange(v ?? "")}
          onMount={handleMount}
          options={{
            ...EDITOR_OPTIONS,
            readOnly,
            contextmenu: true,
            ariaLabel: "Solution editor",
          }}
        />
      </div>
    </div>
  );
}
