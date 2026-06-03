package com.codetraininglab.integration.lsp;

import com.codetraininglab.operations.application.RunnerOpsPaths;
import com.codetraininglab.platform.config.CtlProperties;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * One long-lived LSP Docker container per authenticated user and language. Each WebSocket attaches
 * via {@code docker exec -i} instead of {@code docker run} per tab.
 */
@Component
public class LspUserLanguagePool {

  private static final Logger log = LoggerFactory.getLogger(LspUserLanguagePool.class);
  private static final int DOCKER_TIMEOUT_SECONDS = 60;

  static final String POOL_LABEL = "ctl.lsp-pool=true";
  static final String POOL_USER_LABEL = "ctl.lsp-user";
  static final String POOL_LANGUAGE_LABEL = LspDockerSession.LSP_LANGUAGE_LABEL;

  private final CtlProperties properties;
  private final Environment environment;
  private final Clock clock;
  private final Map<String, PooledLsp> pools = new ConcurrentHashMap<>();

  public LspUserLanguagePool(CtlProperties properties, Environment environment, Clock clock) {
    this.properties = properties;
    this.environment = environment;
    this.clock = clock;
  }

  public LspDockerSession attach(
      WebSocketSession clientSession, UUID userId, String language, String lspImage, String solution)
      throws IOException {
    String key = poolKey(userId, language);
    PooledLsp pooled =
        pools.computeIfAbsent(
            key,
            ignored ->
                new PooledLsp(
                    userId,
                    language,
                    lspImage,
                    poolContainerName(userId, language),
                    workspaceRoot(userId, language),
                    new ReentrantLock(),
                    new AtomicReference<>(null),
                    new AtomicReference<>(null),
                    new AtomicReference<>(Instant.EPOCH)));

    pooled.lock.lock();
    try {
      LspWorkspaceSupport.populate(pooled.workspace, language, solution);
      ensureContainerRunning(pooled);
      replaceActiveBridge(pooled, clientSession);
      pooled.lastUsedAt.set(clock.instant());
      return pooled.activeBridge.get();
    } finally {
      pooled.lock.unlock();
    }
  }

  public void releaseBridge(UUID userId, String language, LspDockerSession session) {
    String key = poolKey(userId, language);
    PooledLsp pooled = pools.get(key);
    if (pooled == null) {
      session.closeBridgeOnly();
      return;
    }
    pooled.lock.lock();
    try {
      pooled.activeBridge.compareAndSet(session, null);
      session.closeBridgeOnly();
      pooled.lastUsedAt.set(clock.instant());
    } finally {
      pooled.lock.unlock();
    }
  }

  public Set<String> managedContainerNames() {
    LinkedHashSet<String> names = new LinkedHashSet<>();
    for (PooledLsp pooled : pools.values()) {
      if (pooled.containerId.get() != null) {
        names.add(pooled.containerName);
      }
    }
    return Set.copyOf(names);
  }

