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
    verify(publicRepo).save(any());
    verify(hiddenRepo).save(any());
  }
}
