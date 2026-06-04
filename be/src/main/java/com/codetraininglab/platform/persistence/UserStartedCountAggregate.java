package com.codetraininglab.platform.persistence;

import java.util.UUID;

public interface UserStartedCountAggregate {

  UUID getUserId();

  long getStartedCount();
}
