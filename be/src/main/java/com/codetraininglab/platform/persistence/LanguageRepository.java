package com.codetraininglab.platform.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<LanguageEntity, UUID> {

  Optional<LanguageEntity> findByName(String name);
}
