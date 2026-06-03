package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RunnerWarmSolutionsTest {

  @Test
  void solutionForReverseStringDoesNotContainStarterStub() {
    var solution = RunnerWarmSolutions.solutionFor("reverse-string");
    assertThat(solution).isPresent();
    assertThat(solution.get()).contains("StringBuilder");
    assertThat(solution.get()).doesNotContain("UnsupportedOperationException");
  }

  @Test
  void solutionForArmstrongNumberIsPresentAndPassesStarterContract() {
    var solution = RunnerWarmSolutions.solutionFor("armstrong-number");
    assertThat(solution).isPresent();
    assertThat(solution.get()).contains("def is_armstrong");
    assertThat(solution.get()).doesNotContain("NotImplementedError");
  }

  @Test
  void solutionForEverySmokeSlugIsDefined() {
    for (String slug :
        java.util.Set.of(
            "reverse-string",
            "armstrong-number",
            "anagram-check-go",
            "anagram-check-node",
            "anagram-check-typescript",
            "anagram-check-csharp",
            "anagram-check-rust",
            "anagram-check-cpp",
            "accordion-react",
            "computed-filter-vue",
            "double-service-angular",
            "sql-count-engineering")) {
      assertThat(RunnerWarmSolutions.solutionFor(slug))
          .as("warm solution for %s", slug)
          .isPresent();
    }
  }
}
