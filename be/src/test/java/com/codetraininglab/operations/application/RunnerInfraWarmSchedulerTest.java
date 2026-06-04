package com.codetraininglab.operations.application;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.operations.api.RunnerOpsJobResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RunnerInfraWarmSchedulerTest {

  @Mock private RunnerOpsService runnerOpsService;

  @Test
  void scheduledTickStartsInfraWarmWithoutForcing() {
    var job =
        new RunnerOpsJobResponse(
            UUID.randomUUID(), "INFRA_WARM", "RUNNING", Instant.now(), null, "", null);
    when(runnerOpsService.startInfraWarm(false, List.of())).thenReturn(job);

    new RunnerInfraWarmScheduler(runnerOpsService).warmInfraOnSchedule();

    verify(runnerOpsService).startInfraWarm(false, List.of());
  }

  @Test
  void scheduledTickSkipsWhenAnotherOpsJobIsRunning() {
    when(runnerOpsService.startInfraWarm(false, List.of()))
        .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "running"));

    new RunnerInfraWarmScheduler(runnerOpsService).warmInfraOnSchedule();

    verify(runnerOpsService).startInfraWarm(false, List.of());
  }
}
