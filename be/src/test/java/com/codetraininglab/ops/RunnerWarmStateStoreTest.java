package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.operations.api.RunnerImageStatusResponse;
import com.codetraininglab.platform.persistence.LspWarmStateEntity;
import com.codetraininglab.platform.persistence.LspWarmStateRepository;
import com.codetraininglab.platform.persistence.OpsPlatformStateEntity;
import com.codetraininglab.platform.persistence.OpsPlatformStateRepository;
import com.codetraininglab.platform.persistence.RunnerPoolWarmStateEntity;
import com.codetraininglab.platform.persistence.RunnerPoolWarmStateRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class RunnerWarmStateStoreTest {

  private static final Instant FIXED_NOW = Instant.parse("2026-06-01T12:00:00Z");

  @TempDir Path tempDir;

  @Mock private RunnerPoolWarmStateRepository runnerPoolRepository;
  @Mock private LspWarmStateRepository lspRepository;
  @Mock private OpsPlatformStateRepository platformStateRepository;
  @Mock private Environment environment;

  private RunnerWarmStateStore store;

  @BeforeEach
  void setUp() {
    store =
        new RunnerWarmStateStore(
            runnerPoolRepository,
            lspRepository,
            platformStateRepository,
            environment,
            JsonMapper.builder().build(),
            Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
  }

  @Test
  void parseLspScopeKeySplitsLabelAndImage() {
    assertThat(RunnerWarmStateStore.parseLspScopeKey("java:code-challenge-ide-lsp-java:local"))
        .isEqualTo(
            new RunnerWarmStateStore.LspScope(
                "java", "code-challenge-ide-lsp-java:local"));
  }

  @Test
  void parseLspScopeKeyRejectsInvalidKeys() {
    assertThat(RunnerWarmStateStore.parseLspScopeKey("nocolon")).isNull();
    assertThat(RunnerWarmStateStore.parseLspScopeKey(":only-image")).isNull();
    assertThat(RunnerWarmStateStore.parseLspScopeKey("")).isNull();
  }

  @Test
  void syncRunnerPoolFromStatusesPersistsColdRows() {
    when(runnerPoolRepository.findById("code-challenge-ide-runner-python:local"))
        .thenReturn(Optional.empty());

    store.syncRunnerPoolFromStatuses(
        List.of(
            new RunnerImageStatusResponse(
                "python 3.12", "code-challenge-ide-runner-python:local", true, "sha256:cold", false)));

    ArgumentCaptor<RunnerPoolWarmStateEntity> saved = ArgumentCaptor.forClass(RunnerPoolWarmStateEntity.class);
    verify(runnerPoolRepository).save(saved.capture());
    assertThat(saved.getValue().getDockerImage()).isEqualTo("code-challenge-ide-runner-python:local");
    assertThat(saved.getValue().isWarmed()).isFalse();
    assertThat(saved.getValue().getImageId()).isEqualTo("sha256:cold");
    assertThat(saved.getValue().getWarmedAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  void runnerPoolStampByImageExcludesColdRows() {
    RunnerPoolWarmStateEntity cold =
        new RunnerPoolWarmStateEntity("cold-image:local", "sha256:old", false, FIXED_NOW);
    RunnerPoolWarmStateEntity warm =
        new RunnerPoolWarmStateEntity("warm-image:local", "sha256:warm", true, FIXED_NOW);
    when(runnerPoolRepository.findAll()).thenReturn(List.of(cold, warm));

    assertThat(store.runnerPoolStampByImage())
        .containsExactly(java.util.Map.entry("warm-image:local", "sha256:warm"));
  }

  @Test
  void syncRunnerPoolFromStatusesPreservesWarmTimestampWhenUnchanged() {
    Instant previousWarmUp = Instant.parse("2026-05-01T08:30:00Z");
    RunnerPoolWarmStateEntity existing =
        new RunnerPoolWarmStateEntity("warm-image:local", "sha256:same", true, previousWarmUp);
    when(runnerPoolRepository.findById("warm-image:local")).thenReturn(Optional.of(existing));

    store.syncRunnerPoolFromStatuses(
        List.of(
            new RunnerImageStatusResponse(
                "java 26", "warm-image:local", true, "sha256:same", true)));

    assertThat(existing.getWarmedAt()).isEqualTo(previousWarmUp);
  }

  @Test
  void recordLastWarmUpAtPersistsPlatformTimestamp() {
    when(platformStateRepository.findById(OpsPlatformStateEntity.DEFAULT_ID))
        .thenReturn(Optional.empty());

    store.recordLastWarmUpAt(FIXED_NOW);

    ArgumentCaptor<OpsPlatformStateEntity> saved =
        ArgumentCaptor.forClass(OpsPlatformStateEntity.class);
    verify(platformStateRepository).save(saved.capture());
    assertThat(saved.getValue().getLastWarmUpAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  void lastWarmUpAtReadsPlatformState() {
    when(platformStateRepository.findById(OpsPlatformStateEntity.DEFAULT_ID))
        .thenReturn(
            Optional.of(
                new OpsPlatformStateEntity(
                    OpsPlatformStateEntity.DEFAULT_ID, FIXED_NOW)));

    assertThat(store.lastWarmUpAt()).contains(FIXED_NOW);
  }

  @Test
  void syncRunnerPoolFromStatusesUpdatesExistingWarmRow() {
    RunnerPoolWarmStateEntity existing =
        new RunnerPoolWarmStateEntity("warm-image:local", "sha256:old", false, FIXED_NOW);
    when(runnerPoolRepository.findById("warm-image:local")).thenReturn(Optional.of(existing));

    store.syncRunnerPoolFromStatuses(
        List.of(
            new RunnerImageStatusResponse(
                "java 26", "warm-image:local", true, "sha256:new", true)));

    assertThat(existing.isWarmed()).isTrue();
    assertThat(existing.getImageId()).isEqualTo("sha256:new");
    assertThat(existing.getWarmedAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  void syncRunnerPoolFromStatusesSkipsBlankImage() {
    store.syncRunnerPoolFromStatuses(
        List.of(new RunnerImageStatusResponse("java 26", "  ", true, "sha256:x", false)));

    verify(runnerPoolRepository, org.mockito.Mockito.never())
        .save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void recordRunnerPoolWarmInsertsNewRow() {
    when(runnerPoolRepository.findById("img:local")).thenReturn(Optional.empty());

    store.recordRunnerPoolWarm("img:local", "sha256:warm");

    ArgumentCaptor<RunnerPoolWarmStateEntity> saved = ArgumentCaptor.forClass(RunnerPoolWarmStateEntity.class);
    verify(runnerPoolRepository).save(saved.capture());
    assertThat(saved.getValue().isWarmed()).isTrue();
    assertThat(saved.getValue().getImageId()).isEqualTo("sha256:warm");
  }

  @Test
  void recordRunnerPoolColdPersistsNotWarmed() {
    when(runnerPoolRepository.findById("missing:local")).thenReturn(Optional.empty());

    store.recordRunnerPoolCold("missing:local");

    ArgumentCaptor<RunnerPoolWarmStateEntity> saved = ArgumentCaptor.forClass(RunnerPoolWarmStateEntity.class);
    verify(runnerPoolRepository).save(saved.capture());
    assertThat(saved.getValue().isWarmed()).isFalse();
    assertThat(saved.getValue().getImageId()).isNull();
  }

  @Test
  void syncLspFromStatusesPersistsWarmAndColdRows() {
    when(lspRepository.findById(
            new LspWarmStateEntity.LspWarmStateId("java", "lsp-java:local")))
        .thenReturn(Optional.empty());
    LspWarmStateEntity existing =
        new LspWarmStateEntity("python", "lsp-python:local", "sha256:old", true, FIXED_NOW);
    when(lspRepository.findById(
            new LspWarmStateEntity.LspWarmStateId("python", "lsp-python:local")))
        .thenReturn(Optional.of(existing));

    store.syncLspFromStatuses(
        List.of(
            new RunnerImageStatusResponse("java", "lsp-java:local", true, "sha256:warm", true),
            new RunnerImageStatusResponse("python", "lsp-python:local", true, "sha256:cold", false)));

    ArgumentCaptor<LspWarmStateEntity> saved = ArgumentCaptor.forClass(LspWarmStateEntity.class);
    verify(lspRepository).save(saved.capture());
    assertThat(saved.getValue().getId().getLabel()).isEqualTo("java");
    assertThat(saved.getValue().isWarmed()).isTrue();
    assertThat(existing.isWarmed()).isFalse();
    assertThat(existing.getImageId()).isEqualTo("sha256:cold");
  }

  @Test
  void lspStampByScopeKeyIncludesOnlyWarmRows() {
    LspWarmStateEntity warm =
        new LspWarmStateEntity("java", "lsp-java:local", "sha256:warm", true, FIXED_NOW);
    LspWarmStateEntity cold =
        new LspWarmStateEntity("python", "lsp-python:local", "sha256:old", false, FIXED_NOW);
    when(lspRepository.findAll()).thenReturn(List.of(warm, cold));

    assertThat(store.lspStampByScopeKey())
        .containsExactly(java.util.Map.entry("java:lsp-java:local", "sha256:warm"));
  }

  @Test
  void importLspStampFromFileImportsValidScopes() throws Exception {
    Path stampFile = tempDir.resolve(".ctl-lsp-warm-stamp");
    Files.writeString(
        stampFile,
        """
        {"java:lsp-java:local":"sha256:1","invalid":"sha256:2"}
        """);
    when(lspRepository.findById(
            new LspWarmStateEntity.LspWarmStateId("java", "lsp-java:local")))
        .thenReturn(Optional.empty());

    store.importLspStampFromFile(stampFile);

    ArgumentCaptor<LspWarmStateEntity> saved = ArgumentCaptor.forClass(LspWarmStateEntity.class);
    verify(lspRepository).save(saved.capture());
    assertThat(saved.getValue().getImageId()).isEqualTo("sha256:1");
    assertThat(saved.getValue().isWarmed()).isTrue();
  }

  @Test
  void importLspStampFromFileIgnoresMissingFile() throws Exception {
    store.importLspStampFromFile(tempDir.resolve("missing.json"));

    verify(lspRepository, org.mockito.Mockito.never())
        .save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void migrateLegacyStampFilesImportsPoolAndLspStamps() throws Exception {
    when(environment.getProperty("ctl.ops-data-dir", "")).thenReturn(tempDir.toString());
    Path poolFile = tempDir.resolve(".ctl-runner-pool-warm-stamp");
    Path lspFile = tempDir.resolve(".ctl-lsp-warm-stamp");
    Files.writeString(poolFile, "{\"runner-java:local\":\"sha256:pool\"}");
    Files.writeString(lspFile, "{\"go:lsp-go:local\":\"sha256:lsp\"}");
    when(runnerPoolRepository.count()).thenReturn(0L);
    when(lspRepository.count()).thenReturn(0L);
    when(runnerPoolRepository.findById("runner-java:local")).thenReturn(Optional.empty());
    when(lspRepository.findById(new LspWarmStateEntity.LspWarmStateId("go", "lsp-go:local")))
        .thenReturn(Optional.empty());

    store.migrateLegacyStampFiles();

    verify(runnerPoolRepository).save(org.mockito.ArgumentMatchers.any());
    verify(lspRepository).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void migrateLegacyStampFilesSkipsWhenDatabaseAlreadyPopulated() throws Exception {
    when(environment.getProperty("ctl.ops-data-dir", "")).thenReturn(tempDir.toString());
    Files.writeString(
        tempDir.resolve(".ctl-runner-pool-warm-stamp"), "{\"runner-java:local\":\"sha256:pool\"}");
    when(runnerPoolRepository.count()).thenReturn(1L);
    when(lspRepository.count()).thenReturn(1L);

    store.migrateLegacyStampFiles();

    verify(runnerPoolRepository, org.mockito.Mockito.never())
        .save(org.mockito.ArgumentMatchers.any());
    verify(lspRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
  }
}
