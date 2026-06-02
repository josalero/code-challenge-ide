package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.RunnerStatus;
import com.codetraininglab.integration.runner.RunnerContainerPool;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class RunnerPoolWarmExecutorTest {

  @TempDir Path tempDir;

  @Test
  void warmSkipsWhenRunnerPoolDisabled() {
    var base = CtlPropertiesTestFixtures.defaults(tempDir.resolve("challenges").toString());
    CtlProperties properties =
        new CtlProperties(
            base.registrationEnabled(),
            base.jwtSecret(),
            base.jwtExpirationHours(),
            base.corsAllowedOrigins(),
            base.challengesPath(),
            base.runnerJava26Image(),
            base.runnerMavenCacheVolume(),
            false,
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
            base.runnerPoolWarmOnStartup());
    RunnerWarmStateStore warmStateStore = mock(RunnerWarmStateStore.class);
    when(warmStateStore.runnerPoolStampByImage()).thenReturn(Map.of());
    var executor =
        new RunnerPoolWarmExecutor(
            properties,
            mock(LanguageRuntimeRepository.class),
            mock(LanguageRepository.class),
            mock(RunnerContainerPool.class),
            warmStateStore,
            JsonMapper.builder().build());
    var log = new StringBuilder();

    Map<String, String> stamp = executor.warm(false, List.of(), log::append);

    assertThat(stamp).isEmpty();
    assertThat(log).contains("Runner pool disabled");
  }

  @Test
  void infrastructureFailureDetectsRunnerOnlyFailures() {
    RunnerResult infra =
        new RunnerResult(
            RunnerStatus.FAILED.name(),
            List.of(new RunnerResult.TestOutcome("runner", "FAIL", "timeout", 0)),
            new RunnerResult.CoverageOutcome(0, 0),
            new RunnerResult.CompileOutcome(0, List.of()),
            null,
            null);
    RunnerResult testFail =
        new RunnerResult(
            RunnerStatus.FAILED.name(),
            List.of(new RunnerResult.TestOutcome("myTest", "FAIL", "assertion", 0)),
            new RunnerResult.CoverageOutcome(0, 0),
            new RunnerResult.CompileOutcome(0, List.of()),
            null,
            null);

    assertThat(RunnerPoolWarmExecutor.isInfrastructureFailure(infra)).isTrue();
    assertThat(RunnerPoolWarmExecutor.isInfrastructureFailure(testFail)).isFalse();
  }

  @Test
  void formatWarmResultDetailIncludesFirstFailureMessage() {
    RunnerResult result =
        new RunnerResult(
            RunnerStatus.FAILED.name(),
            List.of(
                new RunnerResult.TestOutcome(
                    "runner", "FAIL", "pytest failed: tests not found", 0)),
            new RunnerResult.CoverageOutcome(0, 0),
            new RunnerResult.CompileOutcome(0, List.of()),
            null,
            null);

    assertThat(RunnerPoolWarmExecutor.formatWarmResultDetail(result))
        .contains("pytest failed");
  }
}
