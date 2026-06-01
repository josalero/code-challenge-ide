package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.catalog.api.ChallengeTestPayload;
import com.codetraininglab.catalog.api.CreateChallengeRequest;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestRepository;
import com.codetraininglab.platform.persistence.ChallengePublicTestRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import java.util.Map;
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
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class ChallengePublisherTest {

  @TempDir Path tempDir;

  @Mock private ChallengeRepository challengeRepository;
  @Mock private ChallengePublicTestRepository publicTestRepository;
  @Mock private ChallengeHiddenTestRepository hiddenTestRepository;
  @Mock private LanguageRepository languageRepository;
  @Mock private LanguageRuntimeRepository runtimeRepository;

  private ChallengePublisher publisher;

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
            "ctl-runner-m2-cache",
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
            false);
    publisher =
        new ChallengePublisher(
            properties,
            challengeRepository,
            publicTestRepository,
            hiddenTestRepository,
            languageRepository,
            runtimeRepository,
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
  }

  @Test
  void createsChallengeOnDiskAndInDatabase() throws Exception {
    UUID languageId = UUID.randomUUID();
    when(challengeRepository.findBySlug("my-challenge")).thenReturn(Optional.empty());
    when(languageRepository.findByName("java"))
        .thenReturn(Optional.of(new LanguageEntity(languageId, "java", "Java")));
    when(runtimeRepository.findByLanguageIdAndVersion(languageId, "26"))
        .thenReturn(
            Optional.of(
                new LanguageRuntimeEntity(UUID.randomUUID(), languageId, "26", "runner:26", true)));

    var request = sampleRequest("my-challenge");
    var summary = publisher.create(request);

    assertThat(summary.slug()).isEqualTo("my-challenge");
    assertThat(summary.title()).isEqualTo("My Challenge");

    Path root = tempDir.resolve("my-challenge");
    assertThat(Files.exists(root.resolve("challenge.yml"))).isTrue();
    assertThat(Files.exists(root.resolve("starter/Solution.java"))).isTrue();
    assertThat(Files.exists(root.resolve("public/tests/MyPublicTest.java"))).isTrue();
    assertThat(Files.exists(root.resolve("hidden/tests/MyHiddenTest.java"))).isTrue();

    ArgumentCaptor<ChallengeEntity> captor = ArgumentCaptor.forClass(ChallengeEntity.class);
    verify(challengeRepository).save(captor.capture());
    assertThat(captor.getValue().getSource()).isEqualTo("user");
    verify(publicTestRepository).save(any());
    verify(hiddenTestRepository).save(any());
  }

  @Test
  void rejectsDuplicateSlug() {
    when(challengeRepository.findBySlug("dup"))
        .thenReturn(
            Optional.of(
                new ChallengeEntity(
                    UUID.randomUUID(),
                    "dup",
                    "T",
                    "d",
                    "s",
                    "{}",
                    "git",
                    "easy",
                    "java",
                    Instant.EPOCH,
                    Instant.EPOCH)));

    assertThatThrownBy(() -> publisher.create(sampleRequest("dup")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("already exists");
  }

  @Test
  void persistsPublicTestDescriptions() {
    UUID languageId = UUID.randomUUID();
    when(challengeRepository.findBySlug("described")).thenReturn(Optional.empty());
    when(languageRepository.findByName("java"))
        .thenReturn(Optional.of(new LanguageEntity(languageId, "java", "Java")));
    when(runtimeRepository.findByLanguageIdAndVersion(languageId, "26"))
        .thenReturn(
            Optional.of(
                new LanguageRuntimeEntity(UUID.randomUUID(), languageId, "26", "runner:26", true)));

    var request =
        new CreateChallengeRequest(
            "described",
            "Described",
            "Practice",
            "easy",
            "java",
            "26",
            "class Solution {}",
            80,
            List.of(new ChallengeTestPayload("Pub", "source", "Checks factorial")),
            List.of(new ChallengeTestPayload("Hid", "hidden source")));
    publisher.create(request);

    ArgumentCaptor<com.codetraininglab.platform.persistence.ChallengePublicTestEntity> captor =
        ArgumentCaptor.forClass(
            com.codetraininglab.platform.persistence.ChallengePublicTestEntity.class);
    verify(publicTestRepository).save(captor.capture());
    assertThat(captor.getValue().getDescription()).isEqualTo("Checks factorial");
  }

  @Test
  void writesPythonChallengeLayout() throws Exception {
    UUID languageId = UUID.randomUUID();
    when(challengeRepository.findBySlug("py-task")).thenReturn(Optional.empty());
    when(languageRepository.findByName("python"))
        .thenReturn(Optional.of(new LanguageEntity(languageId, "python", "Python")));
    when(runtimeRepository.findByLanguageIdAndVersion(languageId, "3.12"))
        .thenReturn(
            Optional.of(
                new LanguageRuntimeEntity(
                    UUID.randomUUID(), languageId, "3.12", "runner:py", true)));

    var request =
        new CreateChallengeRequest(
            "py-task",
            "Py",
            "Desc",
            "easy",
            "python",
            "3.12",
            "def solve(): pass",
            80,
            List.of(new ChallengeTestPayload("test_sample", "assert True")),
            List.of());
    publisher.create(request);

    Path root = tempDir.resolve("py-task");
    assertThat(Files.exists(root.resolve("starter/solution.py"))).isTrue();
    assertThat(Files.readString(root.resolve("challenge.yml"))).contains("starter_main_class: solution");
    assertThat(Files.list(root.resolve("public/tests")).anyMatch(p -> p.toString().endsWith(".py")))
        .isTrue();
  }

  private static CreateChallengeRequest sampleRequest(String slug) {
    return new CreateChallengeRequest(
        slug,
        "My Challenge",
        "Practice task",
        "easy",
        "java",
        "26",
        "package com.challenge;\n\npublic class Solution { }\n",
        80,
        List.of(new ChallengeTestPayload("MyPublic", "public test source")),
        List.of(new ChallengeTestPayload("MyHidden", "hidden test source")));
  }
}
