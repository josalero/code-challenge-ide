package com.codetraininglab.platform.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomTestsRepository extends JpaRepository<CustomTestsEntity, UUID> {

  Optional<CustomTestsEntity> findByUserIdAndChallengeId(UUID userId, UUID challengeId);
}
