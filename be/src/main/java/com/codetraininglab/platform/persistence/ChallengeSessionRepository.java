package com.codetraininglab.platform.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeSessionRepository extends JpaRepository<ChallengeSessionEntity, UUID> {

  Optional<ChallengeSessionEntity> findByUserIdAndChallengeIdAndEndedAtIsNull(
      UUID userId, UUID challengeId);
}
