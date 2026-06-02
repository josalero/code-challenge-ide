package com.codetraininglab.integration.runner;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StubRunnerClientTest {

  private final StubRunnerClient client = new StubRunnerClient();

  @Test
  void passesWhenSolutionDoesNotContainFail() {
    SubmissionEntity submission = submission("good");
    RunnerResult result =
        client.execute(submission, "slug", List.of(), Path.of("."), "runner:local");
    assertThat(result.tests().getFirst().status()).isEqualTo("PASS");
  }

  @Test
  void failsWhenSolutionContainsFailMarker() {
    SubmissionEntity submission = submission("FAIL");
    RunnerResult result =
        client.execute(submission, "slug", List.of(), Path.of("."), "runner:local");
    assertThat(result.tests().getFirst().status()).isEqualTo("FAIL");
  }

  private static SubmissionEntity submission(String code) {
    return new SubmissionEntity(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        SubmissionStatus.PENDING,
            com.codetraininglab.domain.SubmissionKind.SUBMIT,
        code,
        null,
        null,
        Instant.EPOCH,
        Instant.EPOCH);
  }
}
