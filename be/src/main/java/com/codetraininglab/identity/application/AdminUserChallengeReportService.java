package com.codetraininglab.identity.application;

import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.identity.api.AdminUserChallengeReportResponse;
import com.codetraininglab.identity.api.AdminUserChallengeReportResponse.ChallengeRow;
import com.codetraininglab.identity.api.AdminUserChallengeReportResponse.Summary;
import com.codetraininglab.identity.api.AdminUserChallengeReportResponse.UserHeader;
import com.codetraininglab.platform.persistence.ChallengeIntegrityEventRepository;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserChallengeIntegrityStats;
import com.codetraininglab.platform.persistence.UserChallengeEnhancementStats;
import com.codetraininglab.platform.persistence.UserChallengeFeedbackStats;
import com.codetraininglab.platform.persistence.UserChallengeGradedStats;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminUserChallengeReportService {

  static final long ABANDON_INACTIVITY_DAYS = 14;

  private final UserRepository userRepository;
  private final ChallengeRepository challengeRepository;
  private final UserProgressRepository progressRepository;
  private final SubmissionRepository submissionRepository;
  private final ChallengeIntegrityEventRepository integrityEventRepository;
  private final Clock clock;

  public AdminUserChallengeReportService(
      UserRepository userRepository,
      ChallengeRepository challengeRepository,
      UserProgressRepository progressRepository,
      SubmissionRepository submissionRepository,
      ChallengeIntegrityEventRepository integrityEventRepository,
      Clock clock) {
    this.userRepository = userRepository;
    this.challengeRepository = challengeRepository;
    this.progressRepository = progressRepository;
    this.submissionRepository = submissionRepository;
    this.integrityEventRepository = integrityEventRepository;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public AdminUserChallengeReportResponse reportForUser(UUID userId) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    List<ChallengeEntity> catalog = challengeRepository.findAll();
    Map<UUID, UserProgressEntity> progressByChallenge = loadProgress(userId);
    Map<UUID, SubmissionBucket> submissionByChallenge = loadSubmissionBuckets(userId);
    Map<UUID, GradedBucket> gradedByChallenge = loadGradedBuckets(userId);
    Map<UUID, Long> enhancementsByChallenge = loadEnhancementCounts(userId);
    Map<UUID, FeedbackBucket> feedbackByChallenge = loadFeedbackBuckets(userId);
    Map<UUID, IntegrityBucket> integrityByChallenge = loadIntegrityBuckets(userId);

    int passed = 0;
    int attempted = 0;
    int failed = 0;
    int likelyAbandoned = 0;
    long practiceRunsTotal = 0;
    long gradedSubmitsTotal = 0;
    long gradedPassesTotal = 0;

    List<ChallengeRow> rows = new ArrayList<>();
    for (ChallengeEntity challenge : catalog) {
      UserProgressEntity progress = progressByChallenge.get(challenge.getId());
      ProgressState state =
          progress == null ? ProgressState.NOT_STARTED : progress.getState();
      switch (state) {
        case PASSED -> passed++;
        case ATTEMPTED -> attempted++;
        case FAILED -> failed++;
        default -> {
          /* NOT_STARTED */
        }
      }

      SubmissionBucket submissions = submissionByChallenge.getOrDefault(challenge.getId(), SubmissionBucket.EMPTY);
      if (!isStarted(state, submissions)) {
        continue;
      }

      GradedBucket graded = gradedByChallenge.getOrDefault(challenge.getId(), GradedBucket.EMPTY);
      FeedbackBucket feedback = feedbackByChallenge.getOrDefault(challenge.getId(), FeedbackBucket.EMPTY);
      IntegrityBucket integrity =
          integrityByChallenge.getOrDefault(challenge.getId(), IntegrityBucket.EMPTY);
      long enhancements = enhancementsByChallenge.getOrDefault(challenge.getId(), 0L);

      practiceRunsTotal += submissions.practiceRuns();
      gradedSubmitsTotal += submissions.gradedSubmits();
      gradedPassesTotal += graded.passes();

      boolean likelyAbandonedFlag =
          isLikelyAbandoned(state, submissions, clock.instant());
      if (likelyAbandonedFlag) {
        likelyAbandoned++;
      }

      String engagementStatus = engagementStatus(state, submissions, likelyAbandonedFlag);
      Integer passRatePercent =
          submissions.gradedSubmits() == 0
              ? null
              : Math.round((graded.passes() * 100f) / submissions.gradedSubmits());
      Long timeToPassMs =
          state == ProgressState.PASSED && progress != null && progress.getSubmittedAt() != null
              ? durationMs(submissions.firstActivityAt(), progress.getSubmittedAt())
              : null;

      rows.add(
          new ChallengeRow(
              challenge.getSlug(),
              challenge.getTitle(),
              challenge.getLanguage(),
              challenge.getDifficulty(),
              challenge.getSessionDurationMinutes(),
              state.name(),
              engagementStatus,
              progress != null && progress.isSubmitted(),
              progress == null ? null : progress.getSubmittedAt(),
              submissions.firstActivityAt(),
              submissions.lastActivityAt(),
              submissions.practiceRuns(),
              submissions.gradedSubmits(),
              graded.passes(),
              graded.fails(),
              passRatePercent,
              timeToPassMs,
              submissions.avgProcessingMs(),
              enhancements,
              feedback.items(),
              feedback.warnings(),
              submissions.cancelled(),
              likelyAbandonedFlag,
              integrity.copyAttempts(),
              integrity.pasteAttempts(),
              integrity.cutAttempts(),
              integrity.tabHiddenCount(),
              integrity.windowBlurCount(),
              integrity.largeEditCount(),
              integrity.totalAwayMs()));
    }

    rows.sort(
        Comparator.comparing(
                (ChallengeRow row) -> row.lastActivityAt(), Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(ChallengeRow::title));

    int catalogTotal = catalog.size();
    int started = rows.size();
    int notStarted = Math.max(catalogTotal - passed - attempted - failed, 0);
    int completionPercent = ChallengeProgressCalculator.completionPercent(passed, started);
    Integer gradedPassRatePercent =
        gradedSubmitsTotal == 0
            ? null
            : Math.round((gradedPassesTotal * 100f) / gradedSubmitsTotal);

    Summary summary =
        new Summary(
            catalogTotal,
            started,
            passed,
            attempted,
            failed,
            notStarted,
            completionPercent,
            likelyAbandoned,
            submissionRepository.countByUserId(userId),
            practiceRunsTotal,
            gradedSubmitsTotal,
            gradedPassRatePercent);

    UserHeader header =
        new UserHeader(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.getDeletedAt() == null);

    return new AdminUserChallengeReportResponse(header, summary, rows);
  }

  private Map<UUID, UserProgressEntity> loadProgress(UUID userId) {
    Map<UUID, UserProgressEntity> progress = new HashMap<>();
    for (UserProgressEntity row : progressRepository.findByUserId(userId)) {
      progress.put(row.getChallengeId(), row);
    }
    return progress;
  }

  private Map<UUID, SubmissionBucket> loadSubmissionBuckets(UUID userId) {
    Map<UUID, SubmissionBucket> buckets = new HashMap<>();
    for (SubmissionEntity submission : submissionRepository.findByUserId(userId)) {
      buckets
          .computeIfAbsent(submission.getChallengeId(), ignored -> new SubmissionBucket())
          .add(submission);
    }
    for (SubmissionBucket bucket : buckets.values()) {
      bucket.finalizeAvgProcessingMs();
    }
    return buckets;
  }

  private Map<UUID, GradedBucket> loadGradedBuckets(UUID userId) {
    Map<UUID, GradedBucket> buckets = new HashMap<>();
    for (UserChallengeGradedStats row : submissionRepository.gradedStatsByUserId(userId)) {
      buckets.put(row.getChallengeId(), new GradedBucket(row.getGradedPasses(), row.getGradedFails()));
    }
    return buckets;
  }

  private Map<UUID, Long> loadEnhancementCounts(UUID userId) {
    Map<UUID, Long> counts = new HashMap<>();
    for (UserChallengeEnhancementStats row : submissionRepository.enhancementStatsByUserId(userId)) {
      counts.put(row.getChallengeId(), row.getEnhancementRequests());
    }
    return counts;
  }

  private Map<UUID, FeedbackBucket> loadFeedbackBuckets(UUID userId) {
    Map<UUID, FeedbackBucket> buckets = new HashMap<>();
    for (UserChallengeFeedbackStats row : submissionRepository.feedbackStatsByUserId(userId)) {
      buckets.put(row.getChallengeId(), new FeedbackBucket(row.getFeedbackItems(), row.getFeedbackWarnings()));
    }
    return buckets;
  }

  private Map<UUID, IntegrityBucket> loadIntegrityBuckets(UUID userId) {
    Map<UUID, IntegrityBucket> buckets = new HashMap<>();
    for (UserChallengeIntegrityStats row : integrityEventRepository.statsByUserId(userId)) {
      buckets.put(
          row.getChallengeId(),
          new IntegrityBucket(
              row.getCopyAttempts(),
              row.getPasteAttempts(),
              row.getCutAttempts(),
              row.getTabHiddenCount(),
              row.getWindowBlurCount(),
              row.getLargeEditCount(),
              row.getTotalAwayMs()));
    }
    return buckets;
  }

  static boolean isStarted(ProgressState state, SubmissionBucket submissions) {
    return submissions.hasActivity() || state != ProgressState.NOT_STARTED;
  }

  static String engagementStatus(
      ProgressState state, SubmissionBucket submissions, boolean likelyAbandoned) {
    if (likelyAbandoned) {
      return "LIKELY_ABANDONED";
    }
    return switch (state) {
      case PASSED -> "PASSED";
      case FAILED -> "FAILED";
      case ATTEMPTED -> "IN_PROGRESS";
      case NOT_STARTED ->
          submissions.hasActivity() ? "IN_PROGRESS" : "NOT_STARTED";
    };
  }

  static boolean isLikelyAbandoned(
      ProgressState state, SubmissionBucket submissions, Instant now) {
    if (state == ProgressState.PASSED || state == ProgressState.FAILED) {
      return false;
    }
    if (!submissions.hasActivity()) {
      return false;
    }
    if (submissions.gradedSubmits() > 0) {
      return false;
    }
    if (submissions.lastActivityAt() == null) {
      return false;
    }
    Duration inactiveFor = Duration.between(submissions.lastActivityAt(), now);
    return inactiveFor.toDays() >= ABANDON_INACTIVITY_DAYS;
  }

  private static Long durationMs(Instant start, Instant end) {
    if (start == null || end == null || end.isBefore(start)) {
      return null;
    }
    return Duration.between(start, end).toMillis();
  }

  static final class SubmissionBucket {
    static final SubmissionBucket EMPTY = new SubmissionBucket();

    private long practiceRuns;
    private long gradedSubmits;
    private long cancelled;
    private Instant firstActivityAt;
    private Instant lastActivityAt;
    private long processingTotalMs;
    private long processingCount;

    void add(SubmissionEntity submission) {
      if (submission.getKind() == SubmissionKind.RUN) {
        practiceRuns++;
      } else {
        gradedSubmits++;
      }
      if (submission.getStatus() == SubmissionStatus.CANCELLED) {
        cancelled++;
      }
      firstActivityAt =
          firstActivityAt == null || submission.getCreatedAt().isBefore(firstActivityAt)
              ? submission.getCreatedAt()
              : firstActivityAt;
      lastActivityAt =
          lastActivityAt == null || submission.getUpdatedAt().isAfter(lastActivityAt)
              ? submission.getUpdatedAt()
              : lastActivityAt;
      if (submission.getStatus() == SubmissionStatus.COMPLETED
          || submission.getStatus() == SubmissionStatus.FAILED) {
        processingTotalMs +=
            Duration.between(submission.getCreatedAt(), submission.getUpdatedAt()).toMillis();
        processingCount++;
      }
    }

    void finalizeAvgProcessingMs() {
      // computed on read via avgProcessingMs()
    }

    long practiceRuns() {
      return practiceRuns;
    }

    long gradedSubmits() {
      return gradedSubmits;
    }

    long cancelled() {
      return cancelled;
    }

    Instant firstActivityAt() {
      return firstActivityAt;
    }

    Instant lastActivityAt() {
      return lastActivityAt;
    }

    Long avgProcessingMs() {
      if (processingCount == 0) {
        return null;
      }
      return processingTotalMs / processingCount;
    }

    boolean hasActivity() {
      return firstActivityAt != null;
    }
  }

  record GradedBucket(long passes, long fails) {
    static final GradedBucket EMPTY = new GradedBucket(0, 0);
  }

  record FeedbackBucket(long items, long warnings) {
    static final FeedbackBucket EMPTY = new FeedbackBucket(0, 0);
  }

  record IntegrityBucket(
      long copyAttempts,
      long pasteAttempts,
      long cutAttempts,
      long tabHiddenCount,
      long windowBlurCount,
      long largeEditCount,
      long totalAwayMs) {
    static final IntegrityBucket EMPTY = new IntegrityBucket(0, 0, 0, 0, 0, 0, 0);
  }
}
