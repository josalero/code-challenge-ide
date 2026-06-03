package com.codetraininglab.domain;

public enum WorkspaceLayout {
  MAVEN("maven"),
  PYTEST("pytest"),
  GO_TEST("go-test"),
  NODE_TEST("node-test"),
  DOTNET("dotnet"),
  TYPESCRIPT("typescript-test"),
  CARGO("cargo-test"),
  CMAKE("cmake-test"),
  VITEST_REACT("vitest-react"),
  VITEST_VUE("vitest-vue"),
  VITEST_ANGULAR("vitest-angular"),
  POSTGRES_SQL("postgres-sql");

  private final String id;

  WorkspaceLayout(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  public static WorkspaceLayout forLanguage(String languageName) {
    if (languageName == null || languageName.isBlank()) {
      return MAVEN;
    }
    return switch (languageName.toLowerCase()) {
      case "python" -> PYTEST;
      case "go" -> GO_TEST;
      case "node" -> NODE_TEST;
      case "csharp" -> DOTNET;
      case "typescript" -> TYPESCRIPT;
      case "rust" -> CARGO;
      case "cpp" -> CMAKE;
      case "react" -> VITEST_REACT;
      case "vue" -> VITEST_VUE;
      case "angular" -> VITEST_ANGULAR;
      case "sql" -> POSTGRES_SQL;
      default -> MAVEN;
    };
  }
}
