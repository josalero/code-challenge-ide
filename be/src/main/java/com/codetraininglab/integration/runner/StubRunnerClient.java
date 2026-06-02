package com.codetraininglab.integration.runner;

import com.codetraininglab.domain.RunnerStatus;
import com.codetraininglab.domain.TestOutcomeStatus;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestEntity;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ctl.docker-enabled", havingValue = "false", matchIfMissing = false)
public class StubRunnerClient implements RunnerClient {

  /** Marker in solution code that forces a failing stub test (tests only). */
  static final String STUB_FAIL_MARKER = TestOutcomeStatus.FAIL.name();

  @Override
  public RunnerResult execute(
      SubmissionEntity submission,
      String challengeSlug,
      List<ChallengeHiddenTestEntity> hiddenTests,
      Path challengeDir,
      String runnerImage) {
    boolean passes = !submission.getSolutionCode().contains(STUB_FAIL_MARKER);
    if (passes) {
      return new RunnerResult(
          RunnerStatus.COMPLETED.name(),
          List.of(
              new RunnerResult.TestOutcome(
                  "stub-test", TestOutcomeStatus.PASS.name(), null, 1)),
          new RunnerResult.CoverageOutcome(85.0, 75.0),
          new RunnerResult.CompileOutcome(0, List.of()),
          null,
          null);
    }
    return new RunnerResult(
        RunnerStatus.COMPLETED.name(),
        List.of(
            new RunnerResult.TestOutcome(
                "stub-test", TestOutcomeStatus.FAIL.name(), "Assertion failed", 1)),
        new RunnerResult.CoverageOutcome(10.0, 5.0),
        new RunnerResult.CompileOutcome(0, List.of()),
        null,
        null);
  }
}
