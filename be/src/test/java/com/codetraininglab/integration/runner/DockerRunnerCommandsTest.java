package com.codetraininglab.integration.runner;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.domain.WorkspaceLayout;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import com.codetraininglab.integration.runner.RunnerJobPayload.RunnerLimits;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DockerRunnerCommandsTest {

  @Test
  void poolContainerNameSanitizesImageReference() {
    assertThat(DockerRunnerCommands.poolContainerName("code-challenge-ide-runner-java-26:local"))
        .isEqualTo("ctl-runner-pool-code-challenge-ide-runner-java-26-local");
  }

  @Test
  void poolCreateCommandUsesTmpfsChallengeMountAndPooledEnv() {
    CtlProperties properties = CtlPropertiesTestFixtures.defaults();
    var command =
        DockerRunnerCommands.buildPoolCreateCommand(
            "ctl-runner-pool-java",
            "code-challenge-ide-runner-java-26:local",
            RunnerLimits.defaults(),
            WorkspaceLayout.MAVEN.id(),
            properties);

    assertThat(command)
        .contains("--name", "ctl-runner-pool-java")
        .contains("--label", DockerRunnerCommands.POOL_LABEL)
        .contains("-e", "CTL_RUNNER_POOLED=1")
        .contains("--cap-drop", "ALL")
        .contains("--cap-add", "FOWNER")
        .contains("--security-opt", "no-new-privileges:true")
        .contains("--ipc", "none")
        .contains("--entrypoint", "sleep")
        .contains("infinity")
        .doesNotContain("--read-only");
  }

  @Test
  void ephemeralRunCommandStillMountsChallengeReadOnly() {
    CtlProperties properties = CtlPropertiesTestFixtures.defaults();
    var command =
        DockerRunnerCommands.buildEphemeralRunCommand(
            Path.of("/tmp/challenges/reverse-string"),
            "code-challenge-ide-runner-java-26:local",
            RunnerLimits.defaults(),
            WorkspaceLayout.MAVEN.id(),
            properties);

    assertThat(command)
        .contains("--rm", "-i")
        .contains("--cap-drop", "ALL")
        .contains("--cap-add", "FOWNER")
        .contains("--security-opt", "no-new-privileges:true")
        .contains("--ipc", "none")
        .contains("/tmp/challenges/reverse-string:/challenge:ro");
  }
}
