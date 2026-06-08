package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.RunnerStatus;
import com.codetraininglab.integration.runner.RunnerContainerPool;
import com.codetraininglab.integration.runner.RunnerJobPayload;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class RunnerPoolWarmExecutorWarmFlowTest {

  private static final String JAVA_IMAGE = "code-challenge-ide-runner-java-26:local";
  private static final String STAMP = "sha256:warm-stamp";

  @TempDir Path tempDir;

  @Mock private LanguageRuntimeRepository runtimeRepository;
  @Mock private LanguageRepository languageRepository;
  @Mock private RunnerContainerPool runnerContainerPool;
  @Mock private RunnerWarmStateStore warmStateStore;

  private Path challengesRoot;
  private CtlProperties properties;

  @BeforeEach
  void setUp() throws Exception {
    challengesRoot = tempDir.resolve("challenges");
    Files.createDirectories(challengesRoot.resolve("reverse-string/starter"));
    Files.writeString(
        challengesRoot.resolve("reverse-string/starter/Solution.java"),
        "package com.challenge; public class Solution {}");
    properties = poolEnabledProperties(challengesRoot.toString());
  }

  @Test
  void warmSkipsRuntimeWhenLanguageFilterDoesNotMatch() {
    stubExecutor(image -> new RunnerPoolWarmExecutor.ImageIdentity(true, STAMP))
        .warm(false, List.of("python"), log -> {});

    verify(runnerContainerPool, never()).execute(any(), any(), any(), any(), any());
  }

  @Test
  void warmSkipsRuntimeWhenInactive() {
    UUID languageId = UUID.randomUUID();
    when(languageRepository.findAll())
        .thenReturn(List.of(new LanguageEntity(languageId, "java", "Java")));
    when(runtimeRepository.findAllOrdered())
        .thenReturn(
            List.of(
                new LanguageRuntimeEntity(
                    UUID.randomUUID(), languageId, "26", JAVA_IMAGE, false)));
    when(warmStateStore.runnerPoolStampByImage()).thenReturn(new LinkedHashMap<>());

    executor(image -> new RunnerPoolWarmExecutor.ImageIdentity(true, STAMP))
        .warm(false, List.of(), log -> {});

    verify(runnerContainerPool, never()).execute(any(), any(), any(), any(), any());
  }

  @Test
  void warmSkipsWhenImageAlreadyStampedAndForceIsFalse() {
    UUID languageId = UUID.randomUUID();
    when(languageRepository.findAll())
        .thenReturn(List.of(new LanguageEntity(languageId, "java", "Java")));
    when(runtimeRepository.findAllOrdered())
        .thenReturn(
            List.of(
                new LanguageRuntimeEntity(
                    UUID.randomUUID(), languageId, "26", JAVA_IMAGE, true)));
    when(warmStateStore.runnerPoolStampByImage())
        .thenReturn(new LinkedHashMap<>(Map.of(JAVA_IMAGE, STAMP)));
    when(runnerContainerPool.refreshIdleTimerForImage(JAVA_IMAGE)).thenReturn(true);

    var log = new StringBuilder();
    executor(image -> new RunnerPoolWarmExecutor.ImageIdentity(true, STAMP))
        .warm(false, List.of("java"), log::append);

    verify(runnerContainerPool, never()).execute(any(), any(), any(), any(), any());
    verify(runnerContainerPool).refreshIdleTimerForImage(JAVA_IMAGE);
    verify(warmStateStore).recordRunnerPoolWarm(JAVA_IMAGE, STAMP);
    assertThat(log).contains("already warmed");
  }

  @Test
  void warmRecreatesPoolWhenImageAlreadyStampedButContainerIsMissing() {
    UUID languageId = UUID.randomUUID();
    when(languageRepository.findAll())
        .thenReturn(List.of(new LanguageEntity(languageId, "java", "Java")));
    when(runtimeRepository.findAllOrdered())
        .thenReturn(
            List.of(
                new LanguageRuntimeEntity(
                    UUID.randomUUID(), languageId, "26", JAVA_IMAGE, true)));
    when(warmStateStore.runnerPoolStampByImage())
        .thenReturn(new LinkedHashMap<>(Map.of(JAVA_IMAGE, STAMP)));
    when(runnerContainerPool.execute(
            eq(JAVA_IMAGE),
            eq(challengesRoot.resolve("reverse-string")),
            eq("maven"),
            any(),
            any(RunnerJobPayload.RunnerLimits.class)))
        .thenReturn(
            new RunnerResult(
                RunnerStatus.COMPLETED.name(),
                List.of(new RunnerResult.TestOutcome("smoke", "PASS", null, 1)),
                new RunnerResult.CoverageOutcome(0, 0),
                new RunnerResult.CompileOutcome(0, List.of()),
                null,
                null));

    var log = new StringBuilder();
    executor(image -> new RunnerPoolWarmExecutor.ImageIdentity(true, STAMP))
        .warm(false, List.of("java"), log::append);

    verify(runnerContainerPool)
        .execute(
            eq(JAVA_IMAGE),
            eq(challengesRoot.resolve("reverse-string")),
            eq("maven"),
            any(),
            any(RunnerJobPayload.RunnerLimits.class));
    assertThat(log).contains("warm stamp exists but pool container is not running");
  }

  @Test
  void warmRecordsWarmStampAfterSuccessfulSmoke() {
    UUID languageId = UUID.randomUUID();
    when(languageRepository.findAll())
        .thenReturn(List.of(new LanguageEntity(languageId, "java", "Java")));
    when(runtimeRepository.findAllOrdered())
        .thenReturn(
            List.of(
                new LanguageRuntimeEntity(
                    UUID.randomUUID(), languageId, "26", JAVA_IMAGE, true)));
    when(warmStateStore.runnerPoolStampByImage()).thenReturn(new LinkedHashMap<>());
    when(runnerContainerPool.execute(
            eq(JAVA_IMAGE),
            eq(challengesRoot.resolve("reverse-string")),
            eq("maven"),
            any(),
            any(RunnerJobPayload.RunnerLimits.class)))
        .thenReturn(
            new RunnerResult(
                RunnerStatus.COMPLETED.name(),
                List.of(new RunnerResult.TestOutcome("smoke", "PASS", null, 1)),
                new RunnerResult.CoverageOutcome(0, 0),
                new RunnerResult.CompileOutcome(0, List.of()),
                null,
                null));

    Map<String, String> stamp =
        executor(image -> new RunnerPoolWarmExecutor.ImageIdentity(true, STAMP))
            .warm(true, List.of("java"), log -> {});

    assertThat(stamp).containsEntry(JAVA_IMAGE, STAMP);
    verify(warmStateStore).recordRunnerPoolWarm(JAVA_IMAGE, STAMP);
    verify(runnerContainerPool)
        .execute(
            eq(JAVA_IMAGE),
            eq(challengesRoot.resolve("reverse-string")),
            eq("maven"),
            any(),
            any(RunnerJobPayload.RunnerLimits.class));
  }

  @Test
  void warmThrowsWhenInfrastructureFails() {
    UUID languageId = UUID.randomUUID();
    when(languageRepository.findAll())
        .thenReturn(List.of(new LanguageEntity(languageId, "java", "Java")));
    when(runtimeRepository.findAllOrdered())
        .thenReturn(
            List.of(
                new LanguageRuntimeEntity(
                    UUID.randomUUID(), languageId, "26", JAVA_IMAGE, true)));
    when(warmStateStore.runnerPoolStampByImage()).thenReturn(new LinkedHashMap<>());
    when(runnerContainerPool.execute(any(), any(), any(), any(), any()))
        .thenReturn(
            new RunnerResult(
                RunnerStatus.FAILED.name(),
                List.of(new RunnerResult.TestOutcome("runner", "FAIL", "timeout", 0)),
                new RunnerResult.CoverageOutcome(0, 0),
                new RunnerResult.CompileOutcome(0, List.of()),
                null,
                null));

    assertThatThrownBy(
            () ->
                executor(image -> new RunnerPoolWarmExecutor.ImageIdentity(true, STAMP))
                    .warm(true, List.of("java"), log -> {}))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Smoke warm failed");
  }

  @Test
  void warmThrowsWhenSmokeTestsFail() {
    UUID languageId = UUID.randomUUID();
    when(languageRepository.findAll())
        .thenReturn(List.of(new LanguageEntity(languageId, "java", "Java")));
    when(runtimeRepository.findAllOrdered())
        .thenReturn(
            List.of(
                new LanguageRuntimeEntity(
                    UUID.randomUUID(), languageId, "26", JAVA_IMAGE, true)));
    when(warmStateStore.runnerPoolStampByImage()).thenReturn(new LinkedHashMap<>());
    when(runnerContainerPool.execute(any(), any(), any(), any(), any()))
        .thenReturn(
            new RunnerResult(
                RunnerStatus.COMPLETED.name(),
                List.of(new RunnerResult.TestOutcome("smoke", "FAIL", "assertion", 0)),
                new RunnerResult.CoverageOutcome(0, 0),
                new RunnerResult.CompileOutcome(0, List.of()),
                null,
                null));

    assertThatThrownBy(
            () ->
                executor(image -> new RunnerPoolWarmExecutor.ImageIdentity(true, STAMP))
                    .warm(true, List.of("java"), log -> {}))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("did not pass");
  }

  @Test
  void normalizeLanguageFiltersTrimsAndLowercases() throws Exception {
    var onlyLanguages = new ArrayList<String>();
    onlyLanguages.add(" Java ");
    onlyLanguages.add("");
    onlyLanguages.add(null);
    onlyLanguages.add("PYTHON");
    @SuppressWarnings("unchecked")
    var filters =
        (LinkedHashSet<String>) invokeStatic("normalizeLanguageFilters", onlyLanguages);

    assertThat(filters).containsExactly("java", "python");
  }

  @Test
  void normalizeLanguageFiltersNullReturnsEmpty() throws Exception {
    @SuppressWarnings("unchecked")
    var filters = (LinkedHashSet<String>) invokeStatic("normalizeLanguageFilters", null);

    assertThat(filters).isEmpty();
  }

  private RunnerPoolWarmExecutor executor(
      Function<String, RunnerPoolWarmExecutor.ImageIdentity> probe) {
    return new RunnerPoolWarmExecutor(
        properties,
        runtimeRepository,
        languageRepository,
        runnerContainerPool,
        warmStateStore,
        JsonMapper.builder().build()) {
      @Override
      RunnerPoolWarmExecutor.ImageIdentity inspectImage(String image) {
        return probe.apply(image);
      }
    };
  }

  private RunnerPoolWarmExecutor stubExecutor(
      Function<String, RunnerPoolWarmExecutor.ImageIdentity> probe) {
    UUID languageId = UUID.randomUUID();
    when(languageRepository.findAll())
        .thenReturn(List.of(new LanguageEntity(languageId, "java", "Java")));
    when(runtimeRepository.findAllOrdered())
        .thenReturn(
            List.of(
                new LanguageRuntimeEntity(
                    UUID.randomUUID(), languageId, "26", JAVA_IMAGE, true)));
    when(warmStateStore.runnerPoolStampByImage()).thenReturn(new LinkedHashMap<>());
    return executor(probe);
  }

  private static CtlProperties poolEnabledProperties(String challengesPath) {
    var base = CtlPropertiesTestFixtures.defaults(challengesPath);
    return new CtlProperties(
        base.registrationEnabled(),
        base.jwtSecret(),
        base.jwtExpirationHours(),
        base.corsAllowedOrigins(),
        challengesPath,
        base.runnerJava26Image(),
        base.runnerMavenCacheVolume(),
        true,
        base.runnerPoolIdleMinutes(),
        base.lspImages(),
        base.lspIdleMinutes(),
        base.idempotencyTtlHours(),
        base.aiProvider(),
        base.openrouterApiKey(),
        base.openrouterModel(),
        base.ollamaBaseUrl(),
        base.ollamaModel(),
        base.dockerEnabled(),
        base.lspEnabled(),
        base.runnerPoolWarmOnStartup(), base.userMaxStartedChallenges());
  }

  private static Object invokeStatic(String name, Object arg) throws Exception {
    Method method =
        RunnerPoolWarmExecutor.class.getDeclaredMethod("normalizeLanguageFilters", List.class);
    method.setAccessible(true);
    return method.invoke(null, arg);
  }
}
