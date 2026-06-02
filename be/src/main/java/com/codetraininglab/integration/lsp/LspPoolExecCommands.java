package com.codetraininglab.integration.lsp;

import java.util.ArrayList;
import java.util.List;

/** argv for {@code docker exec} into a pooled LSP container (entrypoint overridden to {@code sleep}). */
final class LspPoolExecCommands {

  private LspPoolExecCommands() {}

  static List<String> command(String language) {
    return switch (normalize(language)) {
      case "java" -> List.of("/entrypoint.sh");
      case "python" -> List.of("pyright-langserver", "--stdio");
      case "go" -> List.of("gopls", "serve");
      case "node", "typescript", "react", "angular" -> List.of("/entrypoint.sh");
      case "vue" -> List.of("/entrypoint.sh");
      case "csharp" -> List.of("csharp-ls");
      case "rust" -> List.of("rust-analyzer");
      case "cpp" ->
          List.of("clangd-14", "--background-index=0", "--compile-commands-dir=/workspace");
      default -> throw new IllegalArgumentException("Unsupported LSP language: " + language);
    };
  }

  static List<String> execEnvironment(String language) {
    List<String> env = new ArrayList<>();
    env.add("CTL_LSP_LANGUAGE=" + normalize(language));
    return env;
  }

  private static String normalize(String language) {
    return language == null ? "" : language.trim().toLowerCase();
  }
}
