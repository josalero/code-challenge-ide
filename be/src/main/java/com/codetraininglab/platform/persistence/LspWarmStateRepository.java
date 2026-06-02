package com.codetraininglab.platform.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LspWarmStateRepository extends JpaRepository<LspWarmStateEntity, LspWarmStateEntity.LspWarmStateId> {}
