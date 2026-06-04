package com.codetraininglab.operations.application;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class RunnerPoolWarmStartupTest {

  @Mock private RunnerOpsService runnerOpsService;

  @Test
  void skipsWarmWhenDockerDisabled() {
    var properties = properties(false, true);
    var startup = new RunnerPoolWarmStartup(properties, runnerOpsService);

    startup.run(new DefaultApplicationArguments(new String[0]));

    verify(runnerOpsService, never()).startRunnerWarm(false, java.util.List.of());
  }

  @Test
  void startsRunnerWarmWhenDockerEnabled() {
    var properties = properties(true, true);
    var startup = new RunnerPoolWarmStartup(properties, runnerOpsService);

    startup.run(new DefaultApplicationArguments(new String[0]));

    verify(runnerOpsService).startRunnerWarm(false, java.util.List.of());
  }

  private static CtlProperties properties(boolean dockerEnabled, boolean warmOnStartup) {
    var base = CtlPropertiesTestFixtures.defaults();
    return new CtlProperties(
        base.registrationEnabled(),
        base.jwtSecret(),
        base.jwtExpirationHours(),
        base.corsAllowedOrigins(),
        base.challengesPath(),
        base.runnerJava26Image(),
        base.runnerMavenCacheVolume(),
        base.runnerPoolEnabled(),
        base.runnerPoolIdleMinutes(),
        base.lspImages(),
        base.lspIdleMinutes(),
        base.idempotencyTtlHours(),
        base.aiProvider(),
        base.openrouterApiKey(),
        base.openrouterModel(),
        base.ollamaBaseUrl(),
        base.ollamaModel(),
        dockerEnabled,
        base.lspEnabled(),
        warmOnStartup,
        base.userMaxStartedChallenges());
  }
}
