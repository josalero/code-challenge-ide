package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserChallengeEnhancementStats;
import com.codetraininglab.platform.persistence.UserChallengeFeedbackStats;
import com.codetraininglab.platform.persistence.UserChallengeGradedStats;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
class AdminUserChallengeReportServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private ChallengeRepository challengeRepository;
  @Mock private UserProgressRepository progressRepository;
  @Mock private SubmissionRepository submissionRepository;

  private AdminUserChallengeReportService service;
  private UUID userId;
  private UUID challengeId;
  private Instant now;

  private static final String BCRYPT_HASH = new BCryptPasswordEncoder(12).encode("TempPass1");

  @BeforeEach
  void setUp() {
    now = Instant.parse("2026-06-04T12:00:00Z");
    Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    service =
        new AdminUserChallengeReportService(
            userRepository,
            challengeRepository,
            progressRepository,
            submissionRepository,
            clock);
    userId = UUID.randomUUID();
    challengeId = UUID.randomUUID();
  }

  @Test
  void reportForUserBuildsChallengeRowsWithPassRateAndAbandonment() {
    UserEntity user =
        new UserEntity(
            userId,
            "learner@test.com",
            BCRYPT_HASH,
            UserRole.USER,
            now,
            now,
            "Ada Lovelace",
            false);
    ChallengeEntity challenge =
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
    UserProgressEntity progress =
        new UserProgressEntity(
            UUID.randomUUID(), userId, challengeId, ProgressState.ATTEMPTED, now.minusSeconds(3600));
    SubmissionEntity practiceRun =
        new SubmissionEntity(
            UUID.randomUUID(),
            userId,
            challengeId,
            UUID.randomUUID(),
            SubmissionStatus.COMPLETED,
            SubmissionKind.RUN,
            "solution",
            null,
            null,
            now.minusSeconds(20 * 24 * 3600L),
            now.minusSeconds(20 * 24 * 3600L - 120));
    SubmissionEntity gradedFail =
        new SubmissionEntity(
            UUID.randomUUID(),
            userId,
            challengeId,
            UUID.randomUUID(),
            SubmissionStatus.COMPLETED,
            SubmissionKind.SUBMIT,
            "solution",
            null,
            null,
            now.minusSeconds(19 * 24 * 3600L),
            now.minusSeconds(19 * 24 * 3600L - 300));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(challengeRepository.findAll()).thenReturn(List.of(challenge));
    when(progressRepository.findByUserId(userId)).thenReturn(List.of(progress));
    when(submissionRepository.findByUserId(userId)).thenReturn(List.of(practiceRun, gradedFail));
    when(submissionRepository.gradedStatsByUserId(userId))
        .thenReturn(
            List.of(
                new GradedStats(challengeId, 0, 1)));
    when(submissionRepository.enhancementStatsByUserId(userId))
        .thenReturn(List.of(new EnhancementStats(challengeId, 2)));
    when(submissionRepository.feedbackStatsByUserId(userId))
        .thenReturn(List.of(new FeedbackStats(challengeId, 3, 1)));
    when(submissionRepository.countByUserId(userId)).thenReturn(2L);

    var report = service.reportForUser(userId);

    assertThat(report.user().email()).isEqualTo("learner@test.com");
    assertThat(report.summary().catalogTotal()).isEqualTo(1);
    assertThat(report.summary().started()).isEqualTo(1);
    assertThat(report.summary().attempted()).isEqualTo(1);
    assertThat(report.summary().likelyAbandoned()).isZero();
    assertThat(report.summary().gradedPassRatePercent()).isZero();

    var row = report.challenges().getFirst();
    assertThat(row.challengeSlug()).isEqualTo("two-sum");
    assertThat(row.practiceRuns()).isEqualTo(1);
    assertThat(row.gradedSubmits()).isEqualTo(1);
    assertThat(row.passRatePercent()).isZero();
    assertThat(row.enhancementRequests()).isEqualTo(2);
    assertThat(row.feedbackItems()).isEqualTo(3);
    assertThat(row.feedbackWarnings()).isEqualTo(1);
    assertThat(row.engagementStatus()).isEqualTo("IN_PROGRESS");
    assertThat(row.avgProcessingMs()).isEqualTo(210_000L);
  }

  @Test
  void reportForUserFlagsLikelyAbandonedWhenInactiveWithoutGradedSubmit() {
    UserEntity user =
        new UserEntity(
            userId, "learner@test.com", BCRYPT_HASH, UserRole.USER, now, now, "Ada", false);
    ChallengeEntity challenge =
        new ChallengeEntity(
            challengeId, "two-sum", "Two Sum", "d", "c", "{}", "seed", "easy", "java", 45, now, now);
    UserProgressEntity progress =
        new UserProgressEntity(
            UUID.randomUUID(), userId, challengeId, ProgressState.ATTEMPTED, now.minusSeconds(60));
    SubmissionEntity practiceRun =
        new SubmissionEntity(
            UUID.randomUUID(),
            userId,
            challengeId,
            UUID.randomUUID(),
            SubmissionStatus.COMPLETED,
            SubmissionKind.RUN,
            "solution",
            null,
            null,
            now.minusSeconds(20 * 24 * 3600L),
            now.minusSeconds(20 * 24 * 3600L - 60));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(challengeRepository.findAll()).thenReturn(List.of(challenge));
    when(progressRepository.findByUserId(userId)).thenReturn(List.of(progress));
    when(submissionRepository.findByUserId(userId)).thenReturn(List.of(practiceRun));
    when(submissionRepository.gradedStatsByUserId(userId)).thenReturn(List.of());
    when(submissionRepository.enhancementStatsByUserId(userId)).thenReturn(List.of());
    when(submissionRepository.feedbackStatsByUserId(userId)).thenReturn(List.of());
    when(submissionRepository.countByUserId(userId)).thenReturn(1L);

    var report = service.reportForUser(userId);

    assertThat(report.summary().likelyAbandoned()).isEqualTo(1);
    assertThat(report.challenges().getFirst().likelyAbandoned()).isTrue();
    assertThat(report.challenges().getFirst().engagementStatus()).isEqualTo("LIKELY_ABANDONED");
  }

  @Test
  void reportForUserOmitsNotStartedChallengesFromRows() {
    UserEntity user =
        new UserEntity(
            userId, "learner@test.com", BCRYPT_HASH, UserRole.USER, now, now, "Ada", false);
    UUID startedId = UUID.randomUUID();
    UUID untouchedId = UUID.randomUUID();
    ChallengeEntity started =
        new ChallengeEntity(
            startedId, "two-sum", "Two Sum", "d", "c", "{}", "seed", "easy", "java", 45, now, now);
    ChallengeEntity untouched =
        new ChallengeEntity(
            untouchedId,
            "three-sum",
            "Three Sum",
            "d",
            "c",
            "{}",
            "seed",
            "medium",
            "java",
            45,
            now,
            now);
    UserProgressEntity progress =
        new UserProgressEntity(
            UUID.randomUUID(), userId, startedId, ProgressState.PASSED, now.minusSeconds(60));
    progress.setSubmittedAt(now.minusSeconds(30));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(challengeRepository.findAll()).thenReturn(List.of(started, untouched));
    when(progressRepository.findByUserId(userId)).thenReturn(List.of(progress));
    when(submissionRepository.findByUserId(userId)).thenReturn(List.of());
    when(submissionRepository.gradedStatsByUserId(userId)).thenReturn(List.of());
    when(submissionRepository.enhancementStatsByUserId(userId)).thenReturn(List.of());
    when(submissionRepository.feedbackStatsByUserId(userId)).thenReturn(List.of());
    when(submissionRepository.countByUserId(userId)).thenReturn(0L);

    var report = service.reportForUser(userId);

    assertThat(report.summary().catalogTotal()).isEqualTo(2);
    assertThat(report.summary().started()).isEqualTo(1);
    assertThat(report.summary().passed()).isEqualTo(1);
    assertThat(report.summary().completionPercent()).isEqualTo(100);
    assertThat(report.summary().notStarted()).isEqualTo(1);
    assertThat(report.challenges()).hasSize(1);
    assertThat(report.challenges().getFirst().challengeSlug()).isEqualTo("two-sum");
  }

  @Test
  void reportForUserRejectsMissingUser() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.reportForUser(userId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  private record GradedStats(UUID challengeId, long gradedPasses, long gradedFails)
      implements UserChallengeGradedStats {
    @Override
    public UUID getChallengeId() {
      return challengeId;
    }

    @Override
    public long getGradedPasses() {
      return gradedPasses;
    }

    @Override
    public long getGradedFails() {
      return gradedFails;
    }
  }

  private record EnhancementStats(UUID challengeId, long enhancementRequests)
      implements UserChallengeEnhancementStats {
    @Override
    public UUID getChallengeId() {
      return challengeId;
    }

    @Override
    public long getEnhancementRequests() {
      return enhancementRequests;
    }
  }

  private record FeedbackStats(UUID challengeId, long feedbackItems, long feedbackWarnings)
      implements UserChallengeFeedbackStats {
    @Override
    public UUID getChallengeId() {
      return challengeId;
    }

    @Override
    public long getFeedbackItems() {
      return feedbackItems;
    }

    @Override
    public long getFeedbackWarnings() {
      return feedbackWarnings;
    }
  }
}
