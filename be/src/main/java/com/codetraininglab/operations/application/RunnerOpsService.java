package com.codetraininglab.operations.application;

import com.codetraininglab.operations.api.LanguageWarmStatusResponse;
import com.codetraininglab.operations.api.RunnerImageStatusResponse;
import com.codetraininglab.operations.api.RunnerOpsJobResponse;
import com.codetraininglab.operations.api.RunnerOpsStatusResponse;
import com.codetraininglab.integration.runner.RunnerContainerPool;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class RunnerOpsService {

  private static final int LOG_TAIL_MAX = 8000;
  private static final List<String> LSP_WARM_LABELS =
      List.of("java", "python", "go", "typescript", "csharp", "rust", "cpp", "vue");
  private static final List<String> RUNNER_WARM_LANGUAGES =
      List.of(
          "java",
          "python",
          "go",
          "node",
          "typescript",
          "csharp",
          "rust",
          "cpp",
          "react",
          "vue",
          "angular");

  private final CtlProperties properties;
  private final Environment environment;
  private final LanguageRuntimeRepository runtimeRepository;
  private final LanguageRepository languageRepository;
  private final JsonMapper jsonMapper;
  private final RunnerWarmStateStore warmStateStore;
  private final ObjectProvider<RunnerPoolWarmExecutor> runnerPoolWarmExecutor;
  private final ObjectProvider<RunnerContainerPool> runnerContainerPool;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private final ConcurrentHashMap<UUID, JobState> jobs = new ConcurrentHashMap<>();
  private volatile UUID activeJobId;

  public RunnerOpsService(
      CtlProperties properties,
      Environment environment,
      LanguageRuntimeRepository runtimeRepository,
      LanguageRepository languageRepository,
      JsonMapper jsonMapper,
      RunnerWarmStateStore warmStateStore,
      ObjectProvider<RunnerPoolWarmExecutor> runnerPoolWarmExecutor,
      ObjectProvider<RunnerContainerPool> runnerContainerPool) {
    this.properties = properties;
    this.environment = environment;
    this.runtimeRepository = runtimeRepository;
    this.languageRepository = languageRepository;
    this.jsonMapper = jsonMapper;
    this.warmStateStore = warmStateStore;
    this.runnerPoolWarmExecutor = runnerPoolWarmExecutor;
    this.runnerContainerPool = runnerContainerPool;
  }

  public RunnerOpsStatusResponse status() {
    Path repoRoot = RunnerOpsPaths.resolveRepoRoot(environment);
    Path opsDataDir = RunnerOpsPaths.resolveOpsDataDir(environment);
    boolean dockerAvailable = isDockerAvailable();
    boolean mavenCacheWarm = isMavenCacheWarm();
    Map<String, String> lspWarmStamp = warmStateStore.lspStampByScopeKey();
    Map<String, String> poolWarmStamp = warmStateStore.runnerPoolStampByImage();
    List<RunnerImageStatusResponse> runnerImages = runnerImageStatuses(poolWarmStamp);
    List<RunnerImageStatusResponse> lspImages = lspImageStatuses(lspWarmStamp);
    persistWarmInventoryToDatabase(runnerImages, lspImages);
    List<LanguageWarmStatusResponse> languages = languageWarmStatuses(poolWarmStamp, lspImages);
    return new RunnerOpsStatusResponse(
        dockerAvailable,
        properties.dockerEnabled(),
        mavenCacheWarm,
        Files.isRegularFile(repoRoot.resolve("scripts/lsp_warm.py")),
        !lspWarmStamp.isEmpty(),
        !poolWarmStamp.isEmpty(),
        repoRoot.toString(),
        opsDataDir.toString(),
        mavenCacheVolume(),
        runnerImages,
        lspImages,
        languages,
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
    Path repoRoot = RunnerOpsPaths.resolveRepoRoot(environment);
    Path script = repoRoot.resolve("scripts/lsp_warm.py");
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

  public RunnerOpsJobResponse startRunnerWarm(boolean force, List<String> only) {
    ensureDockerReady();
    if (!properties.runnerPoolEnabled()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Runner pool is disabled (RUNNER_POOL_ENABLED=false).");
    }
    RunnerPoolWarmExecutor executor = runnerPoolWarmExecutor.getIfAvailable();
    if (executor == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Runner pool warm requires Docker integration.");
    }
    List<String> languages = normalizeRunnerWarmLanguages(only);
    return startJob(
        "RUNNER_POOL_WARM",
        () -> {
          try {
            runRunnerPoolWarm(force, languages, executor);
          } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex.getMessage(), ex);
          }
        });
  }

  /** Warms submission runners and editor LSP for the same language set in one job. */
  public RunnerOpsJobResponse startInfraWarm(boolean force, List<String> only) {
    ensureDockerReady();
    if (!properties.runnerPoolEnabled()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Runner pool is disabled (RUNNER_POOL_ENABLED=false).");
    }
    RunnerPoolWarmExecutor executor = runnerPoolWarmExecutor.getIfAvailable();
    if (executor == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Runner warm requires Docker integration.");
    }
    List<String> runnerLanguages = normalizeRunnerWarmLanguages(only);
    List<String> lspLabels = mapRunnerLanguagesToLspLabels(runnerLanguages);
    Path repoRoot = RunnerOpsPaths.resolveRepoRoot(environment);
    Path lspScript = repoRoot.resolve("scripts/lsp_warm.py");
    boolean lspAvailable = Files.isRegularFile(lspScript);
    return startJob(
        "INFRA_WARM",
        () -> {
          try {
            requireActiveJob().appendLog("=== Runner pool smoke ===\n");
            runRunnerPoolWarm(force, runnerLanguages, executor);
            if (lspAvailable && !lspLabels.isEmpty()) {
              requireActiveJob().appendLog("\n=== Editor (LSP) ===\n");
              runLspWarm(lspScript, force, lspLabels);
            } else if (!lspAvailable) {
              requireActiveJob()
                  .appendLog(
                      "\nSkipping LSP warm — scripts/lsp_warm.py not found (set CTL_REPO_ROOT).\n");
            }
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
            try {
              persistWarmInventoryFromDocker();
            } catch (Exception syncEx) {
              // Best-effort: ops UI still works from live docker inspect on next status poll.
            }
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
    warmStateStore.importLspStampFromFile(RunnerOpsPaths.lspWarmStampFile(environment));
  }

  private void persistWarmInventoryFromDocker() {
    Map<String, String> poolWarmStamp = warmStateStore.runnerPoolStampByImage();
    Map<String, String> lspWarmStamp = warmStateStore.lspStampByScopeKey();
    persistWarmInventoryToDatabase(
        runnerImageStatuses(poolWarmStamp), lspImageStatuses(lspWarmStamp));
  }

  private void persistWarmInventoryToDatabase(
      List<RunnerImageStatusResponse> runnerImages, List<RunnerImageStatusResponse> lspImages) {
    warmStateStore.syncRunnerPoolFromStatuses(runnerImages);
    warmStateStore.syncLspFromStatuses(lspImages);
  }

  private void runRunnerPoolWarm(boolean force, List<String> only, RunnerPoolWarmExecutor executor)
      throws IOException, InterruptedException {
    JobState job = requireActiveJob();
    if (needsJavaWarm(only) && !isMavenCacheWarm()) {
      job.appendLog("Maven cache is cold — warming before Java smoke runs…\n");
      runMavenWarm(force);
    }
    executor.warm(force, only, job::appendLog);
  }

  private boolean needsJavaWarm(List<String> only) {
    return only.isEmpty() || only.stream().anyMatch(language -> "java".equalsIgnoreCase(language));
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
    builder.environment().put("CTL_OPS_DATA_DIR", RunnerOpsPaths.resolveOpsDataDir(environment).toString());
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

  static List<String> mapRunnerLanguagesToLspLabels(List<String> runnerLanguages) {
    LinkedHashSet<String> labels = new LinkedHashSet<>();
    for (String language : runnerLanguages) {
      if (language == null || language.isBlank()) {
        continue;
      }
      switch (language.trim().toLowerCase()) {
        case "java" -> labels.add("java");
        case "python" -> labels.add("python");
        case "go" -> labels.add("go");
        case "node", "typescript", "react", "angular" -> labels.add("typescript");
        case "vue" -> {
          labels.add("typescript");
          labels.add("vue");
        }
        case "csharp" -> labels.add("csharp");
        case "rust" -> labels.add("rust");
        case "cpp" -> labels.add("cpp");
        default -> {
          // Unknown runner language — skip LSP mapping.
        }
      }
    }
    return labels.stream().filter(LSP_WARM_LABELS::contains).toList();
  }

  private List<LanguageWarmStatusResponse> languageWarmStatuses(
      Map<String, String> poolWarmStamp, List<RunnerImageStatusResponse> lspImages) {
    Map<String, RunnerImageStatusResponse> lspByLabel =
        lspImages.stream()
            .collect(Collectors.toMap(RunnerImageStatusResponse::label, image -> image, (a, b) -> a));
    Map<UUID, LanguageEntity> languages =
        languageRepository.findAll().stream()
            .collect(Collectors.toMap(LanguageEntity::getId, language -> language));
    List<LanguageWarmStatusResponse> rows = new ArrayList<>();
    LinkedHashSet<String> languagesWithRuntimes = new LinkedHashSet<>();

    for (LanguageRuntimeEntity runtime : runtimeRepository.findAllOrdered()) {
      if (!runtime.isActive()) {
        continue;
      }
      LanguageEntity language = languages.get(runtime.getLanguageId());
      if (language == null) {
        continue;
      }
      String languageName = language.getName().toLowerCase();
      if (!RUNNER_WARM_LANGUAGES.contains(languageName)) {
        continue;
      }
      languagesWithRuntimes.add(languageName);
      String label = languageName + " " + runtime.getVersion();
      RunnerImageStatusResponse runner =
          inspectRunnerImage(label, runtime.getDockerImage(), poolWarmStamp);
      Boolean runnerReady = runnerReadyFromImage(runner);
      Boolean editorReady = editorReadyForLanguage(languageName, lspByLabel);
      boolean ready = Boolean.TRUE.equals(runnerReady) && Boolean.TRUE.equals(editorReady);
      rows.add(
          new LanguageWarmStatusResponse(
              languageName,
              runtime.getVersion(),
              label,
              runtime.getDockerImage(),
              runner.present(),
              runnerReady,
              editorReady,
              ready));
    }

    for (String languageName : RUNNER_WARM_LANGUAGES) {
      if (languagesWithRuntimes.contains(languageName)) {
        continue;
      }
      Boolean editorReady = editorReadyForLanguage(languageName, lspByLabel);
      rows.add(
          new LanguageWarmStatusResponse(
              languageName, null, languageName, null, false, null, editorReady, false));
    }

    return rows.stream()
        .sorted(
            Comparator.comparing(LanguageWarmStatusResponse::language)
                .thenComparing(
                    row -> row.version() == null ? "" : row.version(), Comparator.naturalOrder()))
        .toList();
  }

  private static Boolean runnerReadyFromImage(RunnerImageStatusResponse runner) {
    if (!runner.present()) {
      return false;
    }
    if (runner.warmed() == null || !runner.warmed()) {
      return false;
    }
    return true;
  }

  private static Boolean editorReadyForLanguage(
      String language, Map<String, RunnerImageStatusResponse> lspByLabel) {
    RunnerImageStatusResponse primary = lspByLabel.get(lspLabelForRunnerLanguage(language));
    if (primary != null) {
      return primary.present() ? Boolean.TRUE.equals(primary.warmed()) : false;
    }
    if ("vue".equals(language)) {
      RunnerImageStatusResponse typescript = lspByLabel.get("typescript");
      if (typescript == null) {
        return null;
      }
      return typescript.present() && Boolean.TRUE.equals(typescript.warmed());
    }
    return null;
  }

  private static String lspLabelForRunnerLanguage(String language) {
    return switch (language) {
      case "java" -> "java";
      case "python" -> "python";
      case "go" -> "go";
      case "node", "typescript", "react", "angular" -> "typescript";
      case "vue" -> "vue";
      case "csharp" -> "csharp";
      case "rust" -> "rust";
      case "cpp" -> "cpp";
      default -> language;
    };
  }

  private List<String> normalizeRunnerWarmLanguages(List<String> only) {
    if (only == null || only.isEmpty()) {
      return RUNNER_WARM_LANGUAGES;
    }
    List<String> languages =
        only.stream()
            .map(label -> label == null ? "" : label.trim().toLowerCase())
            .filter(label -> !label.isBlank())
            .toList();
    for (String language : languages) {
      if (!RUNNER_WARM_LANGUAGES.contains(language)) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Unsupported runner warm language: " + language);
      }
    }
    return languages;
  }

  private List<RunnerImageStatusResponse> runnerImageStatuses(Map<String, String> poolWarmStamp) {
    Map<UUID, LanguageEntity> languages =
        languageRepository.findAll().stream()
            .collect(Collectors.toMap(LanguageEntity::getId, language -> language));
    LinkedHashMap<String, RunnerImageStatusResponse> unique = new LinkedHashMap<>();
    for (LanguageRuntimeEntity runtime : runtimeRepository.findAllOrdered()) {
      if (!runtime.isActive()) {
        continue;
      }
      LanguageEntity language = languages.get(runtime.getLanguageId());
      String label =
          (language == null ? "unknown" : language.getName()) + " " + runtime.getVersion();
      unique.putIfAbsent(
          runtime.getDockerImage(),
          inspectRunnerImage(label, runtime.getDockerImage(), poolWarmStamp));
    }
    return unique.values().stream()
        .sorted(Comparator.comparing(RunnerImageStatusResponse::label))
        .toList();
  }

  private List<RunnerImageStatusResponse> lspImageStatuses(Map<String, String> lspWarmStamp) {
    List<RunnerImageStatusResponse> statuses = new ArrayList<>();
    for (String label : LSP_WARM_LABELS) {
      String image = properties.lspImageFor(label);
      if (image == null || image.isBlank()) {
        continue;
      }
      statuses.add(inspectLspImage(label, image, lspWarmStamp));
    }
    return statuses;
  }

  private RunnerImageStatusResponse inspectRunnerImage(
      String label, String image, Map<String, String> poolWarmStamp) {
    ImageInspect inspect = inspectDockerImage(image);
    Boolean warmed = null;
    if (inspect.present()) {
      if (inspect.imageId() != null) {
        String stampedId = poolWarmStamp.get(image);
        boolean stampMatches = stampedId != null && RunnerWarmImageIds.matches(stampedId, inspect.imageId());
        boolean poolRunning = isRunnerPoolRunning(image);
        warmed = stampMatches || poolRunning;
      } else {
        warmed = false;
      }
    }
    return new RunnerImageStatusResponse(
        label, image, inspect.present(), inspect.imageId(), warmed);
  }

  private boolean isRunnerPoolRunning(String image) {
    RunnerContainerPool pool = runnerContainerPool.getIfAvailable();
    return pool != null && pool.isEnabled() && pool.isPoolRunningForImage(image);
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
    return RunnerOpsPaths.resolveRepoRoot(environment);
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
