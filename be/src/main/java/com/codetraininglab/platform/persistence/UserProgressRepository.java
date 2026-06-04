package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.ProgressState;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserProgressRepository extends JpaRepository<UserProgressEntity, UUID> {

  Optional<UserProgressEntity> findByUserIdAndChallengeId(UUID userId, UUID challengeId);

  List<UserProgressEntity> findByUserId(UUID userId);

  @Query(
      """
      SELECT p.userId AS userId, COUNT(p) AS passedCount
      FROM UserProgressEntity p
      WHERE p.state = :state AND p.userId IN :userIds
      GROUP BY p.userId
      """)
  List<UserPassedCountAggregate> countPassedByUserIds(
      @Param("userIds") Collection<UUID> userIds, @Param("state") ProgressState state);
}
