package com.codetraininglab.integration.runner;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.domain.WorkspaceLayout;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class DockerRunnerClientTest {

  @Mock private LanguageRuntimeRepository runtimeRepository;
  @Mock private LanguageRepository languageRepository;

  private static CtlProperties properties(String mavenCacheVolume) {
    return new CtlProperties(
        true,
        "test-jwt-secret-must-be-at-least-32-characters-long",
        24,
        "http://localhost:5173",
        "challenges",
        "runner",
        mavenCacheVolume,
        "lsp",
        5,
        24,
        "openrouter",
        "",
        "model",
        "http://localhost:11434",
        "ollama",
        true,
        false);
  }

  @Test
  void mountsSharedMavenCacheVolumeWhenConfigured() {
    DockerRunnerClient client =
        new DockerRunnerClient(
            properties("ctl-runner-m2-cache"),
            JsonMapper.builder().build(),
            runtimeRepository,
            languageRepository);
    var command =
        client.buildDockerRunCommand(
            Path.of("/challenges/reverse-string"),
            "code-challenge-ide-runner-java-26:local",
            RunnerJobPayload.RunnerLimits.defaults(),
            WorkspaceLayout.MAVEN.id());

    assertThat(command).contains("-v", "ctl-runner-m2-cache:/tmp/home/.m2:rw");
  }

  @Test
  void skipsMavenCacheMountForPythonRunner() {
    DockerRunnerClient client =
        new DockerRunnerClient(
            properties("ctl-runner-m2-cache"),
            JsonMapper.builder().build(),
            runtimeRepository,
            languageRepository);
    var command =
        client.buildDockerRunCommand(
            Path.of("/challenges/fizzbuzz-python"),
            "code-challenge-ide-runner-python-312:local",
            RunnerJobPayload.RunnerLimits.defaults(),
            WorkspaceLayout.PYTEST.id());

    assertThat(command).doesNotContain(":/tmp/home/.m2:rw");
  }

  @Test
  void skipsMavenCacheMountWhenVolumeNotConfigured() {
    DockerRunnerClient client =
        new DockerRunnerClient(
            properties(""),
            JsonMapper.builder().build(),
            runtimeRepository,
            languageRepository);
    var command =
        client.buildDockerRunCommand(
            Path.of("/challenges/reverse-string"),
            "code-challenge-ide-runner-java-26:local",
            RunnerJobPayload.RunnerLimits.defaults(),
            WorkspaceLayout.MAVEN.id());

    assertThat(command).doesNotContain(":/tmp/home/.m2:rw");
  }
}
