package com.codetraininglab.operations.application;

import com.codetraininglab.platform.config.CtlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Optionally pre-start runner pool containers with a smoke submission after the API is up. */
@Component
@Order(100)
@ConditionalOnProperty(name = "ctl.runner-pool-warm-on-startup", havingValue = "true")
public class RunnerPoolWarmStartup implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(RunnerPoolWarmStartup.class);

  private final CtlProperties properties;
  private final RunnerOpsService runnerOpsService;

  public RunnerPoolWarmStartup(CtlProperties properties, RunnerOpsService runnerOpsService) {
    this.properties = properties;
    this.runnerOpsService = runnerOpsService;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!properties.dockerEnabled()) {
      return;
    }
    log.info("Starting runner pool smoke warm (ctl.runner-pool-warm-on-startup=true)");
    try {
      runnerOpsService.startRunnerWarm(false, java.util.List.of());
    } catch (Exception ex) {
      log.warn("Runner pool warm on startup could not start: {}", ex.getMessage());
    }
  }
}
