import { ApiPaths } from "../domain/constants";
import { monacoLanguageFor } from "../utils/monacoLanguage";

const WORKSPACE_ROOT = "file:///workspace";

export type LspLanguageConfig = {
  challengeLanguage: string;
  lspPath: string;
  modelUri: string;
  monacoLanguage: string;
  documentSelector: Array<{ language?: string; pattern?: string }>;
  clientName: string;
  displayName: string;
};

function workspaceFile(relativePath: string): string {
  return `${WORKSPACE_ROOT}/${relativePath}`;
}

const CONFIGS: Record<string, Omit<LspLanguageConfig, "challengeLanguage">> = {
  java: {
    lspPath: ApiPaths.lsp("java"),
    modelUri: workspaceFile("src/main/java/com/challenge/Solution.java"),
    monacoLanguage: "java",
    documentSelector: [{ language: "java" }],
    clientName: "Java Language Client",
    displayName: "Java",
  },
  python: {
    lspPath: ApiPaths.lsp("python"),
    modelUri: workspaceFile("solution.py"),
    monacoLanguage: "python",
    documentSelector: [{ language: "python" }],
    clientName: "Python Language Client",
    displayName: "Python",
  },
  go: {
    lspPath: ApiPaths.lsp("go"),
    modelUri: workspaceFile("solution.go"),
    monacoLanguage: "go",
    documentSelector: [{ language: "go" }],
    clientName: "Go Language Client",
    displayName: "Go",
  },
  node: {
    lspPath: ApiPaths.lsp("node"),
    modelUri: workspaceFile("solution.js"),
    monacoLanguage: "javascript",
    documentSelector: [{ language: "javascript" }],
    clientName: "JavaScript Language Client",
    displayName: "Node",
  },
  typescript: {
    lspPath: ApiPaths.lsp("typescript"),
    modelUri: workspaceFile("solution.ts"),
    monacoLanguage: "typescript",
    documentSelector: [{ language: "typescript" }],
    clientName: "TypeScript Language Client",
    displayName: "TypeScript",
  },
  react: {
    lspPath: ApiPaths.lsp("react"),
    modelUri: workspaceFile("solution.tsx"),
    monacoLanguage: "typescriptreact",
    documentSelector: [{ language: "typescriptreact" }],
    clientName: "React Language Client",
    displayName: "React",
  },
  vue: {
    lspPath: ApiPaths.lsp("vue"),
    modelUri: workspaceFile("solution.vue"),
    monacoLanguage: "html",
    documentSelector: [{ pattern: workspaceFile("**/*.vue") }],
    clientName: "Vue Language Client",
    displayName: "Vue",
  },
  angular: {
    lspPath: ApiPaths.lsp("angular"),
    modelUri: workspaceFile("solution.ts"),
    monacoLanguage: "typescript",
    documentSelector: [{ language: "typescript" }],
    clientName: "Angular Language Client",
    displayName: "Angular",
  },
  csharp: {
    lspPath: ApiPaths.lsp("csharp"),
    modelUri: workspaceFile("Solution.cs"),
    monacoLanguage: "csharp",
    documentSelector: [{ language: "csharp" }],
    clientName: "C# Language Client",
    displayName: "C#",
  },
  rust: {
    lspPath: ApiPaths.lsp("rust"),
    modelUri: workspaceFile("src/lib.rs"),
    monacoLanguage: "rust",
    documentSelector: [{ language: "rust" }],
    clientName: "Rust Language Client",
    displayName: "Rust",
  },
  cpp: {
    lspPath: ApiPaths.lsp("cpp"),
    modelUri: workspaceFile("solution.cpp"),
    monacoLanguage: "cpp",
    documentSelector: [{ language: "cpp" }],
    clientName: "C++ Language Client",
    displayName: "C++",
  },
};

export function lspConfigFor(language: string | undefined): LspLanguageConfig | null {
  const key = language?.trim().toLowerCase();
  if (!key || !(key in CONFIGS)) {
    return null;
  }
  return { challengeLanguage: key, ...CONFIGS[key] };
}

export function usesLsp(language: string | undefined): boolean {
  return lspConfigFor(language) !== null;
}

/** Monaco language id aligned with challenge language when LSP is off. */
export function editorLanguageFor(language: string | undefined): string {
  return lspConfigFor(language)?.monacoLanguage ?? monacoLanguageFor(language);
}

export function solutionModelUri(language: string | undefined): string {
  return lspConfigFor(language)?.modelUri ?? "file:///workspace/solution";
}

/** Model URI for the custom-tests tab (extension must match language grammar). */
export function customTestsModelUri(language: string | undefined): string {
  const solution = solutionModelUri(language);
  if (solution.endsWith("Solution.java")) {
    return workspaceFile("src/test/java/com/challenge/CustomTests.java");
  }
  if (solution.endsWith(".py")) {
    return workspaceFile("custom_tests.py");
  }
  if (solution.endsWith(".go")) {
    return workspaceFile("custom_tests_test.go");
  }
  if (solution.endsWith(".js")) {
    return workspaceFile("custom_tests.test.js");
  }
  if (solution.endsWith(".ts")) {
    return workspaceFile("custom_tests.test.ts");
  }
  if (solution.endsWith(".tsx")) {
    return workspaceFile("custom_tests.test.tsx");
  }
  if (solution.endsWith(".vue")) {
    return workspaceFile("custom_tests.test.vue");
  }
  if (solution.endsWith("Solution.cs")) {
    return workspaceFile("CustomTests.cs");
  }
  if (solution.endsWith(".rs")) {
    return workspaceFile("tests/custom_tests.rs");
  }
  if (solution.endsWith(".cpp")) {
    return workspaceFile("custom_tests.cpp");
  }
  if (solution.endsWith(".sql")) {
    return workspaceFile("custom_tests.sql");
  }
  return `${solution}.custom`;
}

export const LSP_WORKSPACE_URI = WORKSPACE_ROOT;

function sanitizeJavaIdentifier(value: string): string {
  const cleaned = value.replace(/[^a-zA-Z0-9_]/g, "");
  return cleaned || "Test";
}

function createChallengeExtension(language: string): string {
  switch (language) {
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

/**
 * Monaco model URI for create-challenge editors.
 * Java sources must live under Maven paths so package declarations validate correctly.
 */
export function createChallengeModelUri(language: string | undefined, modelId: string): string {
  const lang = language?.trim().toLowerCase() ?? "";
  const safeId = modelId.replace(/[^a-zA-Z0-9_.-]/g, "-");

  if (lang === "java") {
    if (modelId === "starterCode") {
      return workspaceFile("src/main/java/com/challenge/Solution.java");
    }
    const publicMatch = /^publicTests\.(.+)\.source$/.exec(modelId);
    if (publicMatch) {
      const testName = sanitizeJavaIdentifier(publicMatch[1]);
      return workspaceFile(`src/test/java/com/challenge/tests/${testName}.java`);
    }
    const hiddenMatch = /^hiddenTests\.(.+)\.source$/.exec(modelId);
    if (hiddenMatch) {
      const testName = sanitizeJavaIdentifier(hiddenMatch[1]);
      return workspaceFile(`src/test/java/com/challenge/hidden/${testName}.java`);
    }
  }

  return `file:///challenge-create/${lang || "plain"}/${safeId}.${createChallengeExtension(lang)}`;
}
