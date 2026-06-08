package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.catalog.api.ChallengeTestPayload;
import com.codetraininglab.catalog.api.CreateChallengeRequest;
import com.codetraininglab.catalog.api.ValidateChallengeRequest;
import com.codetraininglab.integration.runner.RunnerClient;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChallengeDraftValidationServiceTest {

  @TempDir Path tempDir;

  @Mock private ChallengePublisher challengePublisher;
  @Mock private LanguageRepository languageRepository;
  @Mock private LanguageRuntimeRepository runtimeRepository;
  @Mock private RunnerClient runnerClient;

  private ChallengeDraftValidationService service;

  @BeforeEach
  void setUp() {
    CtlProperties properties =
        new CtlProperties(
            true,
            "test-jwt-secret-must-be-at-least-32-characters-long",
            24,
            "http://localhost:5173",
            tempDir.toString(),
            "runner:latest",
            "code-challenge-ide-pro-runner-m2-cache",
            true,
            60,
            java.util.Map.of("java", "lsp:latest"),
            5,
            24,
            "openrouter",
            "",
            "model",
            "http://localhost:11434",
            "model",
            false,
            false,
            false,
            0);
    service =
        new ChallengeDraftValidationService(
            properties,
            challengePublisher,
            languageRepository,
            runtimeRepository,
            runnerClient,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
  }

  @Test
  void validatesDraftWithRunnerAndCleansWorkspace() throws Exception {
    UUID languageId = UUID.randomUUID();
    UUID runtimeId = UUID.randomUUID();
    when(languageRepository.findByName("java"))
        .thenReturn(Optional.of(new LanguageEntity(languageId, "java", "Java")));
    when(runtimeRepository.findByLanguageIdAndVersion(languageId, "26"))
        .thenReturn(
            Optional.of(new LanguageRuntimeEntity(runtimeId, languageId, "26", "runner:26", true)));
    doAnswer(
            invocation -> {
              Path challengeDir = invocation.getArgument(0);
              Files.createDirectories(challengeDir.resolve("public/tests"));
              Files.writeString(challengeDir.resolve("public/tests/PublicTest.java"), "test");
              return null;
            })
        .when(challengePublisher)
        .writeChallengeTree(
            any(Path.class),
            any(CreateChallengeRequest.class),
            any(String.class),
            eq("java"),
            anyInt());
    when(runnerClient.execute(any(), any(), any(), any(), eq("runner:26")))
        .thenReturn(
            new RunnerResult(
                "COMPLETED",
                List.of(new RunnerResult.TestOutcome("sample", "FAIL", "assertion", 1)),
                new RunnerResult.CoverageOutcome(10, 0),
                new RunnerResult.CompileOutcome(
                    1,
                    List.of(
                        new RunnerResult.CompileOutcome.CompileMessage(
                            "/workspace/Solution.java", 3, "unchecked"))),
                null,
                new RunnerResult.LogsOutcome("out", "err")));

    var response =
        service.validate(
            new ValidateChallengeRequest(
                "my-draft",
                "java",
                "26",
                "package com.challenge; public class Solution {}",
                List.of(new ChallengeTestPayload("Public", "public source")),
                List.of(new ChallengeTestPayload("Hidden", "hidden source"))));

    assertThat(response.compiled()).isTrue();
    assertThat(response.passed()).isFalse();
    assertThat(response.compile().warnings()).isEqualTo(1);

    ArgumentCaptor<SubmissionEntity> submissionCaptor =
        ArgumentCaptor.forClass(SubmissionEntity.class);
    ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
    verify(runnerClient)
        .execute(submissionCaptor.capture(), any(), any(), pathCaptor.capture(), eq("runner:26"));
    assertThat(submissionCaptor.getValue().getRuntimeId()).isEqualTo(runtimeId);
    assertThat(pathCaptor.getValue()).doesNotExist();
  }
}
