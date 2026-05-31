package com.codetraininglab.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WorkspaceLayoutTest {

  @Test
  void mapsJavaToMaven() {
    assertThat(WorkspaceLayout.forLanguage("java")).isEqualTo(WorkspaceLayout.MAVEN);
  }

  @Test
  void mapsPythonToPytest() {
    assertThat(WorkspaceLayout.forLanguage("python")).isEqualTo(WorkspaceLayout.PYTEST);
  }

  @Test
  void defaultsUnknownLanguageToMaven() {
    assertThat(WorkspaceLayout.forLanguage("go")).isEqualTo(WorkspaceLayout.MAVEN);
  }
}
