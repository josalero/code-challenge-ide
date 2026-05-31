import Editor, { loader } from "@monaco-editor/react";
import * as monaco from "monaco-editor";
import { Alert } from "antd";
import { useEffect, useRef, useState } from "react";
import { getAccessToken } from "../auth/authStorage";
import { ApiPaths } from "../domain/constants";
import { attachJavaLanguageClient, solutionModelUri } from "../lsp/createJavaLanguageClient";
import { encodeSolutionForLsp } from "../lsp/encodeSolution";
import type { MonacoLanguageClient } from "monaco-languageclient";

loader.config({ monaco });

type LspStatus = "off" | "connecting" | "ready" | "unavailable";

type Props = {
  value: string;
  onChange: (value: string) => void;
  lspEnabled: boolean;
  height?: string;
};

export default function JavaLspEditor({
  value,
  onChange,
  lspEnabled,
  height = "100%",
}: Props) {
  const clientRef = useRef<MonacoLanguageClient | null>(null);
  const socketRef = useRef<WebSocket | null>(null);
  const editorRef = useRef<monaco.editor.IStandaloneCodeEditor | null>(null);
  const [debouncedSolution, setDebouncedSolution] = useState(value);
  const [lspStatus, setLspStatus] = useState<LspStatus>(lspEnabled ? "connecting" : "off");
  const [lspMessage, setLspMessage] = useState<string | null>(null);
  const lspReadyRef = useRef(false);

  useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedSolution(value), 1200);
    return () => window.clearTimeout(timer);
  }, [value]);

  useEffect(() => {
    if (!lspEnabled) {
      setLspStatus("off");
      return;
    }
    if (!editorRef.current) {
      return;
    }
    const token = getAccessToken();
    if (!token) {
      setLspStatus("unavailable");
      setLspMessage("Sign in to enable Java IntelliSense.");
      return;
    }

    setLspStatus("connecting");
    setLspMessage(null);
    lspReadyRef.current = false;

    clientRef.current?.stop();
    clientRef.current = null;
    socketRef.current?.close();

    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const params = new URLSearchParams({
      access_token: token,
      solution: encodeSolutionForLsp(debouncedSolution),
    });
    const url = `${protocol}//${window.location.host}${ApiPaths.LSP_JAVA}?${params.toString()}`;
    const socket = new WebSocket(url);
    socketRef.current = socket;

    socket.onopen = () => {
      const editor = editorRef.current;
      if (!editor) {
        return;
      }
      const uri = monaco.Uri.parse(solutionModelUri());
      let model = monaco.editor.getModel(uri);
      if (!model) {
        model = monaco.editor.createModel(debouncedSolution, "java", uri);
      } else {
        model.setValue(debouncedSolution);
      }
      editor.setModel(model);
      clientRef.current = attachJavaLanguageClient(socket);
      lspReadyRef.current = true;
      setLspStatus("ready");
    };

    socket.onerror = () => {
      if (!lspReadyRef.current) {
        setLspStatus("unavailable");
        setLspMessage(
          "Java language server unavailable. Build the LSP image and set CTL_LSP_ENABLED=true on the API.",
        );
      }
    };

    socket.onclose = (event) => {
      if (!lspReadyRef.current && event.code !== 1000) {
        setLspStatus("unavailable");
        setLspMessage(
          event.reason || "Language server connection closed. Editor still works without IntelliSense.",
        );
      }
    };

    return () => {
      lspReadyRef.current = false;
      clientRef.current?.stop();
      clientRef.current = null;
      socket.close();
      socketRef.current = null;
    };
  }, [debouncedSolution, lspEnabled]);

  return (
    <div className="flex h-full flex-col">
      {lspEnabled && lspStatus !== "off" && lspStatus !== "ready" && (
        <Alert
          className="mb-2 shrink-0"
          type={lspStatus === "connecting" ? "info" : "warning"}
          showIcon
          message={
            lspStatus === "connecting"
              ? "Connecting Java language server…"
              : "IntelliSense unavailable"
          }
          description={lspMessage ?? undefined}
        />
      )}
      <div className="min-h-0 flex-1">
        <Editor
          height={height}
          language="java"
          theme="vs-dark"
          value={value}
          onChange={(v) => onChange(v ?? "")}
          onMount={(editor) => {
            editorRef.current = editor;
          }}
          options={{
            minimap: { enabled: false },
            fontSize: 14,
            automaticLayout: true,
          }}
        />
      </div>
    </div>
  );
}
