import path from "node:path";
import importMetaUrlPlugin from "@codingame/esbuild-import-meta-url-plugin";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

const grammarExtensions = [
  "@codingame/monaco-vscode-theme-defaults-default-extension",
  "@codingame/monaco-vscode-java-default-extension",
  "@codingame/monaco-vscode-python-default-extension",
  "@codingame/monaco-vscode-go-default-extension",
  "@codingame/monaco-vscode-javascript-default-extension",
  "@codingame/monaco-vscode-typescript-basics-default-extension",
  "@codingame/monaco-vscode-typescript-language-features-default-extension",
  "@codingame/monaco-vscode-html-default-extension",
  "@codingame/monaco-vscode-cpp-default-extension",
  "@codingame/monaco-vscode-csharp-default-extension",
  "@codingame/monaco-vscode-rust-default-extension",
];

export default defineConfig({
  plugins: [react()],
  worker: {
    format: "es",
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
    dedupe: ["vscode", "monaco-editor"],
  },
  optimizeDeps: {
    include: [
      "monaco-editor",
      "monaco-languageclient",
      "vscode-ws-jsonrpc",
      "vscode/localExtensionHost",
      "vscode-textmate",
      "vscode-oniguruma",
      "@codingame/monaco-vscode-textmate-service-override",
      "@codingame/monaco-vscode-theme-service-override",
      "@codingame/monaco-vscode-configuration-service-override",
      "@codingame/monaco-vscode-languages-service-override",
      "@codingame/monaco-vscode-files-service-override",
      ...grammarExtensions,
    ],
    esbuildOptions: {
      plugins: [importMetaUrlPlugin],
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        ws: true,
      },
    },
  },
});
