package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.platform.persistence.UserStartedCountAggregate;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ChallengeQuotaServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserProgressRepository progressRepository;
  @Mock private SubmissionRepository submissionRepository;

  private ChallengeQuotaService service;
  private final UUID userId = UUID.randomUUID();
  private final UUID challengeId = UUID.randomUUID();
  private final UUID newChallengeId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    var properties =
        new com.codetraininglab.platform.config.CtlProperties(
            true,
            "test-jwt-secret-must-be-at-least-32-characters-long",
            24,
            "http://localhost:5173",
            "challenges",
            "runner",
            "",
            true,
            60,
            CtlPropertiesTestFixtures.TEST_LSP_IMAGES,
            5,
            24,
            "openrouter",
            "",
            "model",
            "http://localhost:11434",
            "ollama",
            false,
            false,
            false,
            5);
    service =
        new ChallengeQuotaService(
            userRepository, progressRepository, submissionRepository, properties);
  }

  @Test
  void ensureMayStartChallengeAllowsAdminWithoutLimit() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(learner(userId, UserRole.ADMIN)));

    service.ensureMayStartChallenge(userId, newChallengeId);
  }

  @Test
  void ensureMayStartChallengeAllowsAlreadyStartedChallenge() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(learner(userId, UserRole.USER)));
    when(progressRepository.findByUserIdAndChallengeId(userId, challengeId))
        .thenReturn(
            Optional.of(
                new UserProgressEntity(
                    UUID.randomUUID(), userId, challengeId, ProgressState.ATTEMPTED, Instant.EPOCH)));

    service.ensureMayStartChallenge(userId, challengeId);
  }

  @Test
  void ensureMayStartChallengeBlocksSixthDistinctExerciseForLearner() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(learner(userId, UserRole.USER)));
    when(progressRepository.findByUserIdAndChallengeId(userId, newChallengeId))
        .thenReturn(Optional.empty());
    when(submissionRepository.existsByUserIdAndChallengeId(userId, newChallengeId)).thenReturn(false);
    when(submissionRepository.countStartedChallengesByUserIds(List.of(userId)))
        .thenReturn(List.of(startedCount(5)));

    assertThatThrownBy(() -> service.ensureMayStartChallenge(userId, newChallengeId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
  }

  @Test
  void quotaForUserReturnsRemainingSlotsForLearner() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(learner(userId, UserRole.USER)));
    when(submissionRepository.countStartedChallengesByUserIds(List.of(userId)))
        .thenReturn(List.of(startedCount(3)));

    var quota = service.quotaForUser(userId);

    assertThat(quota.maxStartedChallenges()).isEqualTo(5);
    assertThat(quota.challengesRemaining()).isEqualTo(2);
  }

  @Test
  void quotaForUserHonorsPerUserOverride() {
    UserEntity user = learner(userId, UserRole.USER);
    user.setMaxStartedChallenges(10);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(progressRepository.findByUserIdAndChallengeId(userId, newChallengeId))
        .thenReturn(Optional.empty());
    when(submissionRepository.existsByUserIdAndChallengeId(userId, newChallengeId)).thenReturn(false);
    when(submissionRepository.countStartedChallengesByUserIds(List.of(userId)))
        .thenReturn(List.of(startedCount(5)));

    service.ensureMayStartChallenge(userId, newChallengeId);

    var quota = service.quotaForUser(userId);
    assertThat(quota.maxStartedChallenges()).isEqualTo(10);
    assertThat(quota.challengesRemaining()).isEqualTo(5);
  }

  private static UserEntity learner(UUID id, UserRole role) {
    return new UserEntity(
        id,
        "learner@test.com",
        new BCryptPasswordEncoder(12).encode("Password1"),
        role,
        Instant.EPOCH,
        Instant.EPOCH,
        "Learner",
        false);
  }

  private UserStartedCountAggregate startedCount(long count) {
    return new UserStartedCountAggregate() {
      @Override
      public UUID getUserId() {
        return userId;
      }

      @Override
      public long getStartedCount() {
        return count;
      }
    };
  }
}
