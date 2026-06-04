package com.codetraininglab.integration.runner;

import com.codetraininglab.domain.RunnerStatus;
import com.codetraininglab.domain.TestOutcomeStatus;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.util.InterruptSupport;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@ConditionalOnProperty(name = "ctl.docker-enabled", havingValue = "true", matchIfMissing = true)
public class RunnerContainerPool {

  private static final Logger log = LoggerFactory.getLogger(RunnerContainerPool.class);
  private static final int EXEC_EXIT_GRACE_SECONDS = 15;

  private final CtlProperties properties;
  private final JsonMapper jsonMapper;
  private final Clock clock;
  private final Map<String, PooledRunner> pools = new ConcurrentHashMap<>();

  public RunnerContainerPool(CtlProperties properties, JsonMapper jsonMapper, Clock clock) {
    this.properties = properties;
    this.jsonMapper = jsonMapper;
    this.clock = clock;
  }

  public boolean isEnabled() {
    return properties.runnerPoolEnabled();
  }

  /**
   * True when a pooled runner container is running for {@code image} (in-memory map or on the
   * Docker host). Ops status uses this so a live pool is not shown as cold when only the image
   * stamp is stale after {@code docker build}.
   */
  public boolean isPoolRunningForImage(String image) {
    if (image == null || image.isBlank()) {
      return false;
    }
    PooledRunner pooled = pools.get(image);
    if (pooled != null) {
      String containerId = pooled.containerId.get();
      if (containerId != null && isRunning(containerId)) {
        return true;
      }
    }
    return isPoolRunningOnHost(image);
  }

  public boolean refreshIdleTimerForImage(String image) {
    if (image == null || image.isBlank()) {
      return false;
    }
    PooledRunner pooled = pools.get(image);
    if (pooled == null) {
      return false;
    }
    String containerId = pooled.containerId.get();
    if (containerId == null || !isRunning(containerId)) {
      return false;
    }
    pooled.lastUsedAt.set(clock.instant());
    return true;
  }

