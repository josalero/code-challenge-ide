package com.codetraininglab.catalog.application;

import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.platform.persistence.UserStartedCountAggregate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChallengeQuotaService {

  private final UserRepository userRepository;
  private final UserProgressRepository progressRepository;
  private final SubmissionRepository submissionRepository;
  private final CtlProperties properties;

  public ChallengeQuotaService(
      UserRepository userRepository,
      UserProgressRepository progressRepository,
      SubmissionRepository submissionRepository,
      CtlProperties properties) {
    this.userRepository = userRepository;
    this.progressRepository = progressRepository;
    this.submissionRepository = submissionRepository;
    this.properties = properties;
  }

  public int platformDefaultMax() {
    return properties.userMaxStartedChallenges();
  }

  @Transactional(readOnly = true)
  public void ensureMayStartChallenge(UUID userId, UUID challengeId) {
    UserEntity user = requireUser(userId);
    if (isExempt(user) || isChallengeStarted(userId, challengeId)) {
      return;
    }
    Integer effectiveLimit = effectiveChallengeLimit(user);
    if (effectiveLimit == null) {
      return;
    }
    int started = countStartedChallenges(userId);
    if (started >= effectiveLimit) {
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS,
          "You have reached the limit of "
              + effectiveLimit
              + " started exercises. Finish or continue one of your in-progress challenges before starting a new one.");
    }
  }

  @Transactional(readOnly = true)
  public int countStartedChallenges(UUID userId) {
    List<UserStartedCountAggregate> rows =
        submissionRepository.countStartedChallengesByUserIds(List.of(userId));
    if (rows.isEmpty()) {
      return 0;
    }
    return Math.toIntExact(rows.getFirst().getStartedCount());
  }

  @Transactional(readOnly = true)
  public boolean isChallengeStarted(UUID userId, UUID challengeId) {
    boolean progressStarted =
        progressRepository
            .findByUserIdAndChallengeId(userId, challengeId)
            .map(progress -> progress.getState() != ProgressState.NOT_STARTED)
            .orElse(false);
    return progressStarted || submissionRepository.existsByUserIdAndChallengeId(userId, challengeId);
  }

  @Transactional(readOnly = true)
  public ChallengeQuotaSnapshot quotaForUser(UUID userId) {
    UserEntity user = requireUser(userId);
    if (isExempt(user)) {
      return ChallengeQuotaSnapshot.unlimited();
    }
    Integer effectiveLimit = effectiveChallengeLimit(user);
    if (effectiveLimit == null) {
      return ChallengeQuotaSnapshot.unlimited();
    }
    int started = countStartedChallenges(userId);
    return new ChallengeQuotaSnapshot(effectiveLimit, Math.max(0, effectiveLimit - started));
  }

  /** Null when unlimited (admin or explicit unlimited override). */
  @Transactional(readOnly = true)
  public Integer effectiveChallengeLimit(UserEntity user) {
    if (user.getRole() == UserRole.ADMIN) {
      return null;
    }
    int resolved = resolveMaxLimit(user);
    return resolved <= 0 ? null : resolved;
  }

  int resolveMaxLimit(UserEntity user) {
    Integer override = user.getMaxStartedChallenges();
    if (override != null) {
      return override;
    }
    return properties.userMaxStartedChallenges();
  }

  private boolean isExempt(UserEntity user) {
    return user.getRole() == UserRole.ADMIN;
  }

  private UserEntity requireUser(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  public record ChallengeQuotaSnapshot(Integer maxStartedChallenges, Integer challengesRemaining) {
    static ChallengeQuotaSnapshot unlimited() {
      return new ChallengeQuotaSnapshot(null, null);
    }
  }
}
