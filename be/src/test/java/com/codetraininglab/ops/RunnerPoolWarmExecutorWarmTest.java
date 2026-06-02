package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class RunnerPoolWarmExecutorWarmTest {

  @TempDir Path tempDir;

  @Mock private LanguageRuntimeRepository runtimeRepository;
  @Mock private LanguageRepository languageRepository;
  @Mock private RunnerContainerPool runnerContainerPool;
  @Mock private RunnerWarmStateStore warmStateStore;

  private RunnerPoolWarmExecutor executor;
  private Path repoRoot;
  private Path challengesRoot;

  @BeforeEach
  void setUp() throws Exception {
    repoRoot = tempDir.resolve("repo");
    challengesRoot = repoRoot.resolve("challenges");
    Files.createDirectories(challengesRoot.resolve("reverse-string/starter"));
    Files.writeString(
        challengesRoot.resolve("reverse-string/starter/Solution.java"),
        """
        package com.challenge;

        public class Solution {
            public static String reverse(String input) {
                throw new UnsupportedOperationException("TODO");
            }
        }
        """);
    CtlProperties properties =
        new CtlProperties(
            true,
            CtlPropertiesTestFixtures.defaults().jwtSecret(),
            24,
            "http://localhost:5173",
            challengesRoot.toString(),
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
    when(warmStateStore.runnerPoolStampByImage()).thenReturn(new java.util.LinkedHashMap<>());
    executor =
        new RunnerPoolWarmExecutor(
            properties,
            runtimeRepository,
            languageRepository,
            runnerContainerPool,
            warmStateStore,
            JsonMapper.builder().build());
  }

  @Test
  void warmRunsSmokeSubmissionForActiveJavaRuntime() throws Exception {
    UUID languageId = UUID.randomUUID();
    LanguageEntity language = new LanguageEntity(languageId, "java", "Java");
    LanguageRuntimeEntity runtime =
        new LanguageRuntimeEntity(
            UUID.randomUUID(),
            languageId,
            "26",
            "code-challenge-ide-runner-java-26:local",
            true);
    when(languageRepository.findAll()).thenReturn(List.of(language));
    when(runtimeRepository.findAllOrdered()).thenReturn(List.of(runtime));
    when(runnerContainerPool.execute(
            eq("code-challenge-ide-runner-java-26:local"),
            any(),
            eq("maven"),
            any(),
            any(RunnerJobPayload.RunnerLimits.class)))
        .thenReturn(
            new RunnerResult(
                RunnerStatus.COMPLETED.name(),
                List.of(new RunnerResult.TestOutcome("t", "PASS", null, 1)),
                new RunnerResult.CoverageOutcome(0, 0),
                new RunnerResult.CompileOutcome(0, List.of()),
                null,
                null));

    var log = new StringBuilder();
    executor.warm(true, List.of("java"), log::append);

    verify(warmStateStore).recordRunnerPoolWarm(eq("code-challenge-ide-runner-java-26:local"), any());

    ArgumentCaptor<String> jobCaptor = ArgumentCaptor.forClass(String.class);
    verify(runnerContainerPool)
        .execute(
            eq("code-challenge-ide-runner-java-26:local"),
            eq(challengesRoot.resolve("reverse-string")),
            eq("maven"),
            jobCaptor.capture(),
            any(RunnerJobPayload.RunnerLimits.class));
    assertThat(jobCaptor.getValue()).contains("reverse-string");
    assertThat(jobCaptor.getValue()).contains("StringBuilder");
    assertThat(jobCaptor.getValue()).doesNotContain("UnsupportedOperationException");
    assertThat(jobCaptor.getValue()).contains("\"hidden_tests\":[]");
    assertThat(log.toString()).contains("Smoke warm java 26");
  }

  @Test
  void warmRecordsColdWhenDockerImageMissing() {
    UUID languageId = UUID.randomUUID();
    LanguageEntity language = new LanguageEntity(languageId, "python", "Python");
    LanguageRuntimeEntity runtime =
        new LanguageRuntimeEntity(
            UUID.randomUUID(),
            languageId,
            "3.12",
            "code-challenge-ide-runner-missing-image:local",
            true);
    when(languageRepository.findAll()).thenReturn(List.of(language));
    when(runtimeRepository.findAllOrdered()).thenReturn(List.of(runtime));

    var log = new StringBuilder();
    executor.warm(false, List.of("python"), log::append);

    verify(warmStateStore).recordRunnerPoolCold("code-challenge-ide-runner-missing-image:local");
    assertThat(log.toString()).contains("image missing");
  }
}
