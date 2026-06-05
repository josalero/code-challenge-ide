package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.catalog.api.ChallengeSessionResponse;
import com.codetraininglab.domain.ChallengeSessionEndReason;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.ChallengeSessionEntity;
import com.codetraininglab.platform.persistence.ChallengeSessionRepository;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ChallengeSessionServiceTest {

  @Mock private ChallengeRepository challengeRepository;
  @Mock private ChallengeSessionRepository sessionRepository;
  @Mock private UserRepository userRepository;

  private ChallengeSessionService service;
  private UUID userId;
  private UUID challengeId;
  private Instant now;
  private String passwordHash;
  private ChallengeEntity challenge;

  @BeforeEach
  void setUp() {
    now = Instant.parse("2026-06-04T12:00:00Z");
    service =
        new ChallengeSessionService(
            challengeRepository,
            sessionRepository,
            userRepository,
            Clock.fixed(now, ZoneOffset.UTC));
    userId = UUID.randomUUID();
    challengeId = UUID.randomUUID();
    passwordHash = new BCryptPasswordEncoder(12).encode("Password1");
    challenge =
        new ChallengeEntity(
            challengeId,
            "two-sum",
            "Two Sum",
            "desc",
            "code",
            "{}",
            "seed",
            "easy",
            "java",
            45,
            now,
            now);
  }

  @Test
  void activeSessionReturnsRemainingTime() {
    ChallengeSessionEntity session = activeSession(now.plusSeconds(900));
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));
    when(sessionRepository.findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId))
        .thenReturn(Optional.of(session));

    ChallengeSessionResponse response = service.activeSession(userId, "two-sum");

    assertThat(response.expired()).isFalse();
    assertThat(response.remainingSeconds()).isEqualTo(900);
    assertThat(response.sessionId()).isEqualTo(session.getId());
  }

  @Test
  void activeSessionThrowsWhenMissing() {
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));
    when(sessionRepository.findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.activeSession(userId, "two-sum"))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void startSessionForAdminSkipsPersistence() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(learner(UserRole.ADMIN)));

    ChallengeSessionResponse response = service.startSession(userId, "two-sum");

    assertThat(response.expired()).isFalse();
    assertThat(response.remainingSeconds())
        .isEqualTo(ChallengeSessionDuration.STANDARD_FALLBACK_MINUTES * 60L);
    verify(sessionRepository, never()).save(any());
  }

  @Test
  void startSessionCreatesNewSessionForLearner() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(learner(UserRole.USER)));
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));
    when(sessionRepository.findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId))
        .thenReturn(Optional.empty());

    ChallengeSessionResponse response = service.startSession(userId, "two-sum");

    assertThat(response.expired()).isFalse();
    assertThat(response.remainingSeconds()).isEqualTo(45 * 60L);
    verify(sessionRepository).save(any(ChallengeSessionEntity.class));
  }

  @Test
  void startSessionReusesActiveSession() {
    ChallengeSessionEntity session = activeSession(now.plusSeconds(600));
    when(userRepository.findById(userId)).thenReturn(Optional.of(learner(UserRole.USER)));
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));
    when(sessionRepository.findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId))
        .thenReturn(Optional.of(session));

    ChallengeSessionResponse response = service.startSession(userId, "two-sum");

    assertThat(response.sessionId()).isEqualTo(session.getId());
    assertThat(response.remainingSeconds()).isEqualTo(600);
    verify(sessionRepository, never()).save(any());
  }

  @Test
  void startSessionExpiresStaleSessionBeforeCreatingNew() {
    ChallengeSessionEntity expired = activeSession(now.minusSeconds(30));
    when(userRepository.findById(userId)).thenReturn(Optional.of(learner(UserRole.USER)));
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));
    when(sessionRepository.findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId))
        .thenReturn(Optional.of(expired));

    ChallengeSessionResponse response = service.startSession(userId, "two-sum");

    assertThat(expired.getEndReason()).isEqualTo(ChallengeSessionEndReason.EXPIRED);
    assertThat(response.remainingSeconds()).isEqualTo(45 * 60L);
    ArgumentCaptor<ChallengeSessionEntity> saved = ArgumentCaptor.forClass(ChallengeSessionEntity.class);
    verify(sessionRepository, org.mockito.Mockito.times(2)).save(saved.capture());
    assertThat(saved.getAllValues().getFirst()).isSameAs(expired);
    assertThat(saved.getAllValues().get(1).getId()).isNotEqualTo(expired.getId());
  }

  @Test
  void abandonSessionEndsActiveSession() {
    ChallengeSessionEntity session = activeSession(now.plusSeconds(300));
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));
    when(sessionRepository.findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId))
        .thenReturn(Optional.of(session));

    service.abandonSession(userId, "two-sum");

    assertThat(session.getEndReason()).isEqualTo(ChallengeSessionEndReason.ABANDONED);
    verify(sessionRepository).save(session);
  }

  @Test
  void ensureMayRunOrSubmitAllowsAdminWithoutSession() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(learner(UserRole.ADMIN)));

    service.ensureMayRunOrSubmit(userId, challengeId);

    verify(sessionRepository, never()).findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId);
  }

  @Test
  void ensureMayRunOrSubmitRequiresActiveSessionForLearner() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(learner(UserRole.USER)));
    when(sessionRepository.findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.ensureMayRunOrSubmit(userId, challengeId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.PRECONDITION_REQUIRED);
  }

  @Test
  void ensureMayRunOrSubmitRejectsExpiredSession() {
    ChallengeSessionEntity expired = activeSession(now.minusSeconds(5));
    when(userRepository.findById(userId)).thenReturn(Optional.of(learner(UserRole.USER)));
    when(sessionRepository.findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId))
        .thenReturn(Optional.of(expired));

    assertThatThrownBy(() -> service.ensureMayRunOrSubmit(userId, challengeId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.CONFLICT);

    assertThat(expired.getEndReason()).isEqualTo(ChallengeSessionEndReason.EXPIRED);
    verify(sessionRepository).save(expired);
  }

  @Test
  void endOnGradedSubmitEndsActiveSession() {
    ChallengeSessionEntity session = activeSession(now.plusSeconds(120));
    when(sessionRepository.findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId))
        .thenReturn(Optional.of(session));

    service.endOnGradedSubmit(userId, challengeId);

    assertThat(session.getEndReason()).isEqualTo(ChallengeSessionEndReason.SUBMITTED);
    verify(sessionRepository).save(session);
  }

  @Test
  void activeSessionIdReturnsNullWhenMissing() {
    when(sessionRepository.findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId))
        .thenReturn(Optional.empty());

    assertThat(service.activeSessionId(userId, challengeId)).isNull();
  }

  private ChallengeSessionEntity activeSession(Instant expiresAt) {
    return new ChallengeSessionEntity(UUID.randomUUID(), userId, challengeId, now.minusSeconds(60), expiresAt);
  }

  private UserEntity learner(UserRole role) {
    return new UserEntity(userId, "learner@example.com", passwordHash, role, now, now);
  }
}
