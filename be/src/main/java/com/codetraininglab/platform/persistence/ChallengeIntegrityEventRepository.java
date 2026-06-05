package com.codetraininglab.platform.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeIntegrityEventRepository
    extends JpaRepository<ChallengeIntegrityEventEntity, UUID> {

  @Query(
      """
      SELECT e.challengeId AS challengeId,
             SUM(CASE WHEN e.eventType = com.codetraininglab.domain.IntegrityEventType.COPY THEN 1 ELSE 0 END)
               AS copyAttempts,
             SUM(CASE WHEN e.eventType = com.codetraininglab.domain.IntegrityEventType.PASTE THEN 1 ELSE 0 END)
               AS pasteAttempts,
             SUM(CASE WHEN e.eventType = com.codetraininglab.domain.IntegrityEventType.CUT THEN 1 ELSE 0 END)
               AS cutAttempts,
             SUM(CASE WHEN e.eventType = com.codetraininglab.domain.IntegrityEventType.TAB_HIDDEN THEN 1 ELSE 0 END)
               AS tabHiddenCount,
             SUM(CASE WHEN e.eventType = com.codetraininglab.domain.IntegrityEventType.WINDOW_BLUR THEN 1 ELSE 0 END)
               AS windowBlurCount,
             SUM(CASE WHEN e.eventType = com.codetraininglab.domain.IntegrityEventType.LARGE_EDIT THEN 1 ELSE 0 END)
               AS largeEditCount,
             SUM(COALESCE(e.awayMs, 0)) AS totalAwayMs
      FROM ChallengeIntegrityEventEntity e
      WHERE e.userId = :userId
      GROUP BY e.challengeId
      """)
  List<UserChallengeIntegrityStats> statsByUserId(@Param("userId") UUID userId);

  List<ChallengeIntegrityEventEntity> findByUserIdAndChallengeIdOrderByOccurredAtDesc(
      UUID userId, UUID challengeId);
}
