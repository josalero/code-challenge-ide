package com.codetraininglab.platform.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RunnerPoolWarmStateRepository
    extends JpaRepository<RunnerPoolWarmStateEntity, String> {}
