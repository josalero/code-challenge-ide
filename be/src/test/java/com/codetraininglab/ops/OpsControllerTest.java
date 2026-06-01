package com.codetraininglab.operations.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codetraininglab.operations.application.DeadLetterQueueService;
import com.codetraininglab.operations.application.RunnerOpsService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpsControllerTest {

  @Mock private DeadLetterQueueService deadLetterQueueService;
  @Mock private RunnerOpsService runnerOpsService;

  @InjectMocks private OpsController controller;

  @Test
  void returnsRunnerStatus() {
    when(runnerOpsService.status())
        .thenReturn(
            new RunnerOpsStatusResponse(
                true,
                true,
                false,
                true,
                false,
                false,
                "/repo",
                "/data/ctl-ops",
                "ctl-runner-m2-cache",
                List.of(),
                List.of(),
                null));
    assertThat(controller.runnerStatus().dockerAvailable()).isTrue();
  }

  @Test
  void startsMavenWarmJob() {
    UUID jobId = UUID.randomUUID();
    when(runnerOpsService.startMavenWarm(true))
        .thenReturn(
            new RunnerOpsJobResponse(
                jobId, "MAVEN_WARM", "RUNNING", null, null, "Running…", ""));
    assertThat(controller.warmMaven(true).id()).isEqualTo(jobId);
  }

  @Test
  void loadsRunnerJob() {
    UUID jobId = UUID.randomUUID();
    when(runnerOpsService.job(jobId))
        .thenReturn(
            Optional.of(
                new RunnerOpsJobResponse(
                    jobId, "LSP_WARM", "COMPLETED", null, null, "Done", "log")));
    assertThat(controller.runnerJob(jobId).status()).isEqualTo("COMPLETED");
  }
}
