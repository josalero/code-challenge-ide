/**
 * Single Monaco instance for @monaco-editor/react and monaco-languageclient (LSP).
 * @see https://github.com/suren-atoyan/monaco-react#use-monaco-editor-as-an-npm-package
 */
import { loader } from "@monaco-editor/react";
import * as monaco from "monaco-editor";
import textMateWorker from "@codingame/monaco-vscode-textmate-service-override/worker?worker";
import editorWorker from "monaco-editor/esm/vs/editor/editor.worker?worker";
import cssWorker from "monaco-editor/esm/vs/language/css/css.worker?worker";
import htmlWorker from "monaco-editor/esm/vs/language/html/html.worker?worker";
import jsonWorker from "monaco-editor/esm/vs/language/json/json.worker?worker";
import tsWorker from "monaco-editor/esm/vs/language/typescript/ts.worker?worker";

type WorkerFactory = () => Worker;

const workerLoaders: Record<string, WorkerFactory> = {
  TextEditorWorker: () => new editorWorker(),
  TextMateWorker: () => new textMateWorker(),
  json: () => new jsonWorker(),
  css: () => new cssWorker(),
  scss: () => new cssWorker(),
  less: () => new cssWorker(),
  html: () => new htmlWorker(),
  handlebars: () => new htmlWorker(),
  razor: () => new htmlWorker(),
  typescript: () => new tsWorker(),
  javascript: () => new tsWorker(),
  typescriptreact: () => new tsWorker(),
  javascriptreact: () => new tsWorker(),
};

const monacoEnvironment = {
  getWorker(_workerId: string, label: string) {
    const factory = workerLoaders[label];
    if (factory) {
      return factory();
    }
    return new editorWorker();
  },
};

Object.defineProperty(globalThis, "MonacoEnvironment", {
  value: monacoEnvironment,
  writable: true,
  configurable: true,
});

loader.config({ monaco });

export { monaco };
