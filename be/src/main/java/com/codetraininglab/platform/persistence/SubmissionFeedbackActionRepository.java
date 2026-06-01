package com.codetraininglab.platform.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionFeedbackActionRepository
    extends JpaRepository<SubmissionFeedbackActionEntity, UUID> {

  List<SubmissionFeedbackActionEntity> findBySubmissionIdOrderByCreatedAtDesc(UUID submissionId);
}
