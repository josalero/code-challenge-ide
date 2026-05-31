package com.codetraininglab.platform.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProgressRepository extends JpaRepository<UserProgressEntity, UUID> {

  Optional<UserProgressEntity> findByUserIdAndChallengeId(UUID userId, UUID challengeId);

  List<UserProgressEntity> findByUserId(UUID userId);
}
