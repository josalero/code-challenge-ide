package com.codetraininglab.operations.application;

import com.codetraininglab.catalog.application.ChallengeLanguageSupport;
import com.codetraininglab.domain.RunnerStatus;
import com.codetraininglab.integration.runner.RunnerContainerPool;
import com.codetraininglab.integration.runner.RunnerJobPayload;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
@ConditionalOnProperty(name = "ctl.docker-enabled", havingValue = "true", matchIfMissing = true)
public class RunnerPoolWarmExecutor {

  private final CtlProperties properties;
  private final LanguageRuntimeRepository runtimeRepository;
  private final LanguageRepository languageRepository;
  private final RunnerContainerPool runnerContainerPool;
  private final JsonMapper jsonMapper;

  public RunnerPoolWarmExecutor(
      CtlProperties properties,
      LanguageRuntimeRepository runtimeRepository,
      LanguageRepository languageRepository,
      RunnerContainerPool runnerContainerPool,
      JsonMapper jsonMapper) {
    this.properties = properties;
    this.runtimeRepository = runtimeRepository;
    this.languageRepository = languageRepository;
    this.runnerContainerPool = runnerContainerPool;
    this.jsonMapper = jsonMapper;
  }

  public Map<String, String> warm(
      boolean force, List<String> onlyLanguages, Consumer<String> log, Path stampFile)
      throws IOException {
    if (!properties.runnerPoolEnabled()) {
      log.accept("Runner pool disabled — skipping smoke warm.\n");
      return loadStamp(stampFile);
    }

    Map<UUID, LanguageEntity> languages =
        languageRepository.findAll().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    LanguageEntity::getId, language -> language));
    LinkedHashSet<String> filters = normalizeLanguageFilters(onlyLanguages);
    Map<String, String> stamp = loadStamp(stampFile);
    Map<String, String> updated = new LinkedHashMap<>(stamp);

    for (LanguageRuntimeEntity runtime : runtimeRepository.findAllOrdered()) {
      if (!runtime.isActive()) {
        continue;
      }
      LanguageEntity language = languages.get(runtime.getLanguageId());
      String languageName = language == null ? "java" : language.getName().toLowerCase();
      if (!filters.isEmpty() && !filters.contains(languageName)) {
        continue;
      }
      String image = runtime.getDockerImage();
      if (image == null || image.isBlank()) {
        continue;
      }
      String label = languageName + " " + runtime.getVersion();
      ImageIdentity identity = inspectImage(image);
      if (!identity.present()) {
        log.accept("Skipping " + label + " — image missing: " + image + "\n");
        continue;
      }
      if (!force && identity.imageId() != null && identity.imageId().equals(updated.get(image))) {
        log.accept("Skipping " + label + " — already warmed for " + image + "\n");
        continue;
      }

      WarmPlan plan = buildWarmPlan(languageName, label);
      if (plan == null) {
        log.accept("Skipping " + label + " — smoke challenge unavailable\n");
        continue;
      }

      log.accept("Smoke warm " + label + " using challenge " + plan.slug() + " …\n");
      RunnerJobPayload job =
          new RunnerJobPayload(
              "warm-" + UUID.randomUUID(),
              plan.slug(),
              plan.workspaceLayout(),
              plan.starterCode(),
              null,
              plan.hiddenTests(),
              RunnerJobPayload.RunnerLimits.defaults());
      String jobJson = jsonMapper.writeValueAsString(job);
      RunnerResult result =
          runnerContainerPool.execute(
              image, plan.challengeDir(), plan.workspaceLayout(), jobJson, job.limits());

      log.accept(
          "  → status="
              + result.status()
              + ", tests="
              + result.tests().size()
              + ", compileWarnings="
              + (result.compile() == null ? 0 : result.compile().warnings())
              + "\n");

      if (isInfrastructureFailure(result)) {
        throw new IllegalStateException(
            "Smoke warm failed for "
                + label
                + ": "
                + result.tests().stream()
                    .filter(test -> "runner".equals(test.name()))
                    .map(RunnerResult.TestOutcome::message)
                    .findFirst()
                    .orElse("runner error"));
      }

      if (identity.imageId() != null) {
        updated.put(image, identity.imageId());
        saveStamp(stampFile, updated);
      }
    }

    return updated;
  }

  static boolean isInfrastructureFailure(RunnerResult result) {
    if (result == null) {
      return true;
    }
    if (!RunnerStatus.FAILED.name().equals(result.status())) {
      return false;
    }
    return result.tests().size() == 1
        && "runner".equals(result.tests().getFirst().name());
  }

  private WarmPlan buildWarmPlan(String languageName, String label) {
    String slug = RunnerSmokeChallenges.slugFor(languageName);
    Path challengeDir =
        Path.of(properties.challengesPath()).toAbsolutePath().normalize().resolve(slug);
    if (!Files.isDirectory(challengeDir)) {
      return null;
    }
    ChallengeLanguageSupport.LanguageFiles files = ChallengeLanguageSupport.filesFor(languageName);
    Path starter = challengeDir.resolve(files.starterRelativePath());
    if (!Files.isRegularFile(starter)) {
      return null;
    }
    try {
      return new WarmPlan(
          slug,
          challengeDir,
          files.workspaceLayout().id(),
          Files.readString(starter),
          loadHiddenTests(challengeDir));
    } catch (IOException ex) {
      throw new IllegalStateException("Could not read starter for " + label + ": " + starter, ex);
    }
  }

  private static List<RunnerJobPayload.HiddenTest> loadHiddenTests(Path challengeDir) {
    Path hiddenDir = challengeDir.resolve("hidden/tests");
    if (!Files.isDirectory(hiddenDir)) {
      return List.of();
    }
    List<RunnerJobPayload.HiddenTest> tests = new ArrayList<>();
    try (Stream<Path> paths = Files.list(hiddenDir)) {
      for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
        String name = path.getFileName().toString();
        tests.add(new RunnerJobPayload.HiddenTest(name, Files.readString(path)));
      }
    } catch (IOException ex) {
      throw new IllegalStateException("Could not read hidden tests under " + hiddenDir, ex);
    }
    return List.copyOf(tests);
  }

  Map<String, String> loadStamp(Path stampFile) {
    return loadStamp(stampFile, jsonMapper);
  }

  static Map<String, String> loadStamp(Path stampFile, JsonMapper jsonMapper) {
    if (!Files.isRegularFile(stampFile)) {
      return new LinkedHashMap<>();
    }
    try {
      String raw = Files.readString(stampFile);
      if (raw.isBlank()) {
        return new LinkedHashMap<>();
      }
      Map<String, String> stamp =
          jsonMapper.readValue(raw, new TypeReference<LinkedHashMap<String, String>>() {});
      return stamp == null ? new LinkedHashMap<>() : new LinkedHashMap<>(stamp);
    } catch (IOException ex) {
      return new LinkedHashMap<>();
    }
  }

  void saveStamp(Path stampFile, Map<String, String> stamp) throws IOException {
    Files.createDirectories(stampFile.getParent());
    Files.writeString(stampFile, jsonMapper.writeValueAsString(stamp));
  }

  private static LinkedHashSet<String> normalizeLanguageFilters(List<String> onlyLanguages) {
    LinkedHashSet<String> filters = new LinkedHashSet<>();
    if (onlyLanguages == null) {
      return filters;
    }
    for (String language : onlyLanguages) {
      if (language != null && !language.isBlank()) {
        filters.add(language.trim().toLowerCase());
      }
    }
    return filters;
  }

  private ImageIdentity inspectImage(String image) {
    try {
      Process process =
          new ProcessBuilder("docker", "image", "inspect", image, "--format", "{{.Id}}").start();
      String output;
      try (var reader = process.inputReader()) {
        output = reader.readLine();
      }
      int exit = process.waitFor();
      if (exit != 0 || output == null || output.isBlank()) {
        return new ImageIdentity(false, null);
      }
      return new ImageIdentity(true, output.trim());
    } catch (IOException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      return new ImageIdentity(false, null);
    }
  }

  private record WarmPlan(
      String slug,
      Path challengeDir,
      String workspaceLayout,
      String starterCode,
      List<RunnerJobPayload.HiddenTest> hiddenTests) {}

  private record ImageIdentity(boolean present, String imageId) {}
}
