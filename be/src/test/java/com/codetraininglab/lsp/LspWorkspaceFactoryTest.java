package com.codetraininglab.integration.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import org.junit.jupiter.api.Test;

class LspWorkspaceFactoryTest {

  @Test
  void createsMavenWorkspaceWithSolution() throws Exception {
    var root = LspWorkspaceFactory.create("package com.challenge; public class Solution {}");
    assertThat(Files.exists(root.resolve("pom.xml"))).isTrue();
    assertThat(Files.exists(root.resolve("src/main/java/com/challenge/Solution.java"))).isTrue();
    try (var walk = Files.walk(root)) {
      walk.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (java.io.IOException ignored) {
          // best-effort cleanup
        }
      });
    }
  }
}
