package com.codetraininglab.integration.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LspContainerCleanupTest {

  @Test
  void orphansToRemoveSkipsActiveSessions() {
    Set<String> running =
        new LinkedHashSet<>(List.of("ctl-lsp-java-abc", "ctl-lsp-java-def", "quizzical_mendel"));
    Set<String> active = Set.of("ctl-lsp-java-abc");

    assertThat(LspContainerCleanup.orphansToRemove(running, active))
        .containsExactly("ctl-lsp-java-def", "quizzical_mendel");
  }

  @Test
  void orphansToRemoveEmptyWhenAllActive() {
    Set<String> names = Set.of("ctl-lsp-python-1");
    assertThat(LspContainerCleanup.orphansToRemove(names, names)).isEmpty();
  }

  @Test
  void orphansToRemoveKeepsPooledContainers() {
    Set<String> running =
        Set.of("ctl-lsp-pool-a1b2c3d4-java", "ctl-lsp-pool-b2c3d4e5-python");
    Set<String> active = Set.of("ctl-lsp-pool-a1b2c3d4-java");

    assertThat(LspContainerCleanup.orphansToRemove(running, active))
        .containsExactly("ctl-lsp-pool-b2c3d4e5-python");
  }
}
