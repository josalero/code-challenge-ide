package com.codetraininglab.integration.runner;

import com.codetraininglab.domain.WorkspaceLayout;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.domain.RunnerStatus;
import com.codetraininglab.domain.TestOutcomeStatus;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestEntity;
import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import tools.jackson.databind.json.JsonMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ctl.docker-enabled", havingValue = "true", matchIfMissing = true)
public class DockerRunnerClient implements RunnerClient {

  private final CtlProperties properties;
  private final JsonMapper jsonMapper;
  private final LanguageRuntimeRepository runtimeRepository;
  private final LanguageRepository languageRepository;
  private final RunnerContainerPool runnerContainerPool;

  public DockerRunnerClient(
      CtlProperties properties,
      JsonMapper jsonMapper,
      LanguageRuntimeRepository runtimeRepository,
      LanguageRepository languageRepository,
      RunnerContainerPool runnerContainerPool) {
    this.properties = properties;
    this.jsonMapper = jsonMapper;
    this.runtimeRepository = runtimeRepository;
    this.languageRepository = languageRepository;
    this.runnerContainerPool = runnerContainerPool;
  }

  @Override
  public RunnerResult execute(
      SubmissionEntity submission,
      String challengeSlug,
      List<ChallengeHiddenTestEntity> hiddenTests,
      Path challengeDir,
      String runnerImage) {
    RunnerJobPayload.RunnerLimits limits = RunnerJobPayload.RunnerLimits.defaults();
    try {
      RuntimeContext runtime = runtimeContext(submission);
      RunnerJobPayload job =
          buildJob(submission, challengeSlug, hiddenTests, limits, runtime.workspaceLayout());
      String jobJson = jsonMapper.writeValueAsString(job);
      String image = resolveRunnerImage(runnerImage, runtime.language(), runtime.version());
      if (runnerContainerPool.isEnabled()) {
        return runnerContainerPool.execute(
            image, challengeDir, runtime.workspaceLayout(), jobJson, limits);
      }
      return executeEphemeral(image, challengeDir, runtime.workspaceLayout(), jobJson, limits);
    } catch (Exception e) {
      return failedResult(e.getMessage() == null ? "Runner error" : e.getMessage());
    }
  }

  private RunnerResult executeEphemeral(
      String image,
      Path challengeDir,
      String workspaceLayout,
      String jobJson,
      RunnerJobPayload.RunnerLimits limits)
      throws Exception {
    Path mountDir = challengeDir.toAbsolutePath().normalize();
    List<String> command =
        buildDockerRunCommand(mountDir, image, limits, workspaceLayout);

    ProcessBuilder builder = new ProcessBuilder(command);
    Process process = builder.start();
    process.getOutputStream().write(jobJson.getBytes(StandardCharsets.UTF_8));
    process.getOutputStream().close();

    CompletableFuture<Void> stderrDrain =
        CompletableFuture.runAsync(
            () -> {
              try {
                process.getErrorStream().transferTo(java.io.OutputStream.nullOutputStream());
              } catch (Exception ignored) {
                // Best-effort drain so a full stderr pipe cannot block the runner.
              }
            });

    String line;
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      line = reader.readLine();
    }

    long waitSeconds = limits.wallSeconds() + 10L;
    boolean finished = process.waitFor(waitSeconds, TimeUnit.SECONDS);
    stderrDrain.join();
    if (!finished) {
      process.destroyForcibly();
      return failedResult("Runner timed out");
    }
    if (line == null || line.isBlank()) {
      return failedResult("Runner produced no output");
    }
    return jsonMapper.readValue(line, RunnerResult.class);
  }

  private RunnerJobPayload buildJob(
      SubmissionEntity submission,
      String challengeSlug,
      List<ChallengeHiddenTestEntity> hiddenTests,
      RunnerJobPayload.RunnerLimits limits,
      String workspaceLayout) {
    List<RunnerJobPayload.HiddenTest> hidden =
        hiddenTests.stream()
            .map(h -> new RunnerJobPayload.HiddenTest(h.getName(), h.getTestSource()))
            .toList();
    return new RunnerJobPayload(
        submission.getId().toString(),
        challengeSlug,
        workspaceLayout,
        submission.getSolutionCode(),
        submission.getCustomTestsCode(),
        hidden,
        limits);
  }

  private RuntimeContext runtimeContext(SubmissionEntity submission) {
    LanguageRuntimeEntity runtime =
        runtimeRepository.findById(submission.getRuntimeId()).orElseThrow();
    LanguageEntity language =
        languageRepository.findById(runtime.getLanguageId()).orElseThrow();
    String languageName = language.getName().toLowerCase();
    return new RuntimeContext(
        languageName, runtime.getVersion(), WorkspaceLayout.forLanguage(languageName).id());
  }

  List<String> buildDockerRunCommand(
      Path challengeMountDir,
      String image,
      RunnerJobPayload.RunnerLimits limits,
      String workspaceLayout) {
    return DockerRunnerCommands.buildEphemeralRunCommand(
        challengeMountDir, image, limits, workspaceLayout, properties);
  }

  private String resolveRunnerImage(String runnerImage, String language, String version) {
    String resolved = properties.runnerImageFor(language, version, runnerImage);
    if (resolved != null && !resolved.isBlank()) {
      return resolved;
    }
    return properties.runnerJava26Image();
  }

  private record RuntimeContext(String language, String version, String workspaceLayout) {}

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
}
