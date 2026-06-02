package com.codetraininglab.integration.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class LspUserLanguagePoolTest {

  private static final UUID USER = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

  @Test
  void poolKeyCombinesUserAndLanguage() {
    assertThat(LspUserLanguagePool.poolKey(USER, "Java"))
        .isEqualTo(USER + ":java");
  }

  @Test
  void poolContainerNameIsBoundedAndStable() {
    String name = LspUserLanguagePool.poolContainerName(USER, "typescript");
    assertThat(name).isEqualTo("ctl-lsp-pool-a1b2c3d4-typescript");
    assertThat(name.length()).isLessThanOrEqualTo(63);
  }

  @Test
  void poolExecCommandsCoverJavaAndPython() {
    assertThat(LspPoolExecCommands.command("java")).containsExactly("/entrypoint.sh");
    assertThat(LspPoolExecCommands.command("python"))
        .containsExactly("pyright-langserver", "--stdio");
    assertThat(LspPoolExecCommands.execEnvironment("vue"))
        .containsExactly("CTL_LSP_LANGUAGE=vue");
  }
}
