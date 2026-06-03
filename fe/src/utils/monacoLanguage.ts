/** Monaco editor language id for a challenge language. */
export function monacoLanguageFor(language: string | undefined): string {
  switch (language?.toLowerCase()) {
    case "java":
      return "java";
    case "python":
      return "python";
    case "go":
      return "go";
    case "node":
      return "javascript";
    case "csharp":
      return "csharp";
    case "typescript":
      return "typescript";
    case "react":
      return "typescriptreact";
    case "vue":
      return "html";
    case "angular":
      return "typescript";
    case "rust":
      return "rust";
    case "cpp":
      return "cpp";
    case "sql":
      return "sql";
    default:
      return "plaintext";
  }
}

export function usesLsp(language: string | undefined): boolean {
  switch (language?.toLowerCase()) {
    case "java":
    case "python":
    case "go":
    case "node":
    case "typescript":
    case "csharp":
    case "rust":
    case "cpp":
    case "react":
    case "vue":
    case "angular":
      return true;
    default:
      return false;
  }
}

/** @deprecated Use usesLsp */
export const usesJavaLsp = usesLsp;

export function customTestsTabLabel(language: string | undefined): string {
  switch (language?.toLowerCase()) {
    case "sql":
      return "Optional SQL checks";
    case "python":
      return "Optional pytest tests";
    case "go":
      return "Optional Go tests";
    case "node":
      return "Optional Node tests";
    case "csharp":
      return "Optional xUnit tests";
    case "typescript":
      return "Optional TypeScript tests";
    case "rust":
      return "Optional Rust tests";
    case "react":
      return "Optional Vitest + RTL tests";
    case "vue":
      return "Optional Vue Test Utils";
    case "angular":
      return "Optional Angular tests";
    default:
      return "Optional tests";
  }
}
