package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.FeedbackActionStatus;
import com.codetraininglab.domain.FeedbackActionType;
import com.codetraininglab.domain.FeedbackCategory;
import com.codetraininglab.domain.FeedbackStatus;
import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.FeedbackItemEntity;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionFeedbackActionEntity;
import com.codetraininglab.platform.persistence.SubmissionFeedbackActionRepository;
import com.codetraininglab.platform.persistence.SubmissionReportEntity;
import com.codetraininglab.platform.persistence.SubmissionReportRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
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
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class AdminUserChallengeDetailServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private ChallengeRepository challengeRepository;
  @Mock private UserProgressRepository progressRepository;
  @Mock private SubmissionRepository submissionRepository;
  @Mock private SubmissionReportRepository reportRepository;
  @Mock private FeedbackItemRepository feedbackItemRepository;
  @Mock private SubmissionFeedbackActionRepository feedbackActionRepository;
  @Mock private LanguageRuntimeRepository languageRuntimeRepository;

  private AdminUserChallengeDetailService service;
  private UUID userId;
  private UUID challengeId;
  private UUID runtimeId;
  private UUID submissionId;
  private UUID reportId;
  private Instant now;

  private static final String BCRYPT_HASH = new BCryptPasswordEncoder(12).encode("TempPass1");

  @BeforeEach
  void setUp() {
    now = Instant.parse("2026-06-04T12:00:00Z");
    Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    service =
        new AdminUserChallengeDetailService(
            userRepository,
            challengeRepository,
            progressRepository,
            submissionRepository,
            reportRepository,
            feedbackItemRepository,
            feedbackActionRepository,
            languageRuntimeRepository,
            JsonMapper.builder().build(),
            clock);
    userId = UUID.randomUUID();
    challengeId = UUID.randomUUID();
    runtimeId = UUID.randomUUID();
    submissionId = UUID.randomUUID();
    reportId = UUID.randomUUID();
  }

  @Test
  void detailForUserChallengeReturnsSubmissionsFeedbackAndCoachActions() {
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
    SubmissionEntity submission =
        new SubmissionEntity(
            submissionId,
            userId,
            challengeId,
            runtimeId,
            SubmissionStatus.COMPLETED,
            SubmissionKind.SUBMIT,
            "class Solution {}",
            null,
            null,
            now.minusSeconds(300),
            now.minusSeconds(60));
    SubmissionReportEntity report =
        new SubmissionReportEntity(reportId, submissionId, 1, "{\"logs\":{}}", true, now);
    FeedbackItemEntity feedbackItem =
        new FeedbackItemEntity(
            UUID.randomUUID(),
            reportId,
            FeedbackCategory.CORRECTNESS,
            FeedbackStatus.fail,
            "medium",
            "Expected 3",
            "stable-1",
            now);
    feedbackItem.setAiExplanation("AI hint text");
    SubmissionFeedbackActionEntity coachAction =
        new SubmissionFeedbackActionEntity(
            UUID.randomUUID(),
            submissionId,
            FeedbackActionType.COACH,
            FeedbackActionStatus.COMPLETED,
            now.minusSeconds(30));
    coachAction.setResult("## Coach review\nTry a hash map.");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));
    when(progressRepository.findByUserIdAndChallengeId(userId, challengeId))
        .thenReturn(Optional.of(progress));
    when(submissionRepository.findByUserIdAndChallengeIdOrderByCreatedAtDesc(userId, challengeId))
        .thenReturn(List.of(submission));
    when(languageRuntimeRepository.findById(runtimeId))
        .thenReturn(Optional.of(new LanguageRuntimeEntity(runtimeId, UUID.randomUUID(), "21", "img", true)));
    when(reportRepository.findBySubmissionId(submissionId)).thenReturn(Optional.of(report));
    when(feedbackItemRepository.findByReportIdOrderByCategoryAsc(reportId))
        .thenReturn(List.of(feedbackItem));
    when(feedbackActionRepository.findBySubmissionIdOrderByCreatedAtDesc(submissionId))
        .thenReturn(List.of(coachAction));

    var response = service.detailForUserChallenge(userId, "two-sum");

    assertThat(response.user().email()).isEqualTo("learner@test.com");
    assertThat(response.stats().challengeSlug()).isEqualTo("two-sum");
    assertThat(response.stats().engagementStatus()).isEqualTo("IN_PROGRESS");
    assertThat(response.submissions()).hasSize(1);
    assertThat(response.submissions().getFirst().runtimeVersion()).isEqualTo("21");
    assertThat(response.submissions().getFirst().report().feedback()).hasSize(1);
    assertThat(response.submissions().getFirst().report().feedback().getFirst().aiExplanation())
        .isEqualTo("AI hint text");
    assertThat(response.submissions().getFirst().feedbackActions()).hasSize(1);
    assertThat(response.submissions().getFirst().feedbackActions().getFirst().action()).isEqualTo("COACH");
    assertThat(response.submissions().getFirst().feedbackActions().getFirst().result())
        .contains("hash map");
  }

  @Test
  void detailForUserChallengeRejectsNotStartedChallenge() {
    UserEntity user =
        new UserEntity(
            userId,
            "learner@test.com",
            BCRYPT_HASH,
            UserRole.USER,
            now,
            now,
            "Ada",
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

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));
    when(progressRepository.findByUserIdAndChallengeId(userId, challengeId))
        .thenReturn(Optional.empty());
    when(submissionRepository.findByUserIdAndChallengeIdOrderByCreatedAtDesc(userId, challengeId))
        .thenReturn(List.of());

    assertThatThrownBy(() -> service.detailForUserChallenge(userId, "two-sum"))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
  }
}
