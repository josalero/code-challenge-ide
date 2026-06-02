package com.codetraininglab.integration.lsp;

import com.codetraininglab.platform.config.CtlProperties;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Removes orphaned LSP editor containers (leaked when the API or docker client dies abruptly). */
@Component
@ConditionalOnProperty(name = "ctl.docker-enabled", havingValue = "true", matchIfMissing = true)
public class LspContainerCleanup {

  private static final Logger log = LoggerFactory.getLogger(LspContainerCleanup.class);
  private static final int DOCKER_TIMEOUT_SECONDS = 30;

  private final CtlProperties properties;

  public LspContainerCleanup(CtlProperties properties) {
    this.properties = properties;
  }

  @PostConstruct
  void removeStaleContainersOnStartup() {
    if (!properties.lspEnabled()) {
      return;
    }
    cleanupOrphans(Set.of());
  }

  /**
   * Stops containers that are not tied to an active {@link LspDockerSession}.
   *
   * @param activeContainerNames names to keep (from the in-memory session registry)
   */
  public void cleanupOrphans(Set<String> activeContainerNames) {
    if (!properties.lspEnabled()) {
      return;
    }
    Set<String> active =
        activeContainerNames == null ? Set.of() : Set.copyOf(activeContainerNames);
    Set<String> running = listManagedLspContainerNames();
    List<String> orphans = orphansToRemove(running, active);
    if (orphans.isEmpty()) {
      return;
    }
    int removed = 0;
    for (String name : orphans) {
      if (forceRemove(name)) {
        removed++;
      }
    }
    log.info("Removed {} orphaned LSP container(s): {}", removed, orphans);
  }

  static List<String> orphansToRemove(Set<String> running, Set<String> active) {
    List<String> orphans = new ArrayList<>();
    for (String name : running) {
      if (name.isBlank() || active.contains(name)) {
        continue;
      }
      orphans.add(name);
    }
    return List.copyOf(orphans);
  }

  /** Best-effort stop for a single session container (used when closing a WebSocket session). */
  public static void forceRemoveQuietly(String containerName) {
    if (containerName == null || containerName.isBlank()) {
      return;
    }
    forceRemove(containerName);
  }

  static boolean forceRemove(String containerName) {
    try {
      Process process =
          new ProcessBuilder("docker", "rm", "-f", containerName)
              .redirectErrorStream(true)
              .start();
      boolean finished = process.waitFor(DOCKER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      if (!finished) {
        process.destroyForcibly();
        return false;
      }
      return process.exitValue() == 0;
    } catch (Exception ex) {
      log.debug("Could not remove LSP container {}: {}", containerName, ex.getMessage());
      return false;
    }
  }

  private Set<String> listManagedLspContainerNames() {
    LinkedHashSet<String> names = new LinkedHashSet<>();
    names.addAll(listContainerNames("label", LspUserLanguagePool.POOL_LABEL));
    names.addAll(listContainerNames("label", LspDockerSession.LSP_SESSION_LABEL));
    names.addAll(listContainerNames("name", "ctl-lsp-"));
    return names;
  }

  private List<String> listContainerNames(String filterKey, String filterValue) {
    try {
      Process process =
          new ProcessBuilder(
                  "docker",
                  "ps",
                  "-a",
                  "--filter",
                  filterKey + "=" + filterValue,
                  "--format",
                  "{{.Names}}")
              .redirectErrorStream(true)
              .start();
      boolean finished = process.waitFor(DOCKER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      if (!finished || process.exitValue() != 0) {
        return List.of();
      }
      List<String> names = new ArrayList<>();
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          String trimmed = line.trim();
          if (!trimmed.isEmpty()) {
            names.add(trimmed);
          }
        }
      }
      return names;
    } catch (Exception ex) {
      log.debug("Could not list LSP containers ({}={}): {}", filterKey, filterValue, ex.getMessage());
      return List.of();
    }
  }
}
