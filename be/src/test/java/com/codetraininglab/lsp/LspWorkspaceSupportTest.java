package com.codetraininglab.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.codetraininglab.integration.lsp.LspWorkspaceSupport;

class LspWorkspaceSupportTest {

  static Stream<String> supportedLanguages() {
    return Stream.of(
        "java",
        "python",
        "go",
        "node",
        "typescript",
        "csharp",
        "rust",
        "cpp",
        "react",
        "vue",
        "angular");
  }

  @ParameterizedTest
  @MethodSource("supportedLanguages")
  void createsWorkspaceWithMainDocument(String language) throws Exception {
    var root = LspWorkspaceSupport.create(language, "");
    var mainPath = LspWorkspaceSupport.mainDocumentPath(language);
    assertThat(Files.exists(root.resolve(mainPath)))
        .as("main document for %s", language)
        .isTrue();
    deleteRecursively(root);
  }

  @Test
  void populateUpdatesExistingWorkspace() throws Exception {
    var root = Files.createTempDirectory("ctl-lsp-populate-");
    LspWorkspaceSupport.populate(root, "python", "x = 1\n");
    assertThat(Files.readString(root.resolve("solution.py"))).contains("x = 1");
    LspWorkspaceSupport.populate(root, "python", "y = 2\n");
    assertThat(Files.readString(root.resolve("solution.py"))).contains("y = 2");
    deleteRecursively(root);
  }

  @Test
  void createsMavenWorkspaceWithSolution() throws Exception {
    var root =
        LspWorkspaceSupport.create(
            "java", "package com.challenge; public class Solution {}");
    assertThat(Files.exists(root.resolve("pom.xml"))).isTrue();
    assertThat(Files.exists(root.resolve("src/main/java/com/challenge/Solution.java")))
        .isTrue();
    deleteRecursively(root);
  }

  private static void deleteRecursively(java.nio.file.Path root) throws Exception {
    try (var walk = Files.walk(root)) {
      walk.sorted(java.util.Comparator.reverseOrder())
          .forEach(
              path -> {
                try {
                  Files.deleteIfExists(path);
                } catch (java.io.IOException ignored) {
                  // best-effort cleanup
                }
              });
    }
  }
}
