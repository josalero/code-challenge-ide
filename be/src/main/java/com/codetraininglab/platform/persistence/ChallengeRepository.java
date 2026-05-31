package com.codetraininglab.platform.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<ChallengeEntity, UUID> {

  Optional<ChallengeEntity> findBySlug(String slug);

  Page<ChallengeEntity> findAllByOrderByTitleAsc(Pageable pageable);
}
