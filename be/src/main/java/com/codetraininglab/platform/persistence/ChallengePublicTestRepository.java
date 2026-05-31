package com.codetraininglab.platform.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengePublicTestRepository extends JpaRepository<ChallengePublicTestEntity, UUID> {

  List<ChallengePublicTestEntity> findByChallengeIdOrderBySortOrderAsc(UUID challengeId);
}
