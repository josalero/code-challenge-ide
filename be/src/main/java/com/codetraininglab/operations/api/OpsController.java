package com.codetraininglab.operations.api;

import com.codetraininglab.operations.application.DeadLetterQueueService;
import com.codetraininglab.operations.application.RunnerOpsService;
import com.codetraininglab.platform.web.ApiPaths;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class OpsController {

  private static final Logger log = LoggerFactory.getLogger(OpsController.class);

  private final DeadLetterQueueService deadLetterQueueService;
  private final RunnerOpsService runnerOpsService;

  public OpsController(
      DeadLetterQueueService deadLetterQueueService, RunnerOpsService runnerOpsService) {
    this.deadLetterQueueService = deadLetterQueueService;
    this.runnerOpsService = runnerOpsService;
  }

  @GetMapping(ApiPaths.OPS_DEAD_LETTER_SUBMISSIONS)
  public List<DeadLetterSubmissionResponse> deadLetterSubmissions(
      @RequestParam(defaultValue = "20") int limit) {
    return deadLetterQueueService.peek(limit);
  }

  @PostMapping(ApiPaths.OPS_DEAD_LETTER_REPLAY)
  public DeadLetterReplayResponse replayDeadLetterSubmissions(
      @RequestParam(defaultValue = "10") int limit) {
    return deadLetterQueueService.replay(limit);
  }

  @GetMapping(ApiPaths.OPS_RUNNERS_STATUS)
  public RunnerOpsStatusResponse runnerStatus() {
    return runnerOpsService.status();
  }

  @PostMapping(ApiPaths.OPS_RUNNERS_WARM_MAVEN)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public RunnerOpsJobResponse warmMaven(@RequestParam(defaultValue = "false") boolean force) {
    log.info("Runner ops HTTP warm maven requested force={}", force);
    RunnerOpsJobResponse response = runnerOpsService.startMavenWarm(force);
    log.info("Runner ops HTTP warm maven accepted jobId={}", response.id());
    return response;
  }

  @PostMapping(ApiPaths.OPS_RUNNERS_WARM_LSP)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public RunnerOpsJobResponse warmLsp(@RequestBody(required = false) LspWarmRequest request) {
    boolean force = request != null && request.force();
    List<String> only = request == null ? List.of() : request.only();
    log.info("Runner ops HTTP warm lsp requested force={} only={}", force, only);
    RunnerOpsJobResponse response = runnerOpsService.startLspWarm(force, only);
    log.info("Runner ops HTTP warm lsp accepted jobId={}", response.id());
    return response;
  }

  @PostMapping(ApiPaths.OPS_RUNNERS_WARM_POOL)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public RunnerOpsJobResponse warmRunnerPool(
      @RequestBody(required = false) RunnerWarmRequest request) {
    boolean force = request != null && request.force();
    List<String> only = request == null ? List.of() : request.only();
    log.info("Runner ops HTTP warm pool requested force={} only={}", force, only);
    RunnerOpsJobResponse response = runnerOpsService.startRunnerWarm(force, only);
    log.info("Runner ops HTTP warm pool accepted jobId={}", response.id());
    return response;
  }

  @PostMapping(ApiPaths.OPS_RUNNERS_WARM_INFRA)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public RunnerOpsJobResponse warmInfra(@RequestBody(required = false) RunnerWarmRequest request) {
    boolean force = request != null && request.force();
    List<String> only = request == null ? List.of() : request.only();
    log.info("Runner ops HTTP warm infra requested force={} only={}", force, only);
    RunnerOpsJobResponse response = runnerOpsService.startInfraWarm(force, only);
    log.info("Runner ops HTTP warm infra accepted jobId={}", response.id());
    return response;
  }

  @GetMapping(ApiPaths.OPS_RUNNERS_JOBS + "/{jobId}")
  public RunnerOpsJobResponse runnerJob(@PathVariable UUID jobId) {
    return runnerOpsService
        .job(jobId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Runner ops job not found"));
  }
}
