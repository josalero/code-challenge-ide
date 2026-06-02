package com.codetraininglab.identity.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.UserRole;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class MeControllerTest {

  @Mock private UserRepository userRepository;
  @Mock private UserProgressRepository progressRepository;
  @Mock private ChallengeRepository challengeRepository;

  @InjectMocks private MeController controller;

  private final UUID userId = UUID.randomUUID();

  @Test
  void returnsProfile() {
    when(userRepository.findById(userId))
        .thenReturn(
            Optional.of(
                new UserEntity(
                    userId, "a@b.com", "hash", UserRole.USER, Instant.EPOCH, Instant.EPOCH)));
    var response =
        controller.me(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
    assertThat(response.email()).isEqualTo("a@b.com");
  }

  @Test
  void returnsProgress() {
    UUID challengeId = UUID.randomUUID();
    when(progressRepository.findByUserId(userId))
        .thenReturn(
            List.of(
                new UserProgressEntity(
                    UUID.randomUUID(), userId, challengeId, ProgressState.PASSED, Instant.EPOCH)));
    when(challengeRepository.findById(challengeId))
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
                    Instant.EPOCH,
                    Instant.EPOCH)));
    var progress =
        controller.progress(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
    assertThat(progress.getFirst().state()).isEqualTo("PASSED");
  }
}
