package com.codetraininglab.catalog.application;

import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestEntity;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestRepository;
import com.codetraininglab.platform.persistence.ChallengePublicTestEntity;
import com.codetraininglab.platform.persistence.ChallengePublicTestRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import tools.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

@Component
public class ChallengeGitLoader implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(ChallengeGitLoader.class);

  private final CtlProperties properties;
  private final ChallengeRepository challengeRepository;
  private final ChallengePublicTestRepository publicTestRepository;
  private final ChallengeHiddenTestRepository hiddenTestRepository;
  private final JsonMapper jsonMapper;
  private final Clock clock;
  private final Yaml yaml = new Yaml();

  public ChallengeGitLoader(
      CtlProperties properties,
      ChallengeRepository challengeRepository,
      ChallengePublicTestRepository publicTestRepository,
      ChallengeHiddenTestRepository hiddenTestRepository,
      JsonMapper jsonMapper,
      Clock clock) {
    this.properties = properties;
    this.challengeRepository = challengeRepository;
    this.publicTestRepository = publicTestRepository;
    this.hiddenTestRepository = hiddenTestRepository;
    this.jsonMapper = jsonMapper;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) throws Exception {
    Path root = Path.of(properties.challengesPath()).toAbsolutePath().normalize();
    if (!Files.isDirectory(root)) {
      log.warn("Challenges path not found, skipping seed: {}", root);
      return;
    }
    try (Stream<Path> dirs = Files.list(root).filter(Files::isDirectory)) {
      dirs.forEach(dir -> loadChallenge(root, dir));
    }
  }

  private void loadChallenge(Path root, Path dir) {
    Path yamlFile = dir.resolve("challenge.yml");
    if (!Files.isRegularFile(yamlFile)) {
      return;
    }
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> meta = yaml.load(Files.readString(yamlFile));
      String slug = stringVal(meta, "slug");
      if (slug == null) {
        return;
      }
      Optional<ChallengeEntity> existing = challengeRepository.findBySlug(slug);
      if (existing.isPresent()) {
        syncPublicTestMetadata(dir, meta, existing.get().getId());
        syncDescriptionFromYaml(meta, existing.get());
        syncSessionDurationFromYaml(meta, existing.get());
        return;
      }
      String language = stringVal(meta, "language");
      if (language == null || language.isBlank()) {
        language = "java";
      }
      language = language.toLowerCase();
      ChallengeLanguageSupport.LanguageFiles files = ChallengeLanguageSupport.filesFor(language);
      String starter = Files.readString(dir.resolve(files.starterRelativePath()));
      String gatingJson = jsonMapper.writeValueAsString(meta.getOrDefault("gating_config", Map.of()));
      Instant now = clock.instant();
      String difficulty = stringVal(meta, "difficulty");
      Integer sessionDurationMinutes = resolveSessionDurationMinutes(meta, difficulty);
      ChallengeEntity entity =
          new ChallengeEntity(
              UUID.randomUUID(),
              slug,
              stringVal(meta, "title"),
              stringVal(meta, "description_md"),
              starter,
              gatingJson,
              "git",
              difficulty,
              language,
              sessionDurationMinutes,
              now,
              now);
      challengeRepository.save(entity);
      seedPublicTests(dir, meta, entity.getId(), files.testFileSuffix());
      seedTests(dir.resolve("hidden/tests"), entity.getId(), false, files.testFileSuffix());
      log.info("Seeded challenge {}", slug);
    } catch (IOException e) {
      log.error("Failed to load challenge from {}", dir, e);
    }
  }

  private void syncSessionDurationFromYaml(Map<String, Object> meta, ChallengeEntity entity) {
    Integer fromYaml = ChallengeSessionLimits.parseSessionDurationMinutes(meta);
    if (fromYaml == null || fromYaml.equals(entity.getSessionDurationMinutes())) {
      return;
    }
    entity.setSessionDurationMinutes(fromYaml);
    entity.setUpdatedAt(clock.instant());
    challengeRepository.save(entity);
    log.info("Synced session duration for challenge {}", entity.getSlug());
  }

  private static Integer resolveSessionDurationMinutes(
      Map<String, Object> meta, String difficulty) {
    Integer fromYaml = ChallengeSessionLimits.parseSessionDurationMinutes(meta);
    if (fromYaml != null) {
      return fromYaml;
    }
    return ChallengeSessionLimits.defaultMinutesForDifficulty(difficulty);
  }

  private void syncDescriptionFromYaml(Map<String, Object> meta, ChallengeEntity entity) {
    String descriptionMd = stringVal(meta, "description_md");
    if (descriptionMd == null || descriptionMd.isBlank()) {
      return;
    }
    if (descriptionMd.equals(entity.getDescriptionMd())) {
      return;
    }
    entity.setDescriptionMd(descriptionMd);
    entity.setUpdatedAt(clock.instant());
    challengeRepository.save(entity);
    log.info("Synced description for challenge {}", entity.getSlug());
  }

  private void syncPublicTestMetadata(Path dir, Map<String, Object> meta, UUID challengeId) {
    List<PublicTestMeta> metas = readPublicTestsMeta(meta);
    if (metas.isEmpty()) {
      return;
    }
    List<ChallengePublicTestEntity> existing =
        publicTestRepository.findByChallengeIdOrderBySortOrderAsc(challengeId);
    boolean fullyDescribed =
        existing.size() == metas.size()
            && existing.stream().noneMatch(row -> row.getDescription().isBlank());
    if (fullyDescribed) {
      return;
    }
    publicTestRepository.deleteAll(existing);
    int order = 0;
    for (PublicTestMeta entry : metas) {
      publicTestRepository.save(
          new ChallengePublicTestEntity(
              UUID.randomUUID(), challengeId, entry.name(), entry.description(), order++));
    }
    log.info("Synced {} public test description(s) for challenge {}", metas.size(), challengeId);
  }

  private void seedPublicTests(
      Path dir, Map<String, Object> meta, UUID challengeId, String extension) throws IOException {
    List<PublicTestMeta> metas = readPublicTestsMeta(meta);
    if (!metas.isEmpty()) {
      int order = 0;
      for (PublicTestMeta entry : metas) {
        publicTestRepository.save(
            new ChallengePublicTestEntity(
                UUID.randomUUID(), challengeId, entry.name(), entry.description(), order++));
      }
      return;
    }
    seedTests(dir.resolve("public/tests"), challengeId, true, extension);
  }

  private List<PublicTestMeta> readPublicTestsMeta(Map<String, Object> meta) {
    Object raw = meta.get("public_tests_meta");
    if (!(raw instanceof List<?> list)) {
      return List.of();
    }
    List<PublicTestMeta> result = new ArrayList<>();
    for (Object item : list) {
      if (item instanceof Map<?, ?> map) {
        String name = stringVal(map, "name");
        String description = stringVal(map, "description");
        if (name != null && !name.isBlank()) {
          result.add(new PublicTestMeta(name, description == null ? "" : description));
        }
      }
    }
    return result;
  }

  private void seedTests(Path testsDir, UUID challengeId, boolean isPublic, String extension)
      throws IOException {
    if (!Files.isDirectory(testsDir)) {
      return;
    }
    try (Stream<Path> files =
        Files.list(testsDir).filter(p -> p.toString().endsWith(extension)).sorted()) {
      int order = 0;
      for (Path file : files.toList()) {
        String name = file.getFileName().toString().replace(extension, "");
        if (isPublic) {
          publicTestRepository.save(
              new ChallengePublicTestEntity(
                  UUID.randomUUID(), challengeId, name, "", order++));
        } else {
          hiddenTestRepository.save(
              new ChallengeHiddenTestEntity(
                  UUID.randomUUID(), challengeId, name, Files.readString(file), order++));
        }
      }
    }
  }

  private static String stringVal(Map<?, ?> meta, String key) {
    Object value = meta.get(key);
    return value == null ? null : value.toString();
  }

  private record PublicTestMeta(String name, String description) {}
}