  private boolean isPoolRunningOnHost(String image) {
    try {
      Process process =
          new ProcessBuilder(
                  "docker",
                  "ps",
                  "--filter",
                  "label=" + DockerRunnerCommands.POOL_LABEL,
                  "--filter",
                  "label=" + DockerRunnerCommands.POOL_IMAGE_LABEL + "=" + image,
                  "--filter",
                  "status=running",
                  "--format",
                  "{{.ID}}")
              .start();
      boolean finished = process.waitFor(10, TimeUnit.SECONDS);
      if (!finished || process.exitValue() != 0) {
        return false;
      }
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line = reader.readLine();
        return line != null && !line.isBlank();
      }
    } catch (IOException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  public RunnerResult execute(
      String image,
      Path challengeDir,
      String workspaceLayout,
      String jobJson,
      RunnerJobPayload.RunnerLimits limits) {
    PooledRunner pooled =
        pools.computeIfAbsent(
            image,
            ignored ->
                new PooledRunner(
                    image,
                    DockerRunnerCommands.poolContainerName(image),
                    new ReentrantLock(),
                    new AtomicReference<>(null),
                    new AtomicReference<>(Instant.EPOCH),
                    new AtomicReference<>(null)));

    pooled.lock.lock();
    try {
      for (int attempt = 0; attempt < 2; attempt++) {
        try {
          ensureContainerRunning(pooled, workspaceLayout, limits);
          syncChallengeIfNeeded(pooled, challengeDir);
          if (isWarmSmokeJob(jobJson)) {
            prepareWarmWorkspace(pooled.containerId.get());
          }
          return execJob(pooled, jobJson, limits);
        } catch (RunnerPoolException ex) {
          // A prior attempt may have set the interrupt flag; clear before retry or return so
          // warm inventory JDBC on this virtual thread is not poisoned.
          InterruptSupport.clearInterrupted();
          log.warn(
              "Runner pool attempt failed for image {} (attempt {}): {}",
              image,
              attempt + 1,
              ex.getMessage());
          destroyContainer(pooled);
          if (attempt == 1) {
            return failedResult(ex.getMessage());
          }
        }
      }
      return failedResult("Runner pool failed");
    } finally {
      pooled.lastUsedAt.set(clock.instant());
      pooled.lock.unlock();
    }
  }

  @PostConstruct
  void adoptExistingContainers() {
    if (!properties.runnerPoolEnabled()) {
      return;
    }
    try {
      Process process =
          new ProcessBuilder(
                  "docker",
                  "ps",
                  "-a",
                  "--filter",
                  "label=" + DockerRunnerCommands.POOL_LABEL,
                  "--format",
                  "{{.ID}}\t{{.Label \"" + DockerRunnerCommands.POOL_IMAGE_LABEL + "\"}}\t{{.Names}}")
              .start();
      boolean finished = process.waitFor(15, TimeUnit.SECONDS);
      if (!finished || process.exitValue() != 0) {
        return;
      }
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.isBlank()) {
            continue;
          }
          String[] parts = line.split("\t", 3);
          if (parts.length < 3 || parts[0].isBlank() || parts[1].isBlank()) {
            continue;
          }
          if (!isRunning(parts[0])) {
            continue;
          }
          String runnerImage = parts[1];
          pools.compute(
              runnerImage,
              (ignored, existing) -> {
                if (existing != null && existing.containerId.get() != null) {
                  return existing;
                }
                return new PooledRunner(
                    runnerImage,
                    parts[2],
                    new ReentrantLock(),
                    new AtomicReference<>(parts[0]),
                    new AtomicReference<>(clock.instant()),
                    new AtomicReference<>(null));
              });
          log.info("Adopted pooled runner container {} for image {}", parts[2], runnerImage);
        }
      }
    } catch (Exception ex) {
      log.debug("Could not adopt existing runner pool containers: {}", ex.getMessage());
    }
  }

  @Scheduled(fixedRate = 60_000)
  void evictIdleContainers() {
    if (!properties.runnerPoolEnabled()) {
      return;
    }
    Duration idleLimit = Duration.ofMinutes(Math.max(5, properties.runnerPoolIdleMinutes()));
    Instant cutoff = clock.instant().minus(idleLimit);
    for (Map.Entry<String, PooledRunner> entry : pools.entrySet()) {
      PooledRunner pooled = entry.getValue();
      if (pooled.lastUsedAt.get().isAfter(cutoff)) {
        continue;
      }
      if (!pooled.lock.tryLock()) {
        continue;
      }
      try {
        if (pooled.lastUsedAt.get().isAfter(cutoff)) {
          continue;
        }
        destroyContainer(pooled);
        pools.remove(entry.getKey(), pooled);
        log.info("Evicted idle runner pool container for image {}", entry.getKey());
      } finally {
        pooled.lock.unlock();
      }
    }
  }

  private void ensureContainerRunning(
      PooledRunner pooled, String workspaceLayout, RunnerJobPayload.RunnerLimits limits)
      throws RunnerPoolException {
    String containerId = pooled.containerId.get();
    if (containerId != null && isRunning(containerId)) {
      return;
    }
    destroyContainer(pooled);
    List<String> createCommand =
        DockerRunnerCommands.buildPoolCreateCommand(
            pooled.containerName, pooled.image, limits, workspaceLayout, properties);
    try {
      Process process = new ProcessBuilder(createCommand).start();
      String createdId;
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        createdId = reader.readLine();
      }
      process.getErrorStream().transferTo(java.io.OutputStream.nullOutputStream());
      boolean finished = process.waitFor(60, TimeUnit.SECONDS);
      if (!finished || process.exitValue() != 0 || createdId == null || createdId.isBlank()) {
        throw new RunnerPoolException("Could not start pooled runner container");
      }
      if (!isRunning(createdId)) {
        throw new RunnerPoolException("Pooled runner container exited immediately");
      }
      pooled.containerId.set(createdId.trim());
      log.info("Started pooled runner container {} ({}) for image {}", pooled.containerName, createdId, pooled.image);
    } catch (IOException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new RunnerPoolException("Could not start pooled runner container", ex);
    }
  }

  private void syncChallengeIfNeeded(PooledRunner pooled, Path challengeDir)
      throws RunnerPoolException {
    Path absolute = challengeDir.toAbsolutePath().normalize();
    String challengeKey = absolute.toString();
    if (challengeKey.equals(pooled.lastChallengeDir.get())) {
      return;
    }
    syncChallenge(pooled.containerId.get(), absolute);
    clearWorkspaceStamp(pooled.containerId.get());
    pooled.lastChallengeDir.set(challengeKey);
  }

  /**
   * Pool smoke warm must not reuse a prior submission workspace (stale tests, Surefire XML, or
   * starter bytecode). Submission ids for warm jobs are prefixed with {@code warm-}.
   */
  private boolean isWarmSmokeJob(String jobJson) {
    try {
      var node = jsonMapper.readTree(jobJson);
      var submissionId = node.get("submission_id");
      if (submissionId == null || submissionId.isNull()) {
        return false;
      }
      return submissionId.asString("").startsWith("warm-");
    } catch (Exception ex) {
      return false;
    }
  }

  private void prepareWarmWorkspace(String containerId) throws RunnerPoolException {
    if (containerId == null || containerId.isBlank()) {
      return;
    }
    clearWorkspaceStamp(containerId);
    try {
      runDocker(
          "docker",
          "exec",
          "-u",
          "0",
          containerId,
          "sh",
          "-c",
          "rm -rf /tmp/workspace/src "
              + "/tmp/workspace/tests "
              + "/tmp/workspace/junit.xml "
              + "/tmp/workspace/coverage "
              + "/tmp/workspace/.c8_tmp "
              + "/tmp/workspace/target/classes "
              + "/tmp/workspace/target/test-classes "
              + "/tmp/workspace/target/surefire-reports");
    } catch (IOException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new RunnerPoolException("Could not reset pooled workspace for warm smoke", ex);
    }
  }

  /**
   * Drops incremental-workspace stamp when the synced challenge changes. Uses root because only
   * Java runner images define a {@code runner} user; other runners run as root and pool exec does
   * not drop privileges.
   */
  private void clearWorkspaceStamp(String containerId) throws RunnerPoolException {
    try {
      runDocker(
          "docker",
          "exec",
          "-u",
          "0",
          containerId,
          "rm",
          "-f",
          "/tmp/workspace/.ctl-challenge-slug");
    } catch (IOException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new RunnerPoolException("Could not clear workspace stamp", ex);
    }
  }

  private void syncChallenge(String containerId, Path absolute) throws RunnerPoolException {
    try {
      runDocker(
          "docker",
          "exec",
          "-u",
          "0",
          containerId,
          "sh",
          "-c",
          "mkdir -p /challenge && rm -rf /challenge/*");
      runDocker("docker", "cp", absolute + "/.", containerId + ":/challenge/");
      runDocker("docker", "exec", "-u", "0", containerId, "chmod", "-R", "a+rX", "/challenge");
    } catch (IOException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new RunnerPoolException("Could not sync challenge files into runner container", ex);
    }
  }

  private RunnerResult execJob(PooledRunner pooled, String jobJson, RunnerJobPayload.RunnerLimits limits)
      throws RunnerPoolException {
    String containerId = pooled.containerId.get();
    if (containerId == null) {
      throw new RunnerPoolException("Runner container is not running");
    }
    return execViaOneShotResult(containerId, jobJson, limits);
  }

  private RunnerResult execViaOneShotResult(
      String containerId, String jobJson, RunnerJobPayload.RunnerLimits limits)
      throws RunnerPoolException {
    try {
      ProcessBuilder builder =
          new ProcessBuilder(DockerRunnerCommands.buildPoolExecCommand(containerId));
      Process process = builder.start();
      process.getOutputStream().write(jobJson.getBytes(StandardCharsets.UTF_8));
      process.getOutputStream().close();
      CompletableFuture.runAsync(
          () -> {
            try {
              process.getErrorStream().transferTo(java.io.OutputStream.nullOutputStream());
            } catch (IOException ignored) {
              // Best-effort drain.
            }
          });
      String line;
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        line = reader.readLine();
      }
      if (line == null || line.isBlank()) {
        boolean finished =
            process.waitFor(Math.min(limits.wallSeconds() + 10L, 130L), TimeUnit.SECONDS);
        if (!finished) {
          process.destroyForcibly();
          throw new RunnerPoolException("Runner produced no output (timed out)");
        }
        throw new RunnerPoolException("Runner produced no output");
      }
      boolean exited = process.waitFor(EXEC_EXIT_GRACE_SECONDS, TimeUnit.SECONDS);
      if (!exited) {
        log.warn("Runner process did not exit within {}s after emitting result; destroying", EXEC_EXIT_GRACE_SECONDS);
        process.destroyForcibly();
        process.waitFor(5, TimeUnit.SECONDS);
      }
      return jsonMapper.readValue(line, RunnerResult.class);
    } catch (IOException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new RunnerPoolException("Runner execution failed", ex);
    }
  }

  private void destroyContainer(PooledRunner pooled) {
    pooled.lastChallengeDir.set(null);
    String containerId = pooled.containerId.getAndSet(null);
    if (containerId != null) {
      try {
        Process process = new ProcessBuilder("docker", "rm", "-f", containerId).start();
        process.waitFor(30, TimeUnit.SECONDS);
      } catch (Exception ex) {
        log.debug("Could not remove pooled runner container {}: {}", containerId, ex.getMessage());
      }
    }
    removeContainerByName(pooled.containerName);
  }

  private void removeContainerByName(String containerName) {
    try {
      Process process = new ProcessBuilder("docker", "rm", "-f", containerName).start();
      process.waitFor(30, TimeUnit.SECONDS);
    } catch (Exception ex) {
      log.debug("Could not remove pooled runner container {}: {}", containerName, ex.getMessage());
    }
  }

  private static boolean isRunning(String containerId) {
    try {
      Process process =
          new ProcessBuilder("docker", "inspect", "-f", "{{.State.Running}}", containerId).start();
      boolean finished = process.waitFor(10, TimeUnit.SECONDS);
      if (!finished || process.exitValue() != 0) {
        return false;
      }
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        return "true".equalsIgnoreCase(reader.readLine());
      }
    } catch (Exception ex) {
      return false;
    }
  }

  private static void runDocker(String... command) throws IOException, InterruptedException {
    Process process = new ProcessBuilder(command).start();
    String stderr =
        new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8).trim();
    process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
    boolean finished = process.waitFor(60, TimeUnit.SECONDS);
    if (!finished || process.exitValue() != 0) {
      String detail = stderr.isBlank() ? "exit " + process.exitValue() : stderr;
      throw new IOException("docker command failed (" + detail + "): " + String.join(" ", command));
    }
  }

  private RunnerResult failedResult(String message) {
    return new RunnerResult(
        RunnerStatus.FAILED.name(),
        List.of(
            new RunnerResult.TestOutcome(
                "runner", TestOutcomeStatus.FAIL.name(), message, 0)),
        new RunnerResult.CoverageOutcome(0, 0),
        new RunnerResult.CompileOutcome(0, List.of()),
        null,
        null);
  }

  private static final class PooledRunner {
    private final String image;
    private final String containerName;
    private final ReentrantLock lock;
    private final AtomicReference<String> containerId;
    private final AtomicReference<Instant> lastUsedAt;
    private final AtomicReference<String> lastChallengeDir;

    private PooledRunner(
        String image,
        String containerName,
        ReentrantLock lock,
        AtomicReference<String> containerId,
        AtomicReference<Instant> lastUsedAt,
        AtomicReference<String> lastChallengeDir) {
      this.image = image;
      this.containerName = containerName;
      this.lock = lock;
      this.containerId = containerId;
      this.lastUsedAt = lastUsedAt;
      this.lastChallengeDir = lastChallengeDir;
    }
  }

  static final class RunnerPoolException extends Exception {
    RunnerPoolException(String message) {
      super(message);
    }

    RunnerPoolException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
