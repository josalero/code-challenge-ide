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
import org.springframework.core.env.Environment;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class RunnerOpsServiceTest {

  @Mock private LanguageRuntimeRepository runtimeRepository;
  @Mock private LanguageRepository languageRepository;
  @Mock private Environment environment;

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
            false);
    lenient().when(runtimeRepository.findAllOrdered()).thenReturn(List.of());
    lenient().when(languageRepository.findAll()).thenReturn(List.of());
    service =
        new RunnerOpsService(
            properties, environment, runtimeRepository, languageRepository, jsonMapper);
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
            true);
    RunnerOpsService dockerEnabledService =
        new RunnerOpsService(
            properties, environment, runtimeRepository, languageRepository, jsonMapper);
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
