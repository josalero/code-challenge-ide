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
      initializationOptions:
        config.challengeLanguage === "java"
          ? {
              settings: {
                java: {
                  configuration: {
                    updateBuildConfiguration: "automatic",
                    // JDT runs on JDK 21 (the validated host) but compiles user
                    // code against JavaSE-26 via this path. The LSP image bundles
                    // JDK 26 at /opt/java-26 (see runners/lsp-java/Dockerfile);
                    // changing the path here without updating the image breaks
                    // Java IntelliSense.
                    runtimes: [
                      {
                        name: "JavaSE-26",
                        path: "/opt/java-26",
                        default: true,
                      },
                    ],
                  },
                  import: {
                    maven: {
                      enabled: true,
                    },
                  },
                },
              },
            }
          : undefined,
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
