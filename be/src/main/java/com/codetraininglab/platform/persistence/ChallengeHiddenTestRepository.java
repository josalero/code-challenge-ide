package com.codetraininglab.platform.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeHiddenTestRepository extends JpaRepository<ChallengeHiddenTestEntity, UUID> {

  List<ChallengeHiddenTestEntity> findByChallengeIdOrderBySortOrderAsc(UUID challengeId);
}
