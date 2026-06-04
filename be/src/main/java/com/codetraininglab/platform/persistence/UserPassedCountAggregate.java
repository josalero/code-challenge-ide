package com.codetraininglab.platform.persistence;

import java.util.UUID;

public interface UserPassedCountAggregate {

  UUID getUserId();

  long getPassedCount();
}
