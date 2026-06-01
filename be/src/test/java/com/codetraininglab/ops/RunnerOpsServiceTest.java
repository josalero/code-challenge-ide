package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class RunnerOpsServiceTest {

  @Mock private LanguageRuntimeRepository runtimeRepository;
  @Mock private LanguageRepository languageRepository;
  @Mock private Environment environment;
  @Mock private ObjectProvider<RunnerPoolWarmExecutor> runnerPoolWarmExecutor;

  private final JsonMapper jsonMapper = JsonMapper.builder().build();
  private RunnerOpsService service;

  @BeforeEach
  void setUp() {
    CtlProperties properties =
        new CtlProperties(
            false,
            "test-secret-test-secret-test-secret-test",
            24,
            "http://localhost:5173",
            "challenges",
            "code-challenge-ide-runner-java-26:test",
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
            false,
            false,
            false);
    lenient().when(runtimeRepository.findAllOrdered()).thenReturn(List.of());
    lenient().when(languageRepository.findAll()).thenReturn(List.of());
    service =
        new RunnerOpsService(
            properties,
            environment,
            runtimeRepository,
            languageRepository,
            jsonMapper,
            runnerPoolWarmExecutor);
  }

  @Test
  void statusReportsDockerDisabled() {
    var status = service.status();
    assertThat(status.dockerEnabled()).isFalse();
  }

  @Test
  void warmMavenRejectedWhenDockerDisabled() {
    assertThatThrownBy(() -> service.startMavenWarm(false))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Docker integration is disabled");
  }

  @Test
  void warmRunnerPoolRejectedWhenDockerDisabled() {
    assertThatThrownBy(() -> service.startRunnerWarm(false, List.of()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Docker integration is disabled");
  }

  @Test
  void warmRunnerPoolStartsJobWhenDockerEnabled() {
    CtlProperties properties =
        new CtlProperties(
            false,
            "test-secret-test-secret-test-secret-test",
            24,
            "http://localhost:5173",
            "challenges",
            "code-challenge-ide-runner-java-26:test",
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
            true,
            false);
    RunnerPoolWarmExecutor executor = org.mockito.Mockito.mock(RunnerPoolWarmExecutor.class);
    when(runnerPoolWarmExecutor.getIfAvailable()).thenReturn(executor);
    RunnerOpsService dockerEnabledService =
        new RunnerOpsService(
            properties,
            environment,
            runtimeRepository,
            languageRepository,
            jsonMapper,
            runnerPoolWarmExecutor);

    var job = dockerEnabledService.startRunnerWarm(false, List.of("java"));

    assertThat(job.type()).isEqualTo("RUNNER_POOL_WARM");
    assertThat(job.status()).isEqualTo("RUNNING");
  }

  @Test
  void warmRunnerPoolRejectsUnknownLanguage() {
    CtlProperties properties =
        new CtlProperties(
            false,
            "test-secret-test-secret-test-secret-test",
            24,
            "http://localhost:5173",
            "challenges",
            "code-challenge-ide-runner-java-26:test",
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
            true,
            false);
    when(runnerPoolWarmExecutor.getIfAvailable())
        .thenReturn(org.mockito.Mockito.mock(RunnerPoolWarmExecutor.class));
    RunnerOpsService dockerEnabledService =
        new RunnerOpsService(
            properties,
            environment,
            runtimeRepository,
            languageRepository,
            jsonMapper,
            runnerPoolWarmExecutor);

    assertThatThrownBy(() -> dockerEnabledService.startRunnerWarm(false, List.of("kotlin")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Unsupported runner warm language");
  }

  @Test
  void warmLspRejectsUnknownLabel() {
    CtlProperties properties =
        new CtlProperties(
            false,
            "test-secret-test-secret-test-secret-test",
            24,
            "http://localhost:5173",
            "challenges",
            "code-challenge-ide-runner-java-26:test",
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
            true,
            false);
    RunnerOpsService dockerEnabledService =
        new RunnerOpsService(
            properties,
            environment,
            runtimeRepository,
            languageRepository,
            jsonMapper,
            runnerPoolWarmExecutor);
    when(environment.getProperty("ctl.repo-root", "")).thenReturn(repoRoot().toString());

    assertThatThrownBy(() -> dockerEnabledService.startLspWarm(false, List.of("kotlin")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Unsupported LSP warm label");
  }

  private static Path repoRoot() {
    Path current = Path.of("").toAbsolutePath();
    for (Path candidate = current; candidate != null; candidate = candidate.getParent()) {
      if (Files.isRegularFile(candidate.resolve("scripts/lsp_warm.py"))) {
        return candidate;
      }
    }
    return current;
  }
}
