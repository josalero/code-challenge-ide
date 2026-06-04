package com.codetraininglab.operations.application;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@ConditionalOnProperty(name = "ctl.runner-infra-warm-scheduled-enabled", havingValue = "true")
public class RunnerInfraWarmScheduler {

  private static final Logger log = LoggerFactory.getLogger(RunnerInfraWarmScheduler.class);

  private final RunnerOpsService runnerOpsService;

  public RunnerInfraWarmScheduler(RunnerOpsService runnerOpsService) {
    this.runnerOpsService = runnerOpsService;
  }

  @Scheduled(
      fixedDelayString = "${ctl.runner-infra-warm-interval-minutes:30}",
      initialDelayString = "${ctl.runner-infra-warm-initial-delay-minutes:5}",
      timeUnit = TimeUnit.MINUTES)
  public void warmInfraOnSchedule() {
    log.info("Scheduled runner infra warm tick started");
    try {
      var job = runnerOpsService.startInfraWarm(false, List.of());
      log.info("Scheduled runner infra warm accepted jobId={}", job.id());
    } catch (ResponseStatusException ex) {
      if (ex.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
        log.info("Scheduled runner infra warm skipped because another runner ops job is running");
        return;
      }
      log.warn("Scheduled runner infra warm could not start: {}", ex.getReason());
    } catch (Exception ex) {
      log.warn("Scheduled runner infra warm failed to enqueue: {}", ex.getMessage(), ex);
    }
  }
}
