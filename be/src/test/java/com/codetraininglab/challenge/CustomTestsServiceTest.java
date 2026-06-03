package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.CustomTestsRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomTestsServiceTest {

  @Mock private CustomTestsRepository customTestsRepository;
  @Mock private ChallengeRepository challengeRepository;

  private CustomTestsService service;

  @BeforeEach
  void setUp() {
    service =
        new CustomTestsService(
            customTestsRepository, challengeRepository, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
  }

  @Test
  void savesCustomTests() {
    UUID challengeId = UUID.randomUUID();
    when(challengeRepository.findBySlug("slug"))
        .thenReturn(
            Optional.of(
                new ChallengeEntity(
                    challengeId,
                    "slug",
                    "t",
                    "d",
                    "s",
                    "{}",
                    "git",
                    "easy",
                    "java",
                    null,
                    Instant.EPOCH,
                    Instant.EPOCH)));
    when(customTestsRepository.findByUserIdAndChallengeId(any(), any())).thenReturn(Optional.empty());
    when(customTestsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var response =
        service.save(UUID.randomUUID(), "slug", new CustomTestsService.CustomTestsRequest("@Test void x(){}"));
    assertThat(response.code()).contains("@Test");
  }
}
