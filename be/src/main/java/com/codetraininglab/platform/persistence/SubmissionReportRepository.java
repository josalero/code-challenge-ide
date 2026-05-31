package com.codetraininglab.platform.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionReportRepository extends JpaRepository<SubmissionReportEntity, UUID> {

  Optional<SubmissionReportEntity> findBySubmissionId(UUID submissionId);
}
