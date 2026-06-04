package com.codetraininglab.platform.persistence;

import java.util.UUID;

public interface UserChallengeFeedbackStats {

  UUID getChallengeId();

  long getFeedbackItems();

  long getFeedbackWarnings();
}
