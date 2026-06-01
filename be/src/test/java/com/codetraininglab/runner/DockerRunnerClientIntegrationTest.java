package com.codetraininglab.integration.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import static org.mockito.Mockito.when;

import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestEntity;
import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class DockerRunnerClientIntegrationTest {

  @Mock private LanguageRuntimeRepository runtimeRepository;
  @Mock private LanguageRepository languageRepository;

  private static final String IMAGE = "code-challenge-ide-runner-java-26:itest";
  private static boolean dockerAvailable;
  private static boolean imageBuilt;

  @BeforeAll
  static void buildRunnerImage() throws Exception {
    dockerAvailable = Runtime.getRuntime().exec(new String[] {"docker", "info"}).waitFor(5, TimeUnit.SECONDS);
    assumeTrue(dockerAvailable, "Docker not available");

    Path repoRoot = repoRoot();
    Path dockerfile = repoRoot.resolve("runners/java/Dockerfile");
    Path context = repoRoot.resolve("runners/java");
    assumeTrue(Files.exists(dockerfile), "runner Dockerfile missing");

    Process build =
        Runtime.getRuntime()
            .exec(
                new String[] {
                  "docker",
                  "build",
                  "-t",
                  IMAGE,
                  "-f",
                  dockerfile.toString(),
                  context.toString()
                });
    boolean finished = build.waitFor(600, TimeUnit.SECONDS);
    imageBuilt = finished && build.exitValue() == 0;
    assumeTrue(imageBuilt, "Failed to build runner image");
  }

  @AfterAll
  static void cleanupPoolContainer() throws Exception {
    String containerName =
        DockerRunnerCommands.poolContainerName(IMAGE);
    Runtime.getRuntime().exec(new String[] {"docker", "rm", "-f", containerName}).waitFor(30, TimeUnit.SECONDS);
  }

  @Test
  void runsReverseStringChallengeInPooledContainer() throws Exception {
    var base = CtlPropertiesTestFixtures.defaults(repoRoot().resolve("challenges").toString());
    CtlProperties properties =
        new CtlProperties(
            base.registrationEnabled(),
            base.jwtSecret(),
            base.jwtExpirationHours(),
            base.corsAllowedOrigins(),
            base.challengesPath(),
            IMAGE,
            base.runnerMavenCacheVolume(),
            true,
            60,
            CtlPropertiesTestFixtures.TEST_LSP_IMAGES,
            base.lspIdleMinutes(),
            base.idempotencyTtlHours(),
            base.aiProvider(),
            base.openrouterApiKey(),
            base.openrouterModel(),
            base.ollamaBaseUrl(),
            base.ollamaModel(),
            true,
            base.lspEnabled(),
            false);
    RunnerContainerPool pool =
        new RunnerContainerPool(properties, JsonMapper.builder().build(), Clock.systemUTC());
    DockerRunnerClient client =
        new DockerRunnerClient(
            properties, JsonMapper.builder().build(), runtimeRepository, languageRepository, pool);

    String solution =
        Files.readString(
            repoRoot().resolve("challenges/reverse-string/starter/Solution.java"))
            .replace(
                "throw new UnsupportedOperationException(\"TODO\");",
                "return new StringBuilder(input).reverse().toString();");

    SubmissionEntity submission = submission(solution);
    UUID langId = UUID.randomUUID();
    when(runtimeRepository.findById(submission.getRuntimeId()))
        .thenReturn(
            Optional.of(
                new LanguageRuntimeEntity(
                    submission.getRuntimeId(), langId, "26", IMAGE, true)));
    when(languageRepository.findById(langId))
        .thenReturn(Optional.of(new LanguageEntity(langId, "java", "Java")));
    Path challengeDir = repoRoot().resolve("challenges/reverse-string");
    String hiddenSource =
        Files.readString(
            repoRoot()
                .resolve("challenges/reverse-string/hidden/tests/ReverseStringHiddenTest.java"));
    List<ChallengeHiddenTestEntity> hidden =
        List.of(
            new ChallengeHiddenTestEntity(
                UUID.randomUUID(), UUID.randomUUID(), "ReverseStringHiddenTest", hiddenSource, 0));

    RunnerResult result =
        client.execute(
            submission,
            "reverse-string",
            hidden,
            challengeDir,
            "code-challenge-ide-runner-java-26:itest");

    assertThat(result.status()).isEqualTo("COMPLETED");
    assertThat(result.tests()).isNotEmpty();
    assertThat(result.tests().stream().filter(t -> "FAIL".equals(t.status()))).isEmpty();
    assertThat(result.coverage().linePercent()).isGreaterThan(0);
  }

  private static Path repoRoot() {
    Path cwd = Path.of("").toAbsolutePath();
    return cwd.getFileName().toString().equals("be") ? cwd.getParent() : cwd;
  }

  private static SubmissionEntity submission(String code) {
    return new SubmissionEntity(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        SubmissionStatus.PENDING,
        code,
        null,
        null,
        Instant.EPOCH,
        Instant.EPOCH);
  }
}
