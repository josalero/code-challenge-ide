package com.codetraininglab.catalog.application;

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
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChallengeSessionService {

  private final ChallengeRepository challengeRepository;
  private final ChallengeSessionRepository sessionRepository;
  private final UserRepository userRepository;
  private final Clock clock;

  public ChallengeSessionService(
      ChallengeRepository challengeRepository,
      ChallengeSessionRepository sessionRepository,
      UserRepository userRepository,
      Clock clock) {
    this.challengeRepository = challengeRepository;
    this.sessionRepository = sessionRepository;
    this.userRepository = userRepository;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public ChallengeSessionResponse activeSession(UUID userId, String challengeSlug) {
    ChallengeEntity challenge = requireChallenge(challengeSlug);
    return sessionRepository
        .findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challenge.getId())
        .map(session -> toResponse(session))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session"));
  }

  @Transactional
  public ChallengeSessionResponse startSession(UUID userId, String challengeSlug) {
    UserEntity user = requireUser(userId);
    if (user.getRole() == UserRole.ADMIN) {
      return adminSessionResponse();
    }
    ChallengeEntity challenge = requireChallenge(challengeSlug);
    Instant now = clock.instant();
    var existing =
        sessionRepository.findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challenge.getId());
    if (existing.isPresent()) {
      ChallengeSessionEntity session = existing.get();
      if (session.isExpired(now)) {
        session.end(now, ChallengeSessionEndReason.EXPIRED);
        sessionRepository.save(session);
      } else {
        return toResponse(session);
      }
    }
    int durationSeconds = ChallengeSessionDuration.durationSeconds(challenge);
    ChallengeSessionEntity session =
        new ChallengeSessionEntity(
            UUID.randomUUID(),
            userId,
            challenge.getId(),
            now,
            now.plusSeconds(durationSeconds));
    sessionRepository.save(session);
    return toResponse(session);
  }

  @Transactional
  public void abandonSession(UUID userId, String challengeSlug) {
    ChallengeEntity challenge = requireChallenge(challengeSlug);
    sessionRepository
        .findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challenge.getId())
        .ifPresent(
            session -> {
              session.end(clock.instant(), ChallengeSessionEndReason.ABANDONED);
              sessionRepository.save(session);
            });
  }

  @Transactional
  public void ensureMayRunOrSubmit(UUID userId, UUID challengeId) {
    UserEntity user = requireUser(userId);
    if (user.getRole() == UserRole.ADMIN) {
      return;
    }
    Instant now = clock.instant();
    ChallengeSessionEntity session =
        sessionRepository
            .findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.PRECONDITION_REQUIRED, "Start the timed attempt before running code"));
    if (session.isExpired(now)) {
      session.end(now, ChallengeSessionEndReason.EXPIRED);
      sessionRepository.save(session);
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Challenge session expired");
    }
  }

  @Transactional
  public void endOnGradedSubmit(UUID userId, UUID challengeId) {
    sessionRepository
        .findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId)
        .ifPresent(
            session -> {
              session.end(clock.instant(), ChallengeSessionEndReason.SUBMITTED);
              sessionRepository.save(session);
            });
  }

  @Transactional(readOnly = true)
  public UUID activeSessionId(UUID userId, UUID challengeId) {
    return sessionRepository
        .findByUserIdAndChallengeIdAndEndedAtIsNull(userId, challengeId)
        .map(ChallengeSessionEntity::getId)
        .orElse(null);
  }

  private ChallengeSessionResponse toResponse(ChallengeSessionEntity session) {
    Instant now = clock.instant();
    long remainingSeconds =
        session.isActive(now)
            ? Math.max(0, Duration.between(now, session.getExpiresAt()).getSeconds())
            : 0;
    boolean expired = session.isExpired(now) || (session.getEndedAt() == null && remainingSeconds == 0);
    return new ChallengeSessionResponse(
        session.getId(),
        session.getStartedAt(),
        session.getExpiresAt(),
        remainingSeconds,
        expired);
  }

  private ChallengeSessionResponse adminSessionResponse() {
    Instant now = clock.instant();
    return new ChallengeSessionResponse(
        UUID.randomUUID(),
        now,
        now.plusSeconds(ChallengeSessionDuration.STANDARD_FALLBACK_MINUTES * 60L),
        ChallengeSessionDuration.STANDARD_FALLBACK_MINUTES * 60L,
        false);
  }

  private ChallengeEntity requireChallenge(String slug) {
    return challengeRepository
        .findBySlug(slug)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
  }

  private UserEntity requireUser(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }
}
