package com.codetraininglab.identity.application;

import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.feedback.api.FeedbackActionResponse;
import com.codetraininglab.identity.api.AdminUserChallengeDetailResponse;
import com.codetraininglab.identity.api.AdminUserChallengeDetailResponse.ReportDetail;
import com.codetraininglab.identity.api.AdminUserChallengeDetailResponse.SubmissionDetail;
import com.codetraininglab.identity.api.AdminUserChallengeReportResponse;
import com.codetraininglab.identity.api.AdminUserChallengeReportResponse.ChallengeRow;
import com.codetraininglab.identity.api.AdminUserChallengeReportResponse.UserHeader;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.FeedbackItemEntity;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionFeedbackActionRepository;
import com.codetraininglab.platform.persistence.SubmissionReportEntity;
import com.codetraininglab.platform.persistence.SubmissionReportRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.submission.api.FeedbackItemResponse;
import com.codetraininglab.submission.application.ReportSummarySupport;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class AdminUserChallengeDetailService {

  private final UserRepository userRepository;
  private final ChallengeRepository challengeRepository;
  private final UserProgressRepository progressRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionReportRepository reportRepository;
  private final FeedbackItemRepository feedbackItemRepository;
  private final SubmissionFeedbackActionRepository feedbackActionRepository;
  private final LanguageRuntimeRepository languageRuntimeRepository;
  private final JsonMapper jsonMapper;
  private final Clock clock;

  public AdminUserChallengeDetailService(
      UserRepository userRepository,
      ChallengeRepository challengeRepository,
      UserProgressRepository progressRepository,
      SubmissionRepository submissionRepository,
      SubmissionReportRepository reportRepository,
      FeedbackItemRepository feedbackItemRepository,
      SubmissionFeedbackActionRepository feedbackActionRepository,
      LanguageRuntimeRepository languageRuntimeRepository,
      JsonMapper jsonMapper,
      Clock clock) {
    this.userRepository = userRepository;
    this.challengeRepository = challengeRepository;
    this.progressRepository = progressRepository;
    this.submissionRepository = submissionRepository;
    this.reportRepository = reportRepository;
    this.feedbackItemRepository = feedbackItemRepository;
    this.feedbackActionRepository = feedbackActionRepository;
    this.languageRuntimeRepository = languageRuntimeRepository;
    this.jsonMapper = jsonMapper;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public AdminUserChallengeDetailResponse detailForUserChallenge(UUID userId, String challengeSlug) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    ChallengeEntity challenge =
        challengeRepository
            .findBySlug(challengeSlug)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));

    UserProgressEntity progress =
        progressRepository.findByUserIdAndChallengeId(userId, challenge.getId()).orElse(null);
    ProgressState state =
        progress == null ? ProgressState.NOT_STARTED : progress.getState();

    List<SubmissionEntity> submissions =
        submissionRepository.findByUserIdAndChallengeIdOrderByCreatedAtDesc(
            userId, challenge.getId());

    AdminUserChallengeReportService.SubmissionBucket bucket =
        new AdminUserChallengeReportService.SubmissionBucket();
    for (SubmissionEntity submission : submissions) {
      bucket.add(submission);
    }

    if (!AdminUserChallengeReportService.isStarted(state, bucket)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Challenge not started by this user");
    }

    long gradedPasses = 0;
    long gradedFails = 0;
    long feedbackItems = 0;
    long feedbackWarnings = 0;
    long enhancementRequests = 0;

    Map<UUID, String> runtimeVersions = loadRuntimeVersions(submissions);
    List<SubmissionDetail> submissionDetails = new ArrayList<>();

    for (SubmissionEntity submission : submissions) {
      List<FeedbackActionResponse> feedbackActions =
          feedbackActionRepository.findBySubmissionIdOrderByCreatedAtDesc(submission.getId()).stream()
              .map(FeedbackActionResponse::from)
              .toList();
      enhancementRequests += feedbackActions.size();

      ReportDetail reportDetail = buildReportDetail(submission);
      if (reportDetail != null) {
        feedbackItems += reportDetail.feedback().size();
        feedbackWarnings +=
            reportDetail.feedback().stream().filter(item -> !"pass".equals(item.status())).count();
        if (submission.getKind() == SubmissionKind.SUBMIT) {
          if (reportDetail.blocked()) {
            gradedFails++;
          } else {
            gradedPasses++;
          }
        }
      }

      submissionDetails.add(
          new SubmissionDetail(
              submission.getId(),
              submission.getKind().name(),
              submission.getStatus().name(),
              runtimeVersions.get(submission.getRuntimeId()),
              submission.getCreatedAt(),
              submission.getUpdatedAt(),
              processingMs(submission),
              submission.getSolutionCode(),
              submission.getCustomTestsCode(),
              reportDetail,
              feedbackActions));
    }

    boolean likelyAbandonedFlag =
        AdminUserChallengeReportService.isLikelyAbandoned(state, bucket, clock.instant());
    String engagementStatus =
        AdminUserChallengeReportService.engagementStatus(state, bucket, likelyAbandonedFlag);
    Integer passRatePercent =
        bucket.gradedSubmits() == 0
            ? null
            : Math.round((gradedPasses * 100f) / bucket.gradedSubmits());
    Long timeToPassMs =
        state == ProgressState.PASSED && progress != null && progress.getSubmittedAt() != null
            ? durationMs(bucket.firstActivityAt(), progress.getSubmittedAt())
            : null;

    ChallengeRow stats =
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
            bucket.firstActivityAt(),
            bucket.lastActivityAt(),
            bucket.practiceRuns(),
            bucket.gradedSubmits(),
            gradedPasses,
            gradedFails,
            passRatePercent,
            timeToPassMs,
            bucket.avgProcessingMs(),
            enhancementRequests,
            feedbackItems,
            feedbackWarnings,
            bucket.cancelled(),
            likelyAbandonedFlag);

    UserHeader header =
        new UserHeader(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.getDeletedAt() == null);

    return new AdminUserChallengeDetailResponse(header, stats, submissionDetails);
  }

  private ReportDetail buildReportDetail(SubmissionEntity submission) {
    return reportRepository
        .findBySubmissionId(submission.getId())
        .map(
            report -> {
              List<FeedbackItemResponse> feedback =
                  feedbackItemRepository.findByReportIdOrderByCategoryAsc(report.getId()).stream()
                      .map(AdminUserChallengeDetailService::toFeedbackResponse)
                      .toList();
              return new ReportDetail(
                  report.getId(),
                  report.isBlocked(),
                  report.getSummary(),
                  ReportSummarySupport.parseRunnerLogs(report.getSummary(), jsonMapper),
                  feedback);
            })
        .orElse(null);
  }

  private Map<UUID, String> loadRuntimeVersions(List<SubmissionEntity> submissions) {
    Map<UUID, String> versions = new HashMap<>();
    for (SubmissionEntity submission : submissions) {
      UUID runtimeId = submission.getRuntimeId();
      if (!versions.containsKey(runtimeId)) {
        languageRuntimeRepository
            .findById(runtimeId)
            .map(LanguageRuntimeEntity::getVersion)
            .ifPresent(version -> versions.put(runtimeId, version));
      }
    }
    return versions;
  }

  private static FeedbackItemResponse toFeedbackResponse(FeedbackItemEntity item) {
    return new FeedbackItemResponse(
        item.getId(),
        item.getCategory().name(),
        item.getStatus().name(),
        item.getMessage(),
        item.getAiExplanation());
  }

  private static Long processingMs(SubmissionEntity submission) {
    if (submission.getStatus() != SubmissionStatus.COMPLETED
        && submission.getStatus() != SubmissionStatus.FAILED) {
      return null;
    }
    return Duration.between(submission.getCreatedAt(), submission.getUpdatedAt()).toMillis();
  }

  private static Long durationMs(Instant start, Instant end) {
    if (start == null || end == null || end.isBefore(start)) {
      return null;
    }
    return Duration.between(start, end).toMillis();
  }
}