  @PostConstruct
  void adoptExistingPoolContainers() {
    if (!properties.lspEnabled()) {
      return;
    }
    try {
      Process process =
          new ProcessBuilder(
                  "docker",
                  "ps",
                  "-a",
                  "--filter",
                  "label=" + POOL_LABEL,
                  "--format",
                  "{{.ID}}\t{{.Label \"" + POOL_USER_LABEL + "\"}}\t{{.Label \"" + POOL_LANGUAGE_LABEL + "\"}}\t{{.Names}}")
              .redirectErrorStream(true)
              .start();
      boolean finished = process.waitFor(DOCKER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
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
          String[] parts = line.split("\t", 4);
          if (parts.length < 4 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            continue;
          }
          if (!isRunning(parts[0])) {
            continue;
          }
          UUID userId = UUID.fromString(parts[1]);
          String language = parts[2];
          String containerName = parts[3];
          String key = poolKey(userId, language);
          pools.compute(
              key,
              (ignored, existing) -> {
                if (existing != null && existing.containerId.get() != null) {
                  return existing;
                }
                String image = properties.lspImageFor(language);
                return new PooledLsp(
                    userId,
                    language,
                    image,
                    containerName,
                    workspaceRoot(userId, language),
                    new ReentrantLock(),
                    new AtomicReference<>(parts[0].trim()),
                    new AtomicReference<>(null),
                    new AtomicReference<>(clock.instant()));
              });
          log.info("Adopted pooled LSP container {} for user {} {}", containerName, userId, language);
        }
      }
    } catch (Exception ex) {
      log.debug("Could not adopt existing LSP pool containers: {}", ex.getMessage());
    }
  }

  @Scheduled(fixedRate = 60_000)
  void evictIdlePools() {
    if (!properties.lspEnabled()) {
      return;
    }
    Duration idleLimit = Duration.ofMinutes(Math.max(1, properties.lspIdleMinutes()));
    Instant cutoff = clock.instant().minus(idleLimit);
    for (Map.Entry<String, PooledLsp> entry : pools.entrySet()) {
      PooledLsp pooled = entry.getValue();
      if (pooled.activeBridge.get() != null) {
        continue;
      }
      if (pooled.lastUsedAt.get().isAfter(cutoff)) {
        continue;
      }
      if (!pooled.lock.tryLock()) {
        continue;
      }
      try {
        if (pooled.activeBridge.get() != null || pooled.lastUsedAt.get().isAfter(cutoff)) {
          continue;
        }
        destroyPool(entry.getKey(), pooled);
        log.info("Evicted idle LSP pool {} ({})", pooled.containerName, pooled.language);
      } finally {
        pooled.lock.unlock();
      }
    }
  }

  static String poolKey(UUID userId, String language) {
    return userId + ":" + language.trim().toLowerCase();
  }

  static String poolContainerName(UUID userId, String language) {
    String userPart = userId.toString().replace("-", "").substring(0, 8);
    String lang = language.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
    if (lang.isBlank()) {
      lang = "lsp";
    }
    String name = "ctl-lsp-pool-" + userPart + "-" + lang;
    if (name.length() > 63) {
      name = name.substring(0, 63);
    }
    return name;
  }

  private Path workspaceRoot(UUID userId, String language) {
    return LspWorkspaceSupport.userWorkspaceRoot(
        RunnerOpsPaths.resolveOpsDataDir(environment), userId, language);
  }

  private void replaceActiveBridge(PooledLsp pooled, WebSocketSession clientSession)
      throws IOException {
    LspDockerSession previous = pooled.activeBridge.getAndSet(null);
    if (previous != null) {
      previous.closeBridgeOnly();
    }
    LspDockerSession bridge =
        LspDockerSession.attachFromPool(
            clientSession,
            this,
            pooled.userId,
            pooled.language,
            pooled.containerName,
            pooled.workspace);
    pooled.activeBridge.set(bridge);
  }

  private void ensureContainerRunning(PooledLsp pooled) throws IOException {
    String containerId = pooled.containerId.get();
    if (containerId != null && isRunning(containerId)) {
      return;
    }
    destroyContainerQuietly(pooled);
    Files.createDirectories(pooled.workspace);
    List<String> command = buildPoolCreateCommand(pooled);
    Process process =
        new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.PIPE).start();
    String createdId;
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      createdId = reader.readLine();
    }
    boolean finished;
    try {
      finished = process.waitFor(DOCKER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while starting LSP pool container", ex);
    }
    if (!finished) {
      process.destroyForcibly();
      throw new IOException("Timed out starting LSP pool container");
    }
    if (process.exitValue() != 0 || createdId == null || createdId.isBlank()) {
      throw new IOException("Could not start LSP pool container (exit " + process.exitValue() + ")");
    }
    String id = createdId.trim();
    if (!isRunning(id)) {
      throw new IOException("LSP pool container exited immediately");
    }
    pooled.containerId.set(id);
    log.info(
        "Started pooled LSP container {} ({}) for user {} {}",
        pooled.containerName,
        id,
        pooled.userId,
        pooled.language);
  }

  private List<String> buildPoolCreateCommand(PooledLsp pooled) {
    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("run");
    command.add("-d");
    command.add("--name");
    command.add(pooled.containerName);
    command.add("--label");
    command.add(POOL_LABEL);
    command.add("--label");
    command.add(POOL_USER_LABEL + "=" + pooled.userId);
    command.add("--label");
    command.add(POOL_LANGUAGE_LABEL + "=" + pooled.language);
    command.add("--network");
    command.add("none");
    command.add("--cap-drop");
    command.add("ALL");
    command.add("--cap-add");
    command.add("DAC_OVERRIDE");
    command.add("--cap-add");
    command.add("FOWNER");
    command.add("--security-opt");
    command.add("no-new-privileges:true");
    command.add("-v");
    command.add(pooled.workspace.toAbsolutePath().normalize() + ":/workspace");
    command.add("-e");
    command.add("CTL_LSP_LANGUAGE=" + pooled.language);
    command.add("--entrypoint");
    command.add("sleep");
    command.add(pooled.lspImage);
    command.add("infinity");
    return command;
  }

  private void destroyPool(String key, PooledLsp pooled) {
    LspDockerSession bridge = pooled.activeBridge.getAndSet(null);
    if (bridge != null) {
      bridge.closeBridgeOnly();
    }
    destroyContainerQuietly(pooled);
    pools.remove(key, pooled);
    deleteWorkspaceQuietly(pooled.workspace);
  }

  private void destroyContainerQuietly(PooledLsp pooled) {
    pooled.containerId.set(null);
    LspContainerCleanup.forceRemoveQuietly(pooled.containerName);
  }

  private static boolean isRunning(String containerId) {
    try {
      Process process =
          new ProcessBuilder(
                  "docker", "inspect", "-f", "{{.State.Running}}", containerId)
              .redirectErrorStream(true)
              .start();
      boolean finished = process.waitFor(10, TimeUnit.SECONDS);
      if (!finished || process.exitValue() != 0) {
        return false;
      }
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line = reader.readLine();
        return line != null && line.trim().equalsIgnoreCase("true");
      }
    } catch (Exception ex) {
      return false;
    }
  }

  private static void deleteWorkspaceQuietly(Path root) {
    if (root == null || !Files.exists(root)) {
      return;
    }
    try (Stream<Path> walk = Files.walk(root)) {
      walk.sorted(Comparator.reverseOrder())
          .forEach(
              path -> {
                try {
                  Files.deleteIfExists(path);
                } catch (IOException ignored) {
                  // best-effort
                }
              });
    } catch (IOException ignored) {
      // best-effort
    }
  }

  private static final class PooledLsp {
    private final UUID userId;
    private final String language;
    private final String lspImage;
    private final String containerName;
    private final Path workspace;
    private final ReentrantLock lock;
    private final AtomicReference<String> containerId;
    private final AtomicReference<LspDockerSession> activeBridge;
    private final AtomicReference<Instant> lastUsedAt;

    private PooledLsp(
        UUID userId,
        String language,
        String lspImage,
        String containerName,
        Path workspace,
        ReentrantLock lock,
        AtomicReference<String> containerId,
        AtomicReference<LspDockerSession> activeBridge,
        AtomicReference<Instant> lastUsedAt) {
      this.userId = userId;
      this.language = language;
      this.lspImage = lspImage;
      this.containerName = containerName;
      this.workspace = workspace;
      this.lock = lock;
      this.containerId = containerId;
      this.activeBridge = activeBridge;
      this.lastUsedAt = lastUsedAt;
    }
  }
}
