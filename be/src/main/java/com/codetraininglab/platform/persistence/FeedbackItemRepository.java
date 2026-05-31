package com.codetraininglab.platform.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackItemRepository extends JpaRepository<FeedbackItemEntity, UUID> {

  List<FeedbackItemEntity> findByReportIdOrderByCategoryAsc(UUID reportId);
}
