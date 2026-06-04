package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codetraininglab.catalog.application.ChallengeQuotaService;
import com.codetraininglab.catalog.application.ChallengeQuotaService.ChallengeQuotaSnapshot;
import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeMetricsServiceTest {

  @Mock private ChallengeRepository challengeRepository;
  @Mock private UserProgressRepository progressRepository;
  @Mock private SubmissionRepository submissionRepository;
  @Mock private ChallengeQuotaService challengeQuotaService;

  @InjectMocks private MeMetricsService service;

  private final UUID userId = UUID.randomUUID();

  @Test
  void aggregatesCatalogProgressAndSubmissions() {
    UUID javaChallenge = UUID.randomUUID();
    UUID reactChallenge = UUID.randomUUID();
    when(challengeRepository.findAll())
        .thenReturn(
            List.of(
                challenge(javaChallenge, "two-sum", "easy", "java"),
                challenge(reactChallenge, "accordion-react", "easy", "react")));
    when(progressRepository.findByUserId(userId))
        .thenReturn(
            List.of(
                progress(userId, javaChallenge, ProgressState.PASSED),
                progress(userId, reactChallenge, ProgressState.ATTEMPTED)));
    when(submissionRepository.countByUserId(userId)).thenReturn(12L);
    when(submissionRepository.countByUserIdAndKind(userId, SubmissionKind.RUN)).thenReturn(8L);
    when(submissionRepository.countByUserIdAndKind(userId, SubmissionKind.SUBMIT)).thenReturn(4L);
    when(submissionRepository.countByUserIdAndStatus(userId, SubmissionStatus.COMPLETED))
        .thenReturn(10L);
    when(submissionRepository.countByUserIdAndStatus(userId, SubmissionStatus.FAILED))
        .thenReturn(2L);
    when(challengeQuotaService.countStartedChallenges(userId)).thenReturn(2);
    when(challengeQuotaService.quotaForUser(userId)).thenReturn(new ChallengeQuotaSnapshot(5, 3));

    var metrics = service.metricsForUser(userId);

    assertThat(metrics.catalogTotal()).isEqualTo(2);
    assertThat(metrics.challengesStarted()).isEqualTo(2);
    assertThat(metrics.maxStartedChallenges()).isEqualTo(5);
    assertThat(metrics.challengesRemaining()).isEqualTo(3);
    assertThat(metrics.passed()).isEqualTo(1);
    assertThat(metrics.attempted()).isEqualTo(1);
    assertThat(metrics.notStarted()).isZero();
    assertThat(metrics.completionPercent()).isEqualTo(50);
    assertThat(metrics.submissionsTotal()).isEqualTo(12);
    assertThat(metrics.practiceRuns()).isEqualTo(8);
    assertThat(metrics.gradedSubmits()).isEqualTo(4);
    assertThat(metrics.byLanguage()).hasSize(2);
    assertThat(metrics.byDifficulty()).hasSize(1);
  }

  private static ChallengeEntity challenge(UUID id, String slug, String difficulty, String language) {
    return new ChallengeEntity(
        id,
        slug,
        "Title",
        "desc",
        "starter",
        "{}",
        "git",
        difficulty,
        language,
        null,
        Instant.EPOCH,
        Instant.EPOCH);
  }

  private static UserProgressEntity progress(
      UUID user, UUID challengeId, ProgressState state) {
    return new UserProgressEntity(
        UUID.randomUUID(), user, challengeId, state, Instant.EPOCH);
  }
}
