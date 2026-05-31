package com.codetraininglab.integration.runner;

import com.codetraininglab.platform.persistence.ChallengeHiddenTestEntity;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import java.nio.file.Path;
import java.util.List;

public interface RunnerClient {

  RunnerResult execute(
      SubmissionEntity submission,
      String challengeSlug,
      List<ChallengeHiddenTestEntity> hiddenTests,
      Path challengeDir,
      String runnerImage);
}
