package com.codetraininglab.identity.application;

import com.codetraininglab.catalog.application.ChallengeQuotaService;
import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeMetricsService {

  private final ChallengeRepository challengeRepository;
  private final UserProgressRepository progressRepository;
  private final SubmissionRepository submissionRepository;
  private final ChallengeQuotaService challengeQuotaService;

  public MeMetricsService(
      ChallengeRepository challengeRepository,
      UserProgressRepository progressRepository,
      SubmissionRepository submissionRepository,
      ChallengeQuotaService challengeQuotaService) {
    this.challengeRepository = challengeRepository;
    this.progressRepository = progressRepository;
    this.submissionRepository = submissionRepository;
    this.challengeQuotaService = challengeQuotaService;
  }

  @Transactional(readOnly = true)
  public MeMetricsResponse metricsForUser(UUID userId) {
    List<ChallengeEntity> catalog = challengeRepository.findAll();
    Map<UUID, ProgressState> progressByChallenge = new HashMap<>();
    for (UserProgressEntity row : progressRepository.findByUserId(userId)) {
      progressByChallenge.put(row.getChallengeId(), row.getState());
    }

    int passed = 0;
    int attempted = 0;
    int failed = 0;
    Map<String, BucketAccumulator> byLanguage = new HashMap<>();
    Map<String, BucketAccumulator> byDifficulty = new HashMap<>();

    for (ChallengeEntity challenge : catalog) {
      ProgressState state =
          progressByChallenge.getOrDefault(challenge.getId(), ProgressState.NOT_STARTED);
      switch (state) {
        case PASSED -> passed++;
        case ATTEMPTED -> attempted++;
        case FAILED -> failed++;
        default -> {
          /* NOT_STARTED */
        }
      }
      String language = normalizeLabel(challenge.getLanguage());
      String difficulty = normalizeLabel(challenge.getDifficulty());
      byLanguage.computeIfAbsent(language, ignored -> new BucketAccumulator()).add(state);
      byDifficulty.computeIfAbsent(difficulty, ignored -> new BucketAccumulator()).add(state);
    }

    int catalogTotal = catalog.size();
    int notStarted = Math.max(catalogTotal - passed - attempted - failed, 0);
    int challengesStarted = challengeQuotaService.countStartedChallenges(userId);
    var quota = challengeQuotaService.quotaForUser(userId);
    int completionPercent =
        ChallengeProgressCalculator.completionPercent(passed, challengesStarted);

    long submissionsTotal = submissionRepository.countByUserId(userId);
    long practiceRuns = submissionRepository.countByUserIdAndKind(userId, SubmissionKind.RUN);
    long gradedSubmits = submissionRepository.countByUserIdAndKind(userId, SubmissionKind.SUBMIT);
    long submissionsCompleted =
        submissionRepository.countByUserIdAndStatus(userId, SubmissionStatus.COMPLETED);
    long submissionsFailed =
        submissionRepository.countByUserIdAndStatus(userId, SubmissionStatus.FAILED);

    return new MeMetricsResponse(
        catalogTotal,
        challengesStarted,
        quota.maxStartedChallenges(),
        quota.challengesRemaining(),
        notStarted,
        attempted,
        passed,
        failed,
        completionPercent,
        submissionsTotal,
        practiceRuns,
        gradedSubmits,
        submissionsCompleted,
        submissionsFailed,
        toBreakdown(byLanguage),
        toBreakdown(byDifficulty));
  }

  private static List<MetricsBreakdownRow> toBreakdown(Map<String, BucketAccumulator> buckets) {
    return buckets.entrySet().stream()
        .map(
            entry ->
                new MetricsBreakdownRow(
                    entry.getKey(),
                    entry.getValue().total(),
                    entry.getValue().passed(),
                    entry.getValue().inProgress(),
                    entry.getValue().notStarted()))
        .sorted(Comparator.comparing(MetricsBreakdownRow::label))
        .toList();
  }

  private static String normalizeLabel(String value) {
    if (value == null || value.isBlank()) {
      return "unknown";
    }
    return value.trim().toLowerCase();
  }

  public record MeMetricsResponse(
      int catalogTotal,
      int challengesStarted,
      Integer maxStartedChallenges,
      Integer challengesRemaining,
      int notStarted,
      int attempted,
      int passed,
      int failed,
      int completionPercent,
      long submissionsTotal,
      long practiceRuns,
      long gradedSubmits,
      long submissionsCompleted,
      long submissionsFailed,
      List<MetricsBreakdownRow> byLanguage,
      List<MetricsBreakdownRow> byDifficulty) {}

  public record MetricsBreakdownRow(
      String label, int total, int passed, int inProgress, int notStarted) {}

  private static final class BucketAccumulator {
    private int total;
    private int passed;
    private int inProgress;
    private int notStarted;

    void add(ProgressState state) {
      total++;
      switch (state) {
        case PASSED -> passed++;
        case ATTEMPTED, FAILED -> inProgress++;
        default -> notStarted++;
      }
    }

    int total() {
      return total;
    }

    int passed() {
      return passed;
    }

    int inProgress() {
      return inProgress;
    }

    int notStarted() {
      return notStarted;
    }
  }
}
