package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.codetraininglab.integration.runner.RunnerContainerPool;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.json.JsonMapper;

/**
 * Verifies smoke warm plans for every catalog language using repo challenges (no Docker required).
 */
class RunnerPoolWarmExecutorLanguageTest {

  private static Path challengesRoot;

  @BeforeAll
  static void resolveChallengesRoot() {
    challengesRoot =
        locateRepoChallenges()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Repo challenges/ not found — run tests from code-challenge-ide checkout"));
  }

  @ParameterizedTest(name = "{0} → {1}")
  @MethodSource("smokeLanguages")
  void buildWarmPlanUsesPassingSolutionForEachLanguage(
      String language, String expectedSlug, String expectedLayout) throws Exception {
    assumeSmokeChallengePresent(expectedSlug);

    RunnerPoolWarmExecutor executor = executorFor(challengesRoot);
    Object plan = invokeBuildWarmPlan(executor, language, language + " runtime");

    assertThat(plan).isNotNull();
    assertThat(readRecordComponent(plan, "slug")).isEqualTo(expectedSlug);
    assertThat(readRecordComponent(plan, "workspaceLayout")).isEqualTo(expectedLayout);
    String starterCode = (String) readRecordComponent(plan, "starterCode");
    assertThat(starterCode).isNotBlank();
    assertThat(starterCode).doesNotContain("UnsupportedOperationException");
    assertThat(starterCode).doesNotContain("NotImplementedError");
    assertThat(RunnerWarmSolutions.solutionFor(expectedSlug)).isPresent();
    assertThat(starterCode).isEqualTo(RunnerWarmSolutions.solutionFor(expectedSlug).orElseThrow());
  }

  static Stream<Arguments> smokeLanguages() {
    return Stream.of(
        Arguments.of("java", "reverse-string", "maven"),
        Arguments.of("python", "armstrong-number", "pytest"),
        Arguments.of("go", "anagram-check-go", "go-test"),
        Arguments.of("node", "anagram-check-node", "node-test"),
        Arguments.of("typescript", "anagram-check-typescript", "typescript-test"),
        Arguments.of("csharp", "anagram-check-csharp", "dotnet"),
        Arguments.of("rust", "anagram-check-rust", "cargo-test"),
        Arguments.of("cpp", "anagram-check-cpp", "cmake-test"),
        Arguments.of("react", "accordion-react", "vitest-react"),
        Arguments.of("vue", "computed-filter-vue", "vitest-vue"),
        Arguments.of("angular", "double-service-angular", "vitest-angular"));
  }

  private static void assumeSmokeChallengePresent(String slug) {
    Path challengeDir = challengesRoot.resolve(slug);
    org.junit.jupiter.api.Assumptions.assumeTrue(
        Files.isDirectory(challengeDir),
        () -> "Smoke challenge missing in checkout: " + challengeDir);
  }

  private static RunnerPoolWarmExecutor executorFor(Path challengesPath) {
    CtlProperties properties =
        new CtlProperties(
            CtlPropertiesTestFixtures.defaults().registrationEnabled(),
            CtlPropertiesTestFixtures.defaults().jwtSecret(),
            24,
            "http://localhost:5173",
            challengesPath.toString(),
            "code-challenge-ide-runner-java-26:local",
            "ctl-runner-m2-cache",
            true,
            60,
            CtlPropertiesTestFixtures.TEST_LSP_IMAGES,
            5,
            24,
            "openrouter",
            "",
            "model",
            "http://localhost:11434",
            "qwen",
            true,
            false,
            false);
    return new RunnerPoolWarmExecutor(
        properties,
        mock(LanguageRuntimeRepository.class),
        mock(LanguageRepository.class),
        mock(RunnerContainerPool.class),
        mock(RunnerWarmStateStore.class),
        JsonMapper.builder().build());
  }

  private static Object invokeBuildWarmPlan(RunnerPoolWarmExecutor executor, String language, String label)
      throws Exception {
    Method method =
        RunnerPoolWarmExecutor.class.getDeclaredMethod("buildWarmPlan", String.class, String.class);
    method.setAccessible(true);
    return method.invoke(executor, language, label);
  }

  private static Object readRecordComponent(Object record, String componentName) throws Exception {
    for (RecordComponent component : record.getClass().getRecordComponents()) {
      if (component.getName().equals(componentName)) {
        return component.getAccessor().invoke(record);
      }
    }
    throw new IllegalArgumentException("No component " + componentName + " on " + record.getClass());
  }

  private static java.util.Optional<Path> locateRepoChallenges() {
    Path current = Path.of("").toAbsolutePath();
    for (Path candidate = current; candidate != null; candidate = candidate.getParent()) {
      Path challenges = candidate.resolve("challenges");
      if (Files.isDirectory(challenges.resolve("reverse-string"))) {
        return java.util.Optional.of(challenges);
      }
    }
    return java.util.Optional.empty();
  }
}
