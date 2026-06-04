package com.codetraininglab.platform.persistence;

import java.util.UUID;

public interface UserChallengeEnhancementStats {

  UUID getChallengeId();

  long getEnhancementRequests();
}
