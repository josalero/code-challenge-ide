package com.codetraininglab.catalog.application;

import com.codetraininglab.catalog.api.ChallengeTestPayload;
import com.codetraininglab.catalog.api.CreateChallengeRequest;
import com.codetraininglab.catalog.application.ChallengeService.ChallengeSummary;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestEntity;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestRepository;
import com.codetraininglab.platform.persistence.ChallengePublicTestEntity;
import com.codetraininglab.platform.persistence.ChallengePublicTestRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@Service
public class ChallengePublisher {

  private final CtlProperties properties;
  private final ChallengeRepository challengeRepository;
  private final ChallengePublicTestRepository publicTestRepository;
  private final ChallengeHiddenTestRepository hiddenTestRepository;
  private final LanguageRepository languageRepository;
  private final LanguageRuntimeRepository runtimeRepository;
  private final JsonMapper jsonMapper;
  private final Clock clock;
  private final Yaml yaml;

  public ChallengePublisher(
      CtlProperties properties,
      ChallengeRepository challengeRepository,
      ChallengePublicTestRepository publicTestRepository,
      ChallengeHiddenTestRepository hiddenTestRepository,
      LanguageRepository languageRepository,
      LanguageRuntimeRepository runtimeRepository,
      JsonMapper jsonMapper,
      Clock clock) {
    this.properties = properties;
    this.challengeRepository = challengeRepository;
    this.publicTestRepository = publicTestRepository;
    this.hiddenTestRepository = hiddenTestRepository;
    this.languageRepository = languageRepository;
    this.runtimeRepository = runtimeRepository;
    this.jsonMapper = jsonMapper;
    this.clock = clock;
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    this.yaml = new Yaml(options);
  }

  @Transactional
  public ChallengeSummary create(CreateChallengeRequest request) {
    String slug = request.slug().trim().toLowerCase();
    if (challengeRepository.findBySlug(slug).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Challenge slug already exists");
    }
    String language = request.language().trim().toLowerCase();
    LanguageEntity languageEntity =
        languageRepository
            .findByName(language)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Unsupported language: " + language));
    String runtimeVersion = request.defaultRuntimeVersion().trim();
    runtimeRepository
        .findByLanguageIdAndVersion(languageEntity.getId(), runtimeVersion)
        .filter(LanguageRuntimeEntity::isActive)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Runtime not available for " + language + " " + runtimeVersion));

    Path challengeDir = resolveChallengeDir(slug);
    if (Files.exists(challengeDir)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Challenge directory already exists");
    }

    try {
      writeChallengeTree(challengeDir, request, slug, language);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to write challenge files", e);
    }

    Instant now = clock.instant();
    int lineCoverage = request.lineCoveragePercent() <= 0 ? 80 : request.lineCoveragePercent();
    Map<String, Object> gating = Map.of("line_coverage_percent", lineCoverage);
    String gatingJson;
    try {
      gatingJson = jsonMapper.writeValueAsString(gating);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid gating config", e);
    }
    ChallengeEntity entity =
        new ChallengeEntity(
            UUID.randomUUID(),
            slug,
            request.title().trim(),
            request.descriptionMd().trim(),
            request.starterCode(),
            gatingJson,
            "user",
            request.difficulty().trim().toLowerCase(),
            language,
            now,
            now);
    challengeRepository.save(entity);
    persistTests(entity.getId(), request.publicTests(), request.hiddenTests(), language);
    return new ChallengeSummary(
        entity.getSlug(), entity.getTitle(), entity.getDifficulty(), entity.getLanguage());
  }

