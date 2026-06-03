package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.domain.WorkspaceLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChallengeLanguageSupportTest {

  @ParameterizedTest
  @CsvSource({
    "java, starter/Solution.java, .java, MAVEN, 26",
    "python, starter/solution.py, .py, PYTEST, 3.12",
    "go, starter/solution.go, _test.go, GO_TEST, 1.23",
    "node, starter/solution.js, .test.js, NODE_TEST, 22",
    "typescript, starter/solution.ts, .test.ts, TYPESCRIPT, 5.7",
    "csharp, starter/Solution.cs, .cs, DOTNET, 8.0",
    "rust, starter/lib.rs, .rs, CARGO, 1.84",
    "cpp, starter/solution.cpp, .cpp, CMAKE, 20",
    "react, starter/solution.tsx, .test.tsx, VITEST_REACT, 19",
    "vue, starter/solution.vue, .test.ts, VITEST_VUE, 3.5",
    "angular, starter/solution.ts, .test.ts, VITEST_ANGULAR, 19",
    "sql, starter/solution.sql, .py, POSTGRES_SQL, 17",
  })
  void resolvesFilesAndRuntime(
      String language, String starter, String suffix, WorkspaceLayout layout, String runtime) {
    var files = ChallengeLanguageSupport.filesFor(language);
    assertThat(files.starterRelativePath()).isEqualTo(starter);
    assertThat(files.testFileSuffix()).isEqualTo(suffix);
    assertThat(files.workspaceLayout()).isEqualTo(layout);
    assertThat(ChallengeLanguageSupport.defaultRuntimeVersion(language)).isEqualTo(runtime);
  }

  @Test
  void defaultsToJavaWhenLanguageMissing() {
    var files = ChallengeLanguageSupport.filesFor(null);
    assertThat(files.starterRelativePath()).isEqualTo("starter/Solution.java");
    assertThat(ChallengeLanguageSupport.defaultRuntimeVersion("")).isEqualTo("26");
  }

  @Test
  void treatsUnknownLanguageAsJava() {
    var files = ChallengeLanguageSupport.filesFor("kotlin");
    assertThat(files.testFileSuffix()).isEqualTo(".java");
    assertThat(files.workspaceLayout()).isEqualTo(WorkspaceLayout.MAVEN);
  }
}
