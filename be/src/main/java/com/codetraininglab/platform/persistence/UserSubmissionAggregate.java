package com.codetraininglab.platform.persistence;

import java.time.Instant;
import java.util.UUID;

public interface UserSubmissionAggregate {

  UUID getUserId();

  long getTotalSubmissions();

  long getPracticeRuns();

  long getGradedSubmits();

  Instant getLastActivityAt();
}
