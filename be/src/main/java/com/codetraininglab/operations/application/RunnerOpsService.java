package com.codetraininglab.operations.application;

import com.codetraininglab.operations.api.RunnerImageStatusResponse;
import com.codetraininglab.operations.api.RunnerOpsJobResponse;
import com.codetraininglab.operations.api.RunnerOpsStatusResponse;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Service
public class RunnerOpsService {

  private static final int LOG_TAIL_MAX = 8000;
  private static final List<String> LSP_WARM_LABELS =
      List.of("java", "python", "go", "typescript", "csharp", "rust", "cpp");

  private final CtlProperties properties;
  private final Environment environment;
  private final LanguageRuntimeRepository runtimeRepository;
  private final LanguageRepository languageRepository;
  private final JsonMapper jsonMapper;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private final ConcurrentHashMap<UUID, JobState> jobs = new ConcurrentHashMap<>();
  private volatile UUID activeJobId;

  public RunnerOpsService(
      CtlProperties properties,
      Environment environment,
      LanguageRuntimeRepository runtimeRepository,
      LanguageRepository languageRepository,
      JsonMapper jsonMapper) {
    this.properties = properties;
    this.environment = environment;
    this.runtimeRepository = runtimeRepository;
    this.languageRepository = languageRepository;
    this.jsonMapper = jsonMapper;
  }

  public RunnerOpsStatusResponse status() {
    Path repoRoot = resolveRepoRoot();
    boolean dockerAvailable = isDockerAvailable();
    boolean mavenCacheWarm = isMavenCacheWarm();
    Map<String, String> lspWarmStamp = loadLspWarmStamp(repoRoot);
    List<RunnerImageStatusResponse> runnerImages = runnerImageStatuses(mavenCacheWarm);
    List<RunnerImageStatusResponse> lspImages = lspImageStatuses(lspWarmStamp);
    return new RunnerOpsStatusResponse(
        dockerAvailable,
        properties.dockerEnabled(),
        mavenCacheWarm,
        Files.isRegularFile(repoRoot.resolve("scripts/lsp_warm.py")),
        !lspWarmStamp.isEmpty(),
        repoRoot.toString(),
        mavenCacheVolume(),
        runnerImages,
        lspImages,
        activeJobId);
  }

