package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, UUID> {

  @Query(
      """
      SELECT s FROM SubmissionEntity s
      WHERE s.userId = :userId AND s.idempotencyKey = :key AND s.createdAt >= :since
      """)
  Optional<SubmissionEntity> findIdempotent(
      @Param("userId") UUID userId, @Param("key") String key, @Param("since") Instant since);

  List<SubmissionEntity> findByStatusAndUpdatedAtBefore(SubmissionStatus status, Instant cutoff);

  List<SubmissionEntity> findByUserId(UUID userId);

  List<SubmissionEntity> findByUserIdAndChallengeIdOrderByCreatedAtDesc(
      UUID userId, UUID challengeId);

  @Query(
      """
      SELECT s.challengeId AS challengeId,
             SUM(CASE WHEN r.blocked = false THEN 1 ELSE 0 END) AS gradedPasses,
             SUM(CASE WHEN r.blocked = true THEN 1 ELSE 0 END) AS gradedFails
      FROM SubmissionEntity s
      INNER JOIN SubmissionReportEntity r ON r.submissionId = s.id
      WHERE s.userId = :userId AND s.kind = com.codetraininglab.domain.SubmissionKind.SUBMIT
      GROUP BY s.challengeId
      """)
  List<UserChallengeGradedStats> gradedStatsByUserId(@Param("userId") UUID userId);

  @Query(
      """
      SELECT s.challengeId AS challengeId, COUNT(a) AS enhancementRequests
      FROM SubmissionEntity s
      INNER JOIN SubmissionFeedbackActionEntity a ON a.submissionId = s.id
      WHERE s.userId = :userId
      GROUP BY s.challengeId
      """)
  List<UserChallengeEnhancementStats> enhancementStatsByUserId(@Param("userId") UUID userId);

  @Query(
      """
      SELECT s.challengeId AS challengeId,
             COUNT(f) AS feedbackItems,
             SUM(CASE WHEN f.status <> com.codetraininglab.domain.FeedbackStatus.pass THEN 1 ELSE 0 END)
               AS feedbackWarnings
      FROM SubmissionEntity s
      INNER JOIN SubmissionReportEntity r ON r.submissionId = s.id
      INNER JOIN FeedbackItemEntity f ON f.reportId = r.id
      WHERE s.userId = :userId AND s.kind = com.codetraininglab.domain.SubmissionKind.SUBMIT
      GROUP BY s.challengeId
      """)
  List<UserChallengeFeedbackStats> feedbackStatsByUserId(@Param("userId") UUID userId);

  long countByUserId(UUID userId);

  boolean existsByUserIdAndChallengeId(UUID userId, UUID challengeId);

  long countByUserIdAndKind(UUID userId, SubmissionKind kind);

  long countByUserIdAndStatus(UUID userId, SubmissionStatus status);

  long countByStatus(SubmissionStatus status);

  @Query(
      """
      SELECT s.userId AS userId,
             COUNT(s) AS totalSubmissions,
             SUM(CASE WHEN s.kind = com.codetraininglab.domain.SubmissionKind.RUN THEN 1 ELSE 0 END) AS practiceRuns,
             SUM(CASE WHEN s.kind = com.codetraininglab.domain.SubmissionKind.SUBMIT THEN 1 ELSE 0 END) AS gradedSubmits,
             MAX(s.updatedAt) AS lastActivityAt
      FROM SubmissionEntity s
      WHERE s.userId IN :userIds
      GROUP BY s.userId
      """)
  List<UserSubmissionAggregate> aggregateByUserIds(@Param("userIds") Collection<UUID> userIds);

  @Query(
      value =
          """
          SELECT user_id AS userId, COUNT(DISTINCT challenge_id) AS startedCount
          FROM (
            SELECT user_id, challenge_id
            FROM user_progress
            WHERE user_id IN (:userIds) AND state <> 'NOT_STARTED'
            UNION
            SELECT user_id, challenge_id
            FROM submissions
            WHERE user_id IN (:userIds)
          ) started_challenges
          GROUP BY user_id
          """,
      nativeQuery = true)
  List<UserStartedCountAggregate> countStartedChallengesByUserIds(
      @Param("userIds") Collection<UUID> userIds);
}
