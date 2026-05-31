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
import java.util.Comparator;
import java.util.Map;
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
      if (slug == null || challengeRepository.findBySlug(slug).isPresent()) {
        return;
      }
      String language = stringVal(meta, "language");
      if (language == null || language.isBlank()) {
        language = "java";
      }
      String starterFile =
          "python".equalsIgnoreCase(language) ? "starter/solution.py" : "starter/Solution.java";
      String testExtension = "python".equalsIgnoreCase(language) ? ".py" : ".java";
      String starter = Files.readString(dir.resolve(starterFile));
      String gatingJson = jsonMapper.writeValueAsString(meta.getOrDefault("gating_config", Map.of()));
      Instant now = clock.instant();
      ChallengeEntity entity =
          new ChallengeEntity(
              UUID.randomUUID(),
              slug,
              stringVal(meta, "title"),
              stringVal(meta, "description_md"),
              starter,
              gatingJson,
              "git",
              stringVal(meta, "difficulty"),
              language.toLowerCase(),
              now,
              now);
      challengeRepository.save(entity);
      seedTests(dir.resolve("public/tests"), entity.getId(), true, testExtension);
      seedTests(dir.resolve("hidden/tests"), entity.getId(), false, testExtension);
      log.info("Seeded challenge {}", slug);
    } catch (IOException e) {
      log.error("Failed to load challenge from {}", dir, e);
    }
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
              new ChallengePublicTestEntity(UUID.randomUUID(), challengeId, name, order++));
        } else {
          hiddenTestRepository.save(
              new ChallengeHiddenTestEntity(
                  UUID.randomUUID(), challengeId, name, Files.readString(file), order++));
        }
      }
    }
  }

  private static String stringVal(Map<String, Object> meta, String key) {
    Object value = meta.get(key);
    return value == null ? null : value.toString();
  }
}
