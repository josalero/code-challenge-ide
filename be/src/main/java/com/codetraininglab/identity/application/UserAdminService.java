package com.codetraininglab.identity.application;

import com.codetraininglab.catalog.application.ChallengeQuotaService;
import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.AdminUserSummary;
import com.codetraininglab.identity.api.CreateUserRequest;
import com.codetraininglab.identity.api.CreateUserResponse;
import com.codetraininglab.identity.api.UpdateUserChallengeQuotaRequest;
import com.codetraininglab.identity.api.UserChallengeQuotaResponse;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserPassedCountAggregate;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.platform.persistence.UserStartedCountAggregate;
import com.codetraininglab.platform.persistence.UserSubmissionAggregate;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserAdminService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;
  private final UserWelcomeEmailSender welcomeEmailSender;
  private final SubmissionRepository submissionRepository;
  private final UserProgressRepository progressRepository;
  private final ChallengeQuotaService challengeQuotaService;

  public UserAdminService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      Clock clock,
      UserWelcomeEmailSender welcomeEmailSender,
      SubmissionRepository submissionRepository,
      UserProgressRepository progressRepository,
      ChallengeQuotaService challengeQuotaService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
    this.welcomeEmailSender = welcomeEmailSender;
    this.submissionRepository = submissionRepository;
    this.progressRepository = progressRepository;
    this.challengeQuotaService = challengeQuotaService;
  }

  @Transactional(readOnly = true)
  public List<AdminUserSummary> listUsers(boolean includeInactive) {
    List<UserEntity> users =
        includeInactive
            ? userRepository.findAllByOrderByEmailAsc()
            : userRepository.findAllByDeletedAtIsNullOrderByEmailAsc();
    if (users.isEmpty()) {
      return List.of();
    }

    List<UUID> userIds = users.stream().map(UserEntity::getId).toList();
    Map<UUID, UserSubmissionAggregate> submissionStats = loadSubmissionStats(userIds);
    Map<UUID, Long> passedByUser = loadPassedCounts(userIds);
    Map<UUID, Long> startedByUser = loadStartedCounts(userIds);

    return users.stream()
        .map(
            user -> {
              UserSubmissionAggregate stats = submissionStats.get(user.getId());
              long submissionsTotal = stats == null ? 0L : stats.getTotalSubmissions();
              long practiceRuns = stats == null ? 0L : stats.getPracticeRuns();
              long gradedSubmits = stats == null ? 0L : stats.getGradedSubmits();
              Instant lastActivityAt = stats == null ? null : stats.getLastActivityAt();
              int challengesPassed =
                  Math.toIntExact(passedByUser.getOrDefault(user.getId(), 0L));
              int challengesStarted =
                  Math.toIntExact(startedByUser.getOrDefault(user.getId(), 0L));
              int completionPercent =
                  ChallengeProgressCalculator.completionPercent(challengesPassed, challengesStarted);
              return new AdminUserSummary(
                  user.getId(),
                  user.getEmail(),
                  user.getFullName(),
                  user.getRole().name(),
                  user.getCreatedAt(),
                  user.getDeletedAt() == null,
                  lastActivityAt,
                  submissionsTotal,
                  practiceRuns,
                  gradedSubmits,
                  challengesPassed,
                  challengesStarted,
                  completionPercent,
                  challengeQuotaService.platformDefaultMax(),
                  user.getMaxStartedChallenges(),
                  challengeQuotaService.effectiveChallengeLimit(user));
            })
        .sorted(
            Comparator.comparing(
                    AdminUserSummary::lastActivityAt,
                    Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(AdminUserSummary::email))
        .toList();
  }

  @Transactional
  public UserChallengeQuotaResponse updateChallengeQuota(
      UUID userId, UpdateUserChallengeQuotaRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    if (user.getRole() != UserRole.USER) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Exercise limits apply to learner accounts only");
    }
    user.setMaxStartedChallenges(request.maxStartedChallenges());
    user.setUpdatedAt(clock.instant());
    userRepository.save(user);
    return quotaResponse(user);
  }

  @Transactional(readOnly = true)
  public UserChallengeQuotaResponse challengeQuota(UUID userId) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    return quotaResponse(user);
  }

  private UserChallengeQuotaResponse quotaResponse(UserEntity user) {
    int started = challengeQuotaService.countStartedChallenges(user.getId());
    Integer effectiveLimit = challengeQuotaService.effectiveChallengeLimit(user);
    Integer remaining =
        effectiveLimit == null ? null : Math.max(0, effectiveLimit - started);
    return new UserChallengeQuotaResponse(
        user.getId(),
        challengeQuotaService.platformDefaultMax(),
        user.getMaxStartedChallenges(),
        effectiveLimit,
        started,
        remaining);
  }

  @Transactional
  public void deactivateUser(UUID actorId, UUID targetUserId) {
    if (actorId.equals(targetUserId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot deactivate your own account");
    }
    UserEntity user =
        userRepository
            .findById(targetUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    if (user.getDeletedAt() != null) {
      return;
    }
    if (user.getRole() == UserRole.ADMIN
        && userRepository.countByDeletedAtIsNullAndRoleAndIdNot(UserRole.ADMIN, targetUserId) == 0) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Cannot deactivate the last active administrator");
    }
    Instant now = clock.instant();
    user.markDeleted(now);
    user.setUpdatedAt(now);
    userRepository.save(user);
  }

  @Transactional
  public CreateUserResponse createUser(CreateUserRequest request) {
    String email = request.email().trim().toLowerCase();
    String fullName = request.fullName().trim();
    if (fullName.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
    }
    PasswordPolicy.validateTemporaryPassword(email, request.temporaryPassword());
    if (userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }
    Instant now = clock.instant();
    UserEntity user =
        new UserEntity(
            UUID.randomUUID(),
            email,
            passwordEncoder.encode(request.temporaryPassword()),
            request.role(),
            now,
            now,
            fullName,
            true);
    userRepository.save(user);
    boolean welcomeEmailSent =
        welcomeEmailSender.sendWelcomeEmail(
            user.getEmail(), user.getFullName(), request.temporaryPassword(), user.getRole().name());
    return new CreateUserResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        request.temporaryPassword(),
        user.getRole().name(),
        welcomeEmailSent);
  }

  private Map<UUID, UserSubmissionAggregate> loadSubmissionStats(List<UUID> userIds) {
    Map<UUID, UserSubmissionAggregate> stats = new HashMap<>();
    for (UserSubmissionAggregate row : submissionRepository.aggregateByUserIds(userIds)) {
      stats.put(row.getUserId(), row);
    }
    return stats;
  }

  private Map<UUID, Long> loadPassedCounts(List<UUID> userIds) {
    Map<UUID, Long> passed = new HashMap<>();
    for (UserPassedCountAggregate row :
        progressRepository.countPassedByUserIds(userIds, ProgressState.PASSED)) {
      passed.put(row.getUserId(), row.getPassedCount());
    }
    return passed;
  }

  private Map<UUID, Long> loadStartedCounts(List<UUID> userIds) {
    Map<UUID, Long> started = new HashMap<>();
    for (UserStartedCountAggregate row :
        submissionRepository.countStartedChallengesByUserIds(userIds)) {
      started.put(row.getUserId(), row.getStartedCount());
    }
    return started;
  }
}
