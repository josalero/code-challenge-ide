/**
 * VS Code TextMate grammars + themes for syntax highlighting.
 * Required because @codingame/monaco-vscode-editor-api does not ship Monarch basic-languages.
 *
 * Before LSP: standard monaco-editor + theme="vs-dark" (built-in, instant).
 * After LSP: codingame monaco + TextMate stack (needed for language client).
 */
import "vscode/localExtensionHost";

import "@codingame/monaco-vscode-theme-defaults-default-extension";
import "@codingame/monaco-vscode-java-default-extension";
import "@codingame/monaco-vscode-python-default-extension";
import "@codingame/monaco-vscode-go-default-extension";
import "@codingame/monaco-vscode-javascript-default-extension";
import "@codingame/monaco-vscode-typescript-basics-default-extension";
import "@codingame/monaco-vscode-typescript-language-features-default-extension";
import "@codingame/monaco-vscode-html-default-extension";
import "@codingame/monaco-vscode-cpp-default-extension";
import "@codingame/monaco-vscode-csharp-default-extension";
import "@codingame/monaco-vscode-rust-default-extension";

import { whenReady as cppReady } from "@codingame/monaco-vscode-cpp-default-extension";
import { whenReady as csharpReady } from "@codingame/monaco-vscode-csharp-default-extension";
import { whenReady as goReady } from "@codingame/monaco-vscode-go-default-extension";
import { whenReady as htmlReady } from "@codingame/monaco-vscode-html-default-extension";
import { whenReady as javaReady } from "@codingame/monaco-vscode-java-default-extension";
import { whenReady as javascriptReady } from "@codingame/monaco-vscode-javascript-default-extension";
import { whenReady as pythonReady } from "@codingame/monaco-vscode-python-default-extension";
import { whenReady as rustReady } from "@codingame/monaco-vscode-rust-default-extension";
import { whenReady as themeDefaultsReady } from "@codingame/monaco-vscode-theme-defaults-default-extension";
import { whenReady as typescriptReady } from "@codingame/monaco-vscode-typescript-basics-default-extension";

import getConfigurationServiceOverride, {
  initUserConfiguration,
  updateUserConfiguration,
} from "@codingame/monaco-vscode-configuration-service-override";
import getLanguagesServiceOverride from "@codingame/monaco-vscode-languages-service-override";
import getTextmateServiceOverride from "@codingame/monaco-vscode-textmate-service-override";
import getThemeServiceOverride from "@codingame/monaco-vscode-theme-service-override";
import { initServices } from "monaco-languageclient/vscode/services";
import { monaco } from "./monacoSetup";
import { buildEditorUserConfiguration, EDITOR_THEME } from "./monacoTheme";

let initPromise: Promise<void> | null = null;

function applyEditorTheme(): void {
  monaco.editor.setTheme(EDITOR_THEME);
}

async function activateGrammarExtensions(): Promise<void> {
  await Promise.all([
    themeDefaultsReady(),
    javaReady(),
    pythonReady(),
    goReady(),
    javascriptReady(),
    typescriptReady(),
    htmlReady(),
    cppReady(),
    csharpReady(),
    rustReady(),
  ]);
}

/** Initialize TextMate + theme services once per page load. Also required for LSP. */
export function ensureMonacoEditorServices(): Promise<void> {
  if (!initPromise) {
    initPromise = (async () => {
      const configuration = buildEditorUserConfiguration();
      await initUserConfiguration(configuration);
      await initServices({
        caller: "code-training-lab-editor",
        serviceConfig: {
          debugLogging: import.meta.env.DEV,
          userServices: {
            ...getConfigurationServiceOverride(),
            ...getThemeServiceOverride(),
            ...getTextmateServiceOverride(),
            ...getLanguagesServiceOverride(),
          },
        },
      });
      await activateGrammarExtensions();
      try {
        await updateUserConfiguration(configuration);
      } catch (error) {
        if (import.meta.env.DEV) {
          console.warn("Could not update editor theme configuration", error);
        }
      }
      applyEditorTheme();
    })();
  }
  return initPromise;
}

export function monacoEditorBeforeMount(instance: typeof monaco): void {
  applyEditorThemeTo(instance);
}

/** Re-apply theme + grammar after the editor mounts (tokenization can miss the first paint). */
export function monacoEditorAfterMount(
  editor: monaco.editor.IStandaloneCodeEditor,
  instance: typeof monaco = monaco,
): void {
  applyEditorThemeTo(instance);
  const model = editor.getModel();
  if (model) {
    const languageId = model.getLanguageId();
    monaco.editor.setModelLanguage(model, languageId);
  }
}

function applyEditorThemeTo(instance: typeof monaco): void {
  instance.editor.setTheme(EDITOR_THEME);
}

/** Re-apply grammar when the model language changes (e.g. after mount). */
export function refreshEditorLanguageModel(
  model: monaco.editor.ITextModel,
  monacoLanguage: string,
): void {
  if (model.getLanguageId() !== monacoLanguage) {
    monaco.editor.setModelLanguage(model, monacoLanguage);
  }
}
