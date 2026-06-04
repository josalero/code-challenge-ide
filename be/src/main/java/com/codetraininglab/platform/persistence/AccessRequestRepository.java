package com.codetraininglab.platform.persistence;

import com.codetraininglab.domain.AccessRequestStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccessRequestRepository extends JpaRepository<AccessRequestEntity, UUID> {

  @Query(
      """
      SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
      FROM AccessRequestEntity a
      WHERE LOWER(a.email) = LOWER(:email) AND a.status = :status
      """)
  boolean existsByEmailIgnoreCaseAndStatus(
      @Param("email") String email, @Param("status") AccessRequestStatus status);

  List<AccessRequestEntity> findAllByOrderByCreatedAtDesc();

  List<AccessRequestEntity> findByStatusOrderByCreatedAtDesc(AccessRequestStatus status);

  long countByStatus(AccessRequestStatus status);
}
