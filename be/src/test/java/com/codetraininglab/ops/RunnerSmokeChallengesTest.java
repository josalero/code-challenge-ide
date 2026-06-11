package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RunnerSmokeChallengesTest {

  @ParameterizedTest(name = "{0} → {1}")
  @MethodSource("expectedSmokeSlugs")
  void mapsLanguageToCanonicalSmokeSlug(String language, String slug) {
    assertThat(RunnerSmokeChallenges.slugFor(language)).isEqualTo(slug);
  }

  static Stream<Arguments> expectedSmokeSlugs() {
    return Stream.of(
        Arguments.of("java", "reverse-string"),
        Arguments.of("python", "armstrong-number"),
        Arguments.of("go", "anagram-check-go"),
        Arguments.of("node", "gcd-node"),
        Arguments.of("typescript", "gcd-typescript"),
        Arguments.of("csharp", "anagram-check-csharp"),
        Arguments.of("rust", "anagram-check-rust"),
        Arguments.of("cpp", "anagram-check-cpp"),
        Arguments.of("react", "accordion-react"),
        Arguments.of("vue", "counter-vue"),
        Arguments.of("angular", "double-service-angular"),
        Arguments.of("sql", "sql-count-engineering"));
  }

  @ParameterizedTest
  @MethodSource("expectedSmokeSlugs")
  void slugForIsStableAcrossCasing(String language, String slug) {
    assertThat(RunnerSmokeChallenges.slugFor(language.toUpperCase())).isEqualTo(slug);
    assertThat(RunnerSmokeChallenges.slugFor("  " + language + "  ")).isEqualTo(slug);
  }
}
