package com.codetraininglab.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.catalog.application.ChallengeWorkspaceService;
import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
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
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ChallengeWorkspaceServiceTest {

  private static final Instant FIXED_NOW = Instant.parse("2026-06-01T12:00:00Z");

  @Mock private ChallengeRepository challengeRepository;
  @Mock private UserProgressRepository progressRepository;

  private ChallengeWorkspaceService service;
  private final UUID userId = UUID.randomUUID();
  private final UUID challengeId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    service =
        new ChallengeWorkspaceService(
            challengeRepository,
            progressRepository,
            Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
  }

  @Test
  void redoUnlocksSubmittedExercise() {
    ChallengeEntity challenge =
        new ChallengeEntity(
            challengeId,
            "reverse-string",
            "title",
            "desc",
            "starter",
            "{}",
            "git",
            "easy",
            "java",
            null,
            FIXED_NOW,
            FIXED_NOW);
    UserProgressEntity progress =
        new UserProgressEntity(
            UUID.randomUUID(), userId, challengeId, ProgressState.PASSED, FIXED_NOW);
    progress.setSubmittedAt(FIXED_NOW);
    when(challengeRepository.findBySlug("reverse-string")).thenReturn(Optional.of(challenge));
    when(progressRepository.findByUserIdAndChallengeId(userId, challengeId))
        .thenReturn(Optional.of(progress));

    service.redo(userId, "reverse-string");

    assertThat(progress.getSubmittedAt()).isNull();
    assertThat(progress.getState()).isEqualTo(ProgressState.ATTEMPTED);
    assertThat(progress.getUpdatedAt()).isEqualTo(FIXED_NOW);
    verify(progressRepository).save(progress);
  }

  @Test
  void redoRejectsUnknownChallenge() {
    when(challengeRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.redo(userId, "missing"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Challenge not found");
  }

  @Test
  void redoRejectsWhenNeverSubmitted() {
    ChallengeEntity challenge =
        new ChallengeEntity(
            challengeId,
            "reverse-string",
            "title",
            "desc",
            "starter",
            "{}",
            "git",
            "easy",
            "java",
            null,
            FIXED_NOW,
            FIXED_NOW);
    when(challengeRepository.findBySlug("reverse-string")).thenReturn(Optional.of(challenge));
    when(progressRepository.findByUserIdAndChallengeId(userId, challengeId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.redo(userId, "reverse-string"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Nothing to redo");
  }

  @Test
  void redoRejectsWhenExerciseNotLocked() {
    ChallengeEntity challenge =
        new ChallengeEntity(
            challengeId,
            "reverse-string",
            "title",
            "desc",
            "starter",
            "{}",
            "git",
            "easy",
            "java",
            null,
            FIXED_NOW,
            FIXED_NOW);
    UserProgressEntity progress =
        new UserProgressEntity(
            UUID.randomUUID(), userId, challengeId, ProgressState.ATTEMPTED, FIXED_NOW);
    when(challengeRepository.findBySlug("reverse-string")).thenReturn(Optional.of(challenge));
    when(progressRepository.findByUserIdAndChallengeId(userId, challengeId))
        .thenReturn(Optional.of(progress));

    assertThatThrownBy(() -> service.redo(userId, "reverse-string"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("not locked");
    verify(progressRepository, org.mockito.Mockito.never()).save(any());
  }
}
