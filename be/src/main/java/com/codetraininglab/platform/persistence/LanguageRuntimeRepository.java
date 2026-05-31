package com.codetraininglab.platform.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LanguageRuntimeRepository extends JpaRepository<LanguageRuntimeEntity, UUID> {

  @Query(
      """
      SELECT r FROM LanguageRuntimeEntity r
      JOIN LanguageEntity l ON l.id = r.languageId
      ORDER BY l.name, r.version
      """)
  List<LanguageRuntimeEntity> findAllOrdered();

  Optional<LanguageRuntimeEntity> findByLanguageIdAndVersion(UUID languageId, String version);

  @Query(
      """
      SELECT r FROM LanguageRuntimeEntity r
      JOIN LanguageEntity l ON l.id = r.languageId
      WHERE l.name = :languageName AND r.active = true
      ORDER BY r.version
      """)
  List<LanguageRuntimeEntity> findActiveByLanguageName(String languageName);
}
