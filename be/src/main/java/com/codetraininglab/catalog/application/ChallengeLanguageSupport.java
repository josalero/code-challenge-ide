package com.codetraininglab.catalog.application;

import com.codetraininglab.domain.WorkspaceLayout;

/** File layout and runner workspace mapping per challenge language. */
public final class ChallengeLanguageSupport {

  public record LanguageFiles(String starterRelativePath, String testFileSuffix, WorkspaceLayout workspaceLayout) {}

  private ChallengeLanguageSupport() {}

  public static LanguageFiles filesFor(String language) {
    if (language == null || language.isBlank()) {
      return javaFiles();
    }
    return switch (language.trim().toLowerCase()) {
      case "python" -> new LanguageFiles("starter/solution.py", ".py", WorkspaceLayout.PYTEST);
      case "go" -> new LanguageFiles("starter/solution.go", "_test.go", WorkspaceLayout.GO_TEST);
      case "node" -> new LanguageFiles("starter/solution.js", ".test.js", WorkspaceLayout.NODE_TEST);
      case "csharp" -> new LanguageFiles("starter/Solution.cs", ".cs", WorkspaceLayout.DOTNET);
      case "typescript" ->
          new LanguageFiles("starter/solution.ts", ".test.ts", WorkspaceLayout.TYPESCRIPT);
      case "rust" -> new LanguageFiles("starter/lib.rs", ".rs", WorkspaceLayout.CARGO);
      case "cpp" -> new LanguageFiles("starter/solution.cpp", ".cpp", WorkspaceLayout.CMAKE);
      case "react" -> new LanguageFiles("starter/solution.tsx", ".test.tsx", WorkspaceLayout.VITEST_REACT);
      case "vue" -> new LanguageFiles("starter/solution.vue", ".test.ts", WorkspaceLayout.VITEST_VUE);
      case "angular" -> new LanguageFiles("starter/solution.ts", ".test.ts", WorkspaceLayout.VITEST_ANGULAR);
      case "sql" -> new LanguageFiles("starter/solution.sql", ".py", WorkspaceLayout.POSTGRES_SQL);
      default -> javaFiles();
    };
  }

  public static String defaultRuntimeVersion(String language) {
    if (language == null || language.isBlank()) {
      return "26";
    }
    return switch (language.trim().toLowerCase()) {
      case "python" -> "3.12";
      case "go" -> "1.23";
      case "node" -> "22";
      case "csharp" -> "8.0";
      case "typescript" -> "5.7";
      case "rust" -> "1.84";
      case "cpp" -> "20";
      case "react" -> "19";
      case "vue" -> "3.5";
      case "angular" -> "19";
      case "sql" -> "17";
      default -> "26";
    };
  }

  private static LanguageFiles javaFiles() {
    return new LanguageFiles("starter/Solution.java", ".java", WorkspaceLayout.MAVEN);
  }
}