  public RunnerOpsJobResponse startMavenWarm(boolean force) {
    ensureDockerReady();
    return startJob(
        "MAVEN_WARM",
        () -> {
          try {
            runMavenWarm(force);
          } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex.getMessage(), ex);
          }
        });
  }

  public RunnerOpsJobResponse startLspWarm(boolean force, List<String> only) {
    ensureDockerReady();
    Path script = resolveRepoRoot().resolve("scripts/lsp_warm.py");
    if (!Files.isRegularFile(script)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "LSP warm script not found. Set CTL_REPO_ROOT or run the API from the repo checkout.");
    }
    List<String> labels = normalizeLspLabels(only);
    return startJob(
        "LSP_WARM",
        () -> {
          try {
            runLspWarm(script, force, labels);
          } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex.getMessage(), ex);
          }
        });
  }

  public Optional<RunnerOpsJobResponse> job(UUID jobId) {
    JobState state = jobs.get(jobId);
    return state == null ? Optional.empty() : Optional.of(state.toResponse());
  }

  private RunnerOpsJobResponse startJob(String type, Runnable action) {
    JobState running = findRunningJob();
    if (running != null) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "A runner ops job is already running: " + running.type);
    }
    JobState job = new JobState(UUID.randomUUID(), type, Instant.now());
    jobs.put(job.id, job);
    activeJobId = job.id;
    executor.submit(
        () -> {
          try {
            action.run();
            job.complete("Completed successfully.");
          } catch (Exception ex) {
            job.fail(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
          } finally {
            if (job.id.equals(activeJobId)) {
              activeJobId = null;
            }
            pruneOldJobs();
          }
        });
    return job.toResponse();
  }

  private JobState findRunningJob() {
    for (JobState job : jobs.values()) {
      if ("RUNNING".equals(job.status)) {
        return job;
      }
    }
    return null;
  }

  private void runMavenWarm(boolean force) throws IOException, InterruptedException {
    JobState job = requireActiveJob();
    if (!force && isMavenCacheWarm()) {
      job.appendLog("Maven cache volume already warm.\n");
      return;
    }
    String image = properties.runnerJava26Image();
    String volume = mavenCacheVolume();
    ensureImagePresent(image, "Java 26 runner");
    List<String> command =
        List.of(
            "docker",
            "run",
            "--rm",
            "--label",
            "ctl.maven-warm=true",
            "-v",
            volume + ":/tmp/home/.m2",
            "-u",
            "0:0",
            "--entrypoint",
            "/bin/bash",
            image,
            "-c",
            """
            set -euo pipefail
            mkdir -p /tmp/home/.m2/repository
            if [ ! -f /tmp/home/.m2/repository/.warm ]; then
              cp -a /opt/m2/repository/. /tmp/home/.m2/repository/
              touch /tmp/home/.m2/repository/.warm
              echo "Maven cache volume warmed."
            else
              echo "Maven cache volume already warm."
            fi
            """);
    runProcess(job, command, resolveRepoRoot());
  }

  private void runLspWarm(Path script, boolean force, List<String> labels)
      throws IOException, InterruptedException {
    JobState job = requireActiveJob();
    List<String> command = new ArrayList<>();
    command.add("python3");
    command.add(script.toString());
    if (force) {
      command.add("--force");
    }
    for (String label : labels) {
      command.add("--only");
      command.add(label);
    }
    runProcess(job, command, script.getParent().getParent());
  }

  private JobState requireActiveJob() {
    UUID id = activeJobId;
    if (id == null) {
      throw new IllegalStateException("No active runner ops job");
    }
    JobState job = jobs.get(id);
    if (job == null) {
      throw new IllegalStateException("Active runner ops job not found");
    }
    return job;
  }

  private void runProcess(JobState job, List<String> command, Path workDir)
      throws IOException, InterruptedException {
    job.appendLog("$ " + String.join(" ", command) + "\n");
    ProcessBuilder builder = new ProcessBuilder(command);
    builder.directory(workDir.toFile());
    builder.redirectErrorStream(true);
    Process process = builder.start();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        job.appendLog(line + "\n");
      }
    }
    int exit = process.waitFor();
    if (exit != 0) {
      throw new IllegalStateException("Command failed with exit code " + exit);
    }
  }

  private List<String> normalizeLspLabels(List<String> only) {
    if (only == null || only.isEmpty()) {
      return LSP_WARM_LABELS;
    }
    List<String> labels =
        only.stream()
            .map(label -> label == null ? "" : label.trim().toLowerCase())
            .filter(label -> !label.isBlank())
            .toList();
    for (String label : labels) {
      if (!LSP_WARM_LABELS.contains(label)) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Unsupported LSP warm label: " + label);
      }
    }
    return labels;
  }

  private List<RunnerImageStatusResponse> runnerImageStatuses(boolean mavenCacheWarm) {
    Map<UUID, LanguageEntity> languages =
        languageRepository.findAll().stream()
            .collect(Collectors.toMap(LanguageEntity::getId, language -> language));
    LinkedHashMap<String, RunnerImageStatusResponse> unique = new LinkedHashMap<>();
    for (LanguageRuntimeEntity runtime : runtimeRepository.findAllOrdered()) {
      LanguageEntity language = languages.get(runtime.getLanguageId());
      String label =
          (language == null ? "unknown" : language.getName()) + " " + runtime.getVersion();
      unique.putIfAbsent(
          runtime.getDockerImage(),
          inspectRunnerImage(label, runtime.getDockerImage(), mavenCacheWarm));
    }
    return unique.values().stream()
        .sorted(Comparator.comparing(RunnerImageStatusResponse::label))
        .toList();
  }

  private List<RunnerImageStatusResponse> lspImageStatuses(Map<String, String> lspWarmStamp) {
    LinkedHashSet<String> seen = new LinkedHashSet<>();
    List<RunnerImageStatusResponse> statuses = new ArrayList<>();
    for (String label : LSP_WARM_LABELS) {
      String image = properties.lspImageFor(label);
      if (image == null || image.isBlank() || !seen.add(image)) {
        continue;
      }
      statuses.add(inspectLspImage(label, image, lspWarmStamp));
    }
    return statuses;
  }

  private RunnerImageStatusResponse inspectRunnerImage(
      String label, String image, boolean mavenCacheWarm) {
    ImageInspect inspect = inspectDockerImage(image);
    Boolean warmed = null;
    if (label.toLowerCase().startsWith("java")) {
      warmed = inspect.present() && mavenCacheWarm;
    }
    return new RunnerImageStatusResponse(
        label, image, inspect.present(), inspect.imageId(), warmed);
  }

  private RunnerImageStatusResponse inspectLspImage(
      String label, String image, Map<String, String> lspWarmStamp) {
    ImageInspect inspect = inspectDockerImage(image);
    Boolean warmed = null;
    if (inspect.present() && inspect.imageId() != null) {
      String stampedId = lspWarmStamp.get(label + ":" + image);
      warmed = stampedId != null && stampedId.equals(inspect.imageId());
    } else if (inspect.present()) {
      warmed = false;
    }
    return new RunnerImageStatusResponse(
        label, image, inspect.present(), inspect.imageId(), warmed);
  }

  private ImageInspect inspectDockerImage(String image) {
    if (!isDockerAvailable()) {
      return new ImageInspect(false, null);
    }
    try {
      ProcessBuilder builder =
          new ProcessBuilder("docker", "image", "inspect", image, "--format", "{{.Id}}");
      Process process = builder.start();
      String output;
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        output = reader.readLine();
      }
      int exit = process.waitFor();
      if (exit != 0 || output == null || output.isBlank()) {
        return new ImageInspect(false, null);
      }
      return new ImageInspect(true, output.trim());
    } catch (IOException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      return new ImageInspect(false, null);
    }
  }

  private Map<String, String> loadLspWarmStamp(Path repoRoot) {
    Path stampFile = repoRoot.resolve(".ctl-lsp-warm-stamp");
    if (!Files.isRegularFile(stampFile)) {
      return Map.of();
    }
    try {
      Map<String, String> stamp =
          jsonMapper.readValue(Files.readString(stampFile), new TypeReference<>() {});
      return stamp == null ? Map.of() : stamp;
    } catch (IOException ex) {
      return Map.of();
    }
  }

  private record ImageInspect(boolean present, String imageId) {}

  private void ensureImagePresent(String image, String label) {
    if (!inspectDockerImage(image).present()) {
      throw new IllegalStateException("Docker image missing: " + image + " (" + label + ")");
    }
  }

  private boolean isMavenCacheWarm() {
    if (!isDockerAvailable()) {
      return false;
    }
    try {
      Process process =
          new ProcessBuilder(
                  "docker",
                  "run",
                  "--rm",
                  "-v",
                  mavenCacheVolume() + ":/cache",
                  "alpine:3.20",
                  "test",
                  "-f",
                  "/cache/repository/.warm")
              .start();
      return process.waitFor() == 0;
    } catch (IOException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private void ensureDockerReady() {
    if (!properties.dockerEnabled()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Docker integration is disabled.");
    }
    if (!isDockerAvailable()) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "Docker CLI is not available on the API host.");
    }
  }

  private boolean isDockerAvailable() {
    try {
      Process process = new ProcessBuilder("docker", "info").redirectErrorStream(true).start();
      return process.waitFor() == 0;
    } catch (IOException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private String mavenCacheVolume() {
    String volume = properties.runnerMavenCacheVolume();
    return volume == null || volume.isBlank() ? "ctl-runner-m2-cache" : volume.trim();
  }

  private Path resolveRepoRoot() {
    String configured = environment.getProperty("ctl.repo-root", "");
    if (configured != null && !configured.isBlank()) {
      return Path.of(configured).toAbsolutePath().normalize();
    }
    Path current = Path.of("").toAbsolutePath();
    for (Path candidate = current; candidate != null; candidate = candidate.getParent()) {
      if (Files.isRegularFile(candidate.resolve("scripts/lsp_warm.py"))) {
        return candidate;
      }
    }
    return current;
  }

  private void pruneOldJobs() {
    if (jobs.size() <= 20) {
      return;
    }
    jobs.entrySet().stream()
        .filter(entry -> !"RUNNING".equals(entry.getValue().status))
        .sorted(Comparator.comparing(entry -> entry.getValue().finishedAt, Comparator.nullsLast(Comparator.naturalOrder())))
        .limit(jobs.size() - 20L)
        .map(Map.Entry::getKey)
        .toList()
        .forEach(jobs::remove);
  }

  private static final class JobState {
    private final UUID id;
    private final String type;
    private final Instant startedAt;
    private volatile String status = "RUNNING";
    private volatile Instant finishedAt;
    private volatile String message = "Running…";
    private final StringBuilder log = new StringBuilder();

    private JobState(UUID id, String type, Instant startedAt) {
      this.id = id;
      this.type = type;
      this.startedAt = startedAt;
    }

    private synchronized void appendLog(String chunk) {
      log.append(chunk);
      if (log.length() > LOG_TAIL_MAX) {
        log.delete(0, log.length() - LOG_TAIL_MAX);
      }
    }

    private synchronized void complete(String message) {
      this.status = "COMPLETED";
      this.message = message;
      this.finishedAt = Instant.now();
    }

    private synchronized void fail(String message) {
      this.status = "FAILED";
      this.message = message;
      this.finishedAt = Instant.now();
    }

    private synchronized RunnerOpsJobResponse toResponse() {
      return new RunnerOpsJobResponse(
          id,
          type,
          status,
          startedAt,
          finishedAt,
          message,
          log.toString());
    }
  }
}
