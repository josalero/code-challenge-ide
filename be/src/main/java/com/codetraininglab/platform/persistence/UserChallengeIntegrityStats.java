package com.codetraininglab.platform.persistence;

import java.util.UUID;

public interface UserChallengeIntegrityStats {

  UUID getChallengeId();

  long getCopyAttempts();

  long getPasteAttempts();

  long getCutAttempts();

  long getTabHiddenCount();

  long getWindowBlurCount();

  long getLargeEditCount();

  long getTotalAwayMs();
}
