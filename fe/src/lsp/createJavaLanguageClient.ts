import { MonacoLanguageClient } from "monaco-languageclient";
import {
  toSocket,
  WebSocketMessageReader,
  WebSocketMessageWriter,
} from "vscode-ws-jsonrpc";

const WORKSPACE_URI = "file:///workspace/src/main/java/com/challenge/Solution.java";

export function solutionModelUri(): string {
  return WORKSPACE_URI;
}

export function attachJavaLanguageClient(socket: WebSocket): MonacoLanguageClient {
  const client = new MonacoLanguageClient({
    name: "Java Language Client",
    clientOptions: {
      documentSelector: [{ language: "java" }],
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
  void client.start();
  return client;
}
