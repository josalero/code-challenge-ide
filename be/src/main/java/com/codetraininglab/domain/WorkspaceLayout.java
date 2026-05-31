package com.codetraininglab.domain;

public enum WorkspaceLayout {
  MAVEN("maven"),
  PYTEST("pytest");

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
      default -> MAVEN;
    };
  }
}
