import { MonacoLanguageClient } from "monaco-languageclient";
import type { DocumentFilter } from "vscode-languageclient";
import {
  toSocket,
  WebSocketMessageReader,
  WebSocketMessageWriter,
} from "vscode-ws-jsonrpc";
import { monaco } from "../monacoSetup";
import type { LspLanguageConfig } from "./lspLanguageConfig";
import { LSP_WORKSPACE_URI } from "./lspLanguageConfig";

export async function attachMonacoLanguageClient(
  socket: WebSocket,
  config: LspLanguageConfig,
): Promise<MonacoLanguageClient> {
  const client = new MonacoLanguageClient({
    name: config.clientName,
    clientOptions: {
      documentSelector: config.documentSelector as DocumentFilter[],
      workspaceFolder: {
        index: 0,
        name: "workspace",
        uri: monaco.Uri.parse(LSP_WORKSPACE_URI),
      },
    },
    connectionProvider: {
      get: async () => {
        const webSocket = toSocket(socket);
        return {
          reader: new WebSocketMessageReader(webSocket),
          writer: new WebSocketMessageWriter(webSocket),
        };
      },
    },
  });
  await client.start();
  return client;
}