  private void writeChallengeTree(
      Path challengeDir, CreateChallengeRequest request, String slug, String language)
      throws IOException {
    Files.createDirectories(challengeDir);
    Files.createDirectories(challengeDir.resolve("public/tests"));
    Files.createDirectories(challengeDir.resolve("hidden/tests"));

    ChallengeLanguageSupport.LanguageFiles files = ChallengeLanguageSupport.filesFor(language);
    Files.createDirectories(challengeDir.resolve("starter"));
    Files.writeString(challengeDir.resolve(files.starterRelativePath()), request.starterCode());

    writeTestFiles(challengeDir.resolve("public/tests"), request.publicTests(), files.testFileSuffix());
    writeTestFiles(challengeDir.resolve("hidden/tests"), request.hiddenTests(), files.testFileSuffix());

    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("slug", slug);
    meta.put("title", request.title().trim());
    meta.put("difficulty", request.difficulty().trim().toLowerCase());
    meta.put("language", language);
    meta.put("description_md", request.descriptionMd().trim());
    int lineCoverage = request.lineCoveragePercent() <= 0 ? 80 : request.lineCoveragePercent();
    meta.put("gating_config", Map.of("line_coverage_percent", lineCoverage));
    meta.put("limits", Map.of("per_test_timeout_seconds", 10));
    meta.put("default_runtime_version", request.defaultRuntimeVersion().trim());
    if ("java".equals(language)) {
      meta.put("starter_main_class", "com.challenge.Solution");
    } else if ("python".equals(language)) {
      meta.put("starter_main_class", "solution");
    }
    Files.writeString(challengeDir.resolve("challenge.yml"), yaml.dump(meta));
  }

  private static void writeTestFiles(
      Path testsDir, List<ChallengeTestPayload> tests, String extension) throws IOException {
    int index = 0;
    for (ChallengeTestPayload test : tests) {
      String fileName = normalizeTestFileName(test.name(), extension, index++);
      Files.writeString(testsDir.resolve(fileName), test.source());
    }
  }

  private static String normalizeTestFileName(String name, String extension, int index) {
    String trimmed = name.trim();
    if (trimmed.isEmpty()) {
      return defaultTestFileName(extension, index);
    }
    if (trimmed.endsWith(extension)) {
      return trimmed;
    }
    if ("_test.go".equals(extension)) {
      return trimmed.replaceAll("(?i)\\.go$", "") + "_test.go";
    }
    if (".test.js".equals(extension) || ".test.ts".equals(extension)) {
      return trimmed.replaceAll("(?i)\\.(js|ts)$", "") + extension;
    }
    if (!trimmed.endsWith("Test") && !trimmed.endsWith("test")) {
      trimmed = trimmed + "Test";
    }
    return trimmed + extension;
  }

  private static String defaultTestFileName(String extension, int index) {
    return switch (extension) {
      case "_test.go" -> "challenge_test.go";
      case ".test.js" -> "challenge.test.js";
      case ".test.ts" -> "challenge.test.ts";
      case ".rs" -> "challenge_tests.rs";
      case ".py" -> "test_" + index + ".py";
      default -> "Test" + index + extension;
    };
  }

  private void persistTests(
      UUID challengeId,
      List<ChallengeTestPayload> publicTests,
      List<ChallengeTestPayload> hiddenTests,
      String language) {
    String extension = ChallengeLanguageSupport.filesFor(language).testFileSuffix();
    int order = 0;
    for (ChallengeTestPayload test : publicTests) {
      String fileName = normalizeTestFileName(test.name(), extension, order);
      String testName = fileName.replace(extension, "");
      String description = test.description() == null ? "" : test.description();
      publicTestRepository.save(
          new ChallengePublicTestEntity(
              UUID.randomUUID(), challengeId, testName, description, order++));
    }
    order = 0;
    for (ChallengeTestPayload test : hiddenTests) {
      String fileName = normalizeTestFileName(test.name(), extension, order);
      String testName = fileName.replace(extension, "");
      hiddenTestRepository.save(
          new ChallengeHiddenTestEntity(
              UUID.randomUUID(), challengeId, testName, test.source(), order++));
    }
  }

  private Path resolveChallengeDir(String slug) {
    return Path.of(properties.challengesPath()).toAbsolutePath().normalize().resolve(slug);
  }
}
