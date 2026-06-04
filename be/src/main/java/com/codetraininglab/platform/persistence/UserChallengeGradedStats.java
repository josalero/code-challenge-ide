package com.codetraininglab.platform.persistence;

import java.util.UUID;

public interface UserChallengeGradedStats {

  UUID getChallengeId();

  long getGradedPasses();

  long getGradedFails();
}
