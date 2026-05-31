package com.codetraininglab.platform.persistence;

import java.time.Instant;
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
}
