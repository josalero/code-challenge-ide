package com.codetraininglab.integration.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.WorkspaceLayout;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class DockerRunnerClientTest {

  @Mock private LanguageRuntimeRepository runtimeRepository;
  @Mock private LanguageRepository languageRepository;
  @Mock private RunnerContainerPool runnerContainerPool;

  @BeforeEach
  void disablePoolByDefault() {
    lenient().when(runnerContainerPool.isEnabled()).thenReturn(false);
  }

  private static CtlProperties properties(String mavenCacheVolume) {
    var base = CtlPropertiesTestFixtures.defaults();
    return new CtlProperties(
        base.registrationEnabled(),
        base.jwtSecret(),
        base.jwtExpirationHours(),
        base.corsAllowedOrigins(),
        base.challengesPath(),
        base.runnerJava26Image(),
        mavenCacheVolume,
        base.runnerPoolEnabled(),
        base.runnerPoolIdleMinutes(),
        CtlPropertiesTestFixtures.TEST_LSP_IMAGES,
        base.lspIdleMinutes(),
        base.idempotencyTtlHours(),
        base.aiProvider(),
        base.openrouterApiKey(),
        base.openrouterModel(),
        base.ollamaBaseUrl(),
        base.ollamaModel(),
        true,
        base.lspEnabled());
  }

  private DockerRunnerClient client(CtlProperties properties) {
    return new DockerRunnerClient(
        properties,
        JsonMapper.builder().build(),
        runtimeRepository,
        languageRepository,
        runnerContainerPool);
  }

  @Test
  void mountsSharedMavenCacheVolumeWhenConfigured() {
    var command =
        client(properties("ctl-runner-m2-cache"))
            .buildDockerRunCommand(
            Path.of("/challenges/reverse-string"),
            "code-challenge-ide-runner-java-26:local",
            RunnerJobPayload.RunnerLimits.defaults(),
            WorkspaceLayout.MAVEN.id());

    assertThat(command).contains("-v", "ctl-runner-m2-cache:/tmp/home/.m2:rw");
  }

  @Test
  void skipsMavenCacheMountForPythonRunner() {
    var command =
        client(properties("ctl-runner-m2-cache"))
            .buildDockerRunCommand(
            Path.of("/challenges/fizzbuzz-python"),
            "code-challenge-ide-runner-python-312:local",
            RunnerJobPayload.RunnerLimits.defaults(),
            WorkspaceLayout.PYTEST.id());

    assertThat(command).doesNotContain(":/tmp/home/.m2:rw");
  }

  @Test
  void skipsMavenCacheMountWhenVolumeNotConfigured() {
    var command =
        client(properties(""))
            .buildDockerRunCommand(
            Path.of("/challenges/reverse-string"),
            "code-challenge-ide-runner-java-26:local",
            RunnerJobPayload.RunnerLimits.defaults(),
            WorkspaceLayout.MAVEN.id());

    assertThat(command).doesNotContain(":/tmp/home/.m2:rw");
  }
}
