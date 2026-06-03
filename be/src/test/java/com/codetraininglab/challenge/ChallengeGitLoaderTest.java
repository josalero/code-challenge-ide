package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestRepository;
import com.codetraininglab.platform.persistence.ChallengePublicTestRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import tools.jackson.databind.json.JsonMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;

class ChallengeGitLoaderTest {

  @TempDir Path tempDir;

  @Test
  void loadsChallengeFromDirectory() throws Exception {
    Path challengeDir = tempDir.resolve("demo");
    Files.createDirectories(challengeDir.resolve("starter"));
    Files.createDirectories(challengeDir.resolve("public/tests"));
    Files.createDirectories(challengeDir.resolve("hidden/tests"));
    Files.writeString(
        challengeDir.resolve("challenge.yml"),
        """
        slug: demo
        title: Demo
        difficulty: easy
        description_md: Hello
        gating_config:
          line_coverage_percent: 80
        """);
    Files.writeString(
        challengeDir.resolve("starter/Solution.java"), "package com.challenge; class Solution {}");
    Files.writeString(
        challengeDir.resolve("public/tests/DemoTest.java"),
        "package com.challenge.public_; class DemoTest {}");
    Files.writeString(
        challengeDir.resolve("hidden/tests/DemoHiddenTest.java"),
        "package com.challenge.hidden; class DemoHiddenTest {}");

    ChallengeRepository challengeRepository = org.mockito.Mockito.mock(ChallengeRepository.class);
    ChallengePublicTestRepository publicRepo =
        org.mockito.Mockito.mock(ChallengePublicTestRepository.class);
    ChallengeHiddenTestRepository hiddenRepo =
        org.mockito.Mockito.mock(ChallengeHiddenTestRepository.class);
    when(challengeRepository.findBySlug("demo")).thenReturn(Optional.empty());
    when(challengeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var properties = CtlPropertiesTestFixtures.defaults(tempDir.toString());
    ChallengeGitLoader loader =
        new ChallengeGitLoader(
            properties,
            challengeRepository,
            publicRepo,
            hiddenRepo,
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    loader.run(new DefaultApplicationArguments(new String[] {}));

    ArgumentCaptor<ChallengeEntity> captor = ArgumentCaptor.forClass(ChallengeEntity.class);
    verify(challengeRepository).save(captor.capture());
    assertThat(captor.getValue().getSlug()).isEqualTo("demo");
    assertThat(captor.getValue().getSessionDurationMinutes()).isEqualTo(30);
    verify(publicRepo).save(any());
    verify(hiddenRepo).save(any());
  }

  @Test
  void loadsSessionDurationFromLimitsBlock() throws Exception {
    Path challengeDir = tempDir.resolve("timed");
    Files.createDirectories(challengeDir.resolve("starter"));
    Files.createDirectories(challengeDir.resolve("public/tests"));
    Files.createDirectories(challengeDir.resolve("hidden/tests"));
    Files.writeString(
        challengeDir.resolve("challenge.yml"),
        """
        slug: timed
        title: Timed
        difficulty: hard
        description_md: Hello
        gating_config:
          line_coverage_percent: 80
        limits:
          session_duration_minutes: 45
        """);
    Files.writeString(
        challengeDir.resolve("starter/Solution.java"), "package com.challenge; class Solution {}");
    Files.writeString(
        challengeDir.resolve("public/tests/DemoTest.java"),
        "package com.challenge.public_; class DemoTest {}");
    Files.writeString(
        challengeDir.resolve("hidden/tests/DemoHiddenTest.java"),
        "package com.challenge.hidden; class DemoHiddenTest {}");

    ChallengeRepository challengeRepository = org.mockito.Mockito.mock(ChallengeRepository.class);
    when(challengeRepository.findBySlug("timed")).thenReturn(Optional.empty());
    when(challengeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var properties = CtlPropertiesTestFixtures.defaults(tempDir.toString());
    ChallengeGitLoader loader =
        new ChallengeGitLoader(
            properties,
            challengeRepository,
            org.mockito.Mockito.mock(ChallengePublicTestRepository.class),
            org.mockito.Mockito.mock(ChallengeHiddenTestRepository.class),
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    loader.run(new DefaultApplicationArguments(new String[] {}));

    ArgumentCaptor<ChallengeEntity> captor = ArgumentCaptor.forClass(ChallengeEntity.class);
    verify(challengeRepository).save(captor.capture());
    assertThat(captor.getValue().getSessionDurationMinutes()).isEqualTo(45);
  }

  @Test
  void skipsLoadWhenChallengesPathMissing() throws Exception {
    Path missingRoot = tempDir.resolve("not-a-challenges-dir");
    ChallengeRepository challengeRepository = org.mockito.Mockito.mock(ChallengeRepository.class);
    var properties = CtlPropertiesTestFixtures.defaults(missingRoot.toString());
    ChallengeGitLoader loader =
        new ChallengeGitLoader(
            properties,
            challengeRepository,
            org.mockito.Mockito.mock(ChallengePublicTestRepository.class),
            org.mockito.Mockito.mock(ChallengeHiddenTestRepository.class),
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

    loader.run(new DefaultApplicationArguments(new String[] {}));

    org.mockito.Mockito.verifyNoInteractions(challengeRepository);
  }

  @Test
  void skipsDirectoryWithoutChallengeYaml() throws Exception {
    Files.createDirectories(tempDir.resolve("empty-dir"));
    ChallengeRepository challengeRepository = org.mockito.Mockito.mock(ChallengeRepository.class);
    var properties = CtlPropertiesTestFixtures.defaults(tempDir.toString());
    ChallengeGitLoader loader =
        new ChallengeGitLoader(
            properties,
            challengeRepository,
            org.mockito.Mockito.mock(ChallengePublicTestRepository.class),
            org.mockito.Mockito.mock(ChallengeHiddenTestRepository.class),
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

    loader.run(new DefaultApplicationArguments(new String[] {}));

    org.mockito.Mockito.verifyNoInteractions(challengeRepository);
  }

  @Test
  void seedsPublicTestsFromYamlMetadata() throws Exception {
    Path challengeDir = tempDir.resolve("meta");
    Files.createDirectories(challengeDir.resolve("starter"));
    Files.createDirectories(challengeDir.resolve("hidden/tests"));
    Files.writeString(
        challengeDir.resolve("challenge.yml"),
        """
        slug: meta
        title: Meta
        difficulty: easy
        description_md: Hello
        gating_config:
          line_coverage_percent: 80
        public_tests_meta:
          - name: DemoTest
            description: Checks demo behavior
        """);
    Files.writeString(
        challengeDir.resolve("starter/Solution.java"), "package com.challenge; class Solution {}");
    Files.writeString(
        challengeDir.resolve("hidden/tests/DemoHiddenTest.java"),
        "package com.challenge.hidden; class DemoHiddenTest {}");

    ChallengeRepository challengeRepository = org.mockito.Mockito.mock(ChallengeRepository.class);
    ChallengePublicTestRepository publicRepo =
        org.mockito.Mockito.mock(ChallengePublicTestRepository.class);
    when(challengeRepository.findBySlug("meta")).thenReturn(Optional.empty());
    when(challengeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var properties = CtlPropertiesTestFixtures.defaults(tempDir.toString());
    ChallengeGitLoader loader =
        new ChallengeGitLoader(
            properties,
            challengeRepository,
            publicRepo,
            org.mockito.Mockito.mock(ChallengeHiddenTestRepository.class),
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    loader.run(new DefaultApplicationArguments(new String[] {}));

    org.mockito.ArgumentCaptor<com.codetraininglab.platform.persistence.ChallengePublicTestEntity>
        captor =
            org.mockito.ArgumentCaptor.forClass(
                com.codetraininglab.platform.persistence.ChallengePublicTestEntity.class);
    verify(publicRepo).save(captor.capture());
    assertThat(captor.getValue().getName()).isEqualTo("DemoTest");
    assertThat(captor.getValue().getDescription()).isEqualTo("Checks demo behavior");
  }

  @Test
  void syncsSessionDurationWhenExistingChallengeLimitsChange() throws Exception {
    Path challengeDir = tempDir.resolve("timed-sync");
    Files.createDirectories(challengeDir.resolve("starter"));
    Files.createDirectories(challengeDir.resolve("public/tests"));
    Files.createDirectories(challengeDir.resolve("hidden/tests"));
    Files.writeString(
        challengeDir.resolve("challenge.yml"),
        """
        slug: timed-sync
        title: Timed
        difficulty: hard
        description_md: Hello
        gating_config:
          line_coverage_percent: 80
        limits:
          session_duration_minutes: 90
        """);
    Files.writeString(
        challengeDir.resolve("starter/Solution.java"), "package com.challenge; class Solution {}");
    Files.writeString(
        challengeDir.resolve("public/tests/DemoTest.java"),
        "package com.challenge.public_; class DemoTest {}");
    Files.writeString(
        challengeDir.resolve("hidden/tests/DemoHiddenTest.java"),
        "package com.challenge.hidden; class DemoHiddenTest {}");

    ChallengeEntity existing =
        new ChallengeEntity(
            UUID.randomUUID(),
            "timed-sync",
            "Timed",
            "Hello",
            "package com.challenge; class Solution {}",
            "{\"line_coverage_percent\":80}",
            "git",
            "hard",
            "java",
            30,
            Instant.EPOCH,
            Instant.EPOCH);

    ChallengeRepository challengeRepository = org.mockito.Mockito.mock(ChallengeRepository.class);
    when(challengeRepository.findBySlug("timed-sync")).thenReturn(Optional.of(existing));
    when(challengeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Clock clock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);
    var properties = CtlPropertiesTestFixtures.defaults(tempDir.toString());
    ChallengeGitLoader loader =
        new ChallengeGitLoader(
            properties,
            challengeRepository,
            org.mockito.Mockito.mock(ChallengePublicTestRepository.class),
            org.mockito.Mockito.mock(ChallengeHiddenTestRepository.class),
            JsonMapper.builder().build(),
            clock);
    loader.run(new DefaultApplicationArguments(new String[] {}));

    ArgumentCaptor<ChallengeEntity> captor = ArgumentCaptor.forClass(ChallengeEntity.class);
    verify(challengeRepository).save(captor.capture());
    assertThat(captor.getValue().getSessionDurationMinutes()).isEqualTo(90);
  }

  @Test
  void syncsDescriptionWhenChallengeAlreadyExists() throws Exception {
    Path challengeDir = tempDir.resolve("demo");
    Files.createDirectories(challengeDir.resolve("starter"));
    Files.createDirectories(challengeDir.resolve("public/tests"));
    Files.createDirectories(challengeDir.resolve("hidden/tests"));
    Files.writeString(
        challengeDir.resolve("challenge.yml"),
        """
        slug: demo
        title: Demo
        difficulty: easy
        description_md: |
          ## What to do
          Updated narrative for learners.
        gating_config:
          line_coverage_percent: 80
        """);
    Files.writeString(
        challengeDir.resolve("starter/Solution.java"), "package com.challenge; class Solution {}");
    Files.writeString(
        challengeDir.resolve("public/tests/DemoTest.java"),
        "package com.challenge.public_; class DemoTest {}");
    Files.writeString(
        challengeDir.resolve("hidden/tests/DemoHiddenTest.java"),
        "package com.challenge.hidden; class DemoHiddenTest {}");

    ChallengeEntity existing =
        new ChallengeEntity(
            UUID.randomUUID(),
            "demo",
            "Demo",
            "Old description",
            "package com.challenge; class Solution {}",
            "{\"line_coverage_percent\":80}",
            "git",
            "easy",
            "java",
            null,
            Instant.EPOCH,
            Instant.EPOCH);

    ChallengeRepository challengeRepository = org.mockito.Mockito.mock(ChallengeRepository.class);
    ChallengePublicTestRepository publicRepo =
        org.mockito.Mockito.mock(ChallengePublicTestRepository.class);
    ChallengeHiddenTestRepository hiddenRepo =
        org.mockito.Mockito.mock(ChallengeHiddenTestRepository.class);
    when(challengeRepository.findBySlug("demo")).thenReturn(Optional.of(existing));
    when(challengeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    Clock clock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);
    var properties = CtlPropertiesTestFixtures.defaults(tempDir.toString());
    ChallengeGitLoader loader =
        new ChallengeGitLoader(
            properties,
            challengeRepository,
            publicRepo,
            hiddenRepo,
            JsonMapper.builder().build(),
            clock);
    loader.run(new DefaultApplicationArguments(new String[] {}));

    ArgumentCaptor<ChallengeEntity> captor = ArgumentCaptor.forClass(ChallengeEntity.class);
    verify(challengeRepository).save(captor.capture());
    assertThat(captor.getValue().getDescriptionMd()).contains("Updated narrative");
    verify(publicRepo, org.mockito.Mockito.never()).save(any());
    verify(hiddenRepo, org.mockito.Mockito.never()).save(any());
  }
}
