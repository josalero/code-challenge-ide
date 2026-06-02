package com.codetraininglab.operations.application;

import com.codetraininglab.operations.api.RunnerImageStatusResponse;
import com.codetraininglab.platform.persistence.LspWarmStateEntity;
import com.codetraininglab.platform.persistence.LspWarmStateRepository;
import com.codetraininglab.platform.persistence.RunnerPoolWarmStateEntity;
import com.codetraininglab.platform.persistence.RunnerPoolWarmStateRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/** Durable warm inventory in Postgres (replaces JSON stamp files for reads/writes). */
@Service
public class RunnerWarmStateStore {

  private static final Logger log = LoggerFactory.getLogger(RunnerWarmStateStore.class);

  private final RunnerPoolWarmStateRepository runnerPoolRepository;
  private final LspWarmStateRepository lspRepository;
  private final Environment environment;
  private final JsonMapper jsonMapper;
  private final Clock clock;

  public RunnerWarmStateStore(
      RunnerPoolWarmStateRepository runnerPoolRepository,
      LspWarmStateRepository lspRepository,
      Environment environment,
      JsonMapper jsonMapper,
      Clock clock) {
    this.runnerPoolRepository = runnerPoolRepository;
    this.lspRepository = lspRepository;
    this.environment = environment;
    this.jsonMapper = jsonMapper;
    this.clock = clock;
  }

  @PostConstruct
  void migrateLegacyStampFilesIfNeeded() {
    try {
      migrateLegacyStampFiles();
    } catch (Exception ex) {
      log.warn("Could not migrate legacy warm stamp files into Postgres: {}", ex.getMessage());
    }
  }

  @Transactional(readOnly = true)
  public Map<String, String> runnerPoolStampByImage() {
    Map<String, String> stamp = new LinkedHashMap<>();
    for (RunnerPoolWarmStateEntity row : runnerPoolRepository.findAll()) {
      if (row.isWarmed() && row.getImageId() != null) {
        stamp.put(row.getDockerImage(), row.getImageId());
      }
    }
    return stamp;
  }

  @Transactional(readOnly = true)
  public Map<String, String> lspStampByScopeKey() {
    Map<String, String> stamp = new LinkedHashMap<>();
    for (LspWarmStateEntity row : lspRepository.findAll()) {
      if (!row.isWarmed() || row.getImageId() == null) {
        continue;
      }
      LspWarmStateEntity.LspWarmStateId id = row.getId();
      stamp.put(id.getLabel() + ":" + id.getDockerImage(), row.getImageId());
    }
    return stamp;
  }

  @Transactional
  public void syncRunnerPoolFromStatuses(List<RunnerImageStatusResponse> images) {
    Instant now = clock.instant();
    for (RunnerImageStatusResponse image : images) {
      if (image.image() == null || image.image().isBlank()) {
        continue;
      }
      boolean warmed = Boolean.TRUE.equals(image.warmed());
      upsertRunnerPoolRow(image.image(), image.imageId(), warmed, now);
    }
  }

  @Transactional
  public void syncLspFromStatuses(List<RunnerImageStatusResponse> images) {
    Instant now = clock.instant();
    for (RunnerImageStatusResponse image : images) {
      if (image.label() == null
          || image.label().isBlank()
          || image.image() == null
          || image.image().isBlank()) {
        continue;
      }
      boolean warmed = Boolean.TRUE.equals(image.warmed());
      upsertLspRow(image.label(), image.image(), image.imageId(), warmed, now);
    }
  }

  @Transactional
  public void recordRunnerPoolWarm(String dockerImage, String imageId) {
    upsertRunnerPoolRow(dockerImage, imageId, true, clock.instant());
  }

  @Transactional
  public void recordRunnerPoolCold(String dockerImage) {
    upsertRunnerPoolRow(dockerImage, null, false, clock.instant());
  }

  @Transactional
  public void importLspStampFromFile(Path stampFile) throws IOException {
    Map<String, String> stamp = readJsonStamp(stampFile);
    if (stamp.isEmpty()) {
      return;
    }
    Instant now = clock.instant();
    for (Map.Entry<String, String> entry : stamp.entrySet()) {
      LspScope scope = parseLspScopeKey(entry.getKey());
      if (scope == null) {
        continue;
      }
      upsertLspRow(scope.label(), scope.dockerImage(), entry.getValue(), true, now);
    }
  }

  @Transactional
  public void migrateLegacyStampFiles() throws IOException {
    Path poolFile = RunnerOpsPaths.poolWarmStampFile(environment);
    Path lspFile = RunnerOpsPaths.lspWarmStampFile(environment);

    if (runnerPoolRepository.count() == 0 && Files.isRegularFile(poolFile)) {
      Map<String, String> legacy = readJsonStamp(poolFile);
      if (!legacy.isEmpty()) {
        Instant now = clock.instant();
        for (Map.Entry<String, String> entry : legacy.entrySet()) {
          upsertRunnerPoolRow(entry.getKey(), entry.getValue(), true, now);
        }
        log.info("Migrated {} runner pool warm entries from {}", legacy.size(), poolFile);
      }
    }

    if (lspRepository.count() == 0 && Files.isRegularFile(lspFile)) {
      Map<String, String> legacy = readJsonStamp(lspFile);
      if (!legacy.isEmpty()) {
        importLspStampFromFile(lspFile);
        log.info("Migrated {} LSP warm entries from {}", legacy.size(), lspFile);
      }
    }
  }

  static LspScope parseLspScopeKey(String scopeKey) {
    if (scopeKey == null || scopeKey.isBlank()) {
      return null;
    }
    int separator = scopeKey.indexOf(':');
    if (separator <= 0 || separator >= scopeKey.length() - 1) {
      return null;
    }
    return new LspScope(scopeKey.substring(0, separator), scopeKey.substring(separator + 1));
  }

  private Map<String, String> readJsonStamp(Path stampFile) throws IOException {
    if (!Files.isRegularFile(stampFile)) {
      return Map.of();
    }
    String raw = Files.readString(stampFile);
    if (raw.isBlank()) {
      return Map.of();
    }
    Map<String, String> stamp =
        jsonMapper.readValue(raw, new TypeReference<LinkedHashMap<String, String>>() {});
    return stamp == null ? Map.of() : stamp;
  }

  private void upsertRunnerPoolRow(
      String dockerImage, String imageId, boolean warmed, Instant warmedAt) {
    runnerPoolRepository
        .findById(dockerImage)
        .ifPresentOrElse(
            existing -> {
              existing.setImageId(imageId);
              existing.setWarmed(warmed);
              existing.setWarmedAt(warmedAt);
            },
            () ->
                runnerPoolRepository.save(
                    new RunnerPoolWarmStateEntity(dockerImage, imageId, warmed, warmedAt)));
  }

  private void upsertLspRow(
      String label, String dockerImage, String imageId, boolean warmed, Instant warmedAt) {
    LspWarmStateEntity.LspWarmStateId id = new LspWarmStateEntity.LspWarmStateId(label, dockerImage);
    lspRepository
        .findById(id)
        .ifPresentOrElse(
            existing -> {
              existing.setImageId(imageId);
              existing.setWarmed(warmed);
              existing.setWarmedAt(warmedAt);
            },
            () ->
                lspRepository.save(
                    new LspWarmStateEntity(label, dockerImage, imageId, warmed, warmedAt)));
  }

  record LspScope(String label, String dockerImage) {}
}
