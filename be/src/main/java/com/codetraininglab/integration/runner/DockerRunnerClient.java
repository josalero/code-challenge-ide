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
import java.util.ArrayList;
import java.util.List;
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

  public DockerRunnerClient(
      CtlProperties properties,
      JsonMapper jsonMapper,
      LanguageRuntimeRepository runtimeRepository,
      LanguageRepository languageRepository) {
    this.properties = properties;
    this.jsonMapper = jsonMapper;
    this.runtimeRepository = runtimeRepository;
    this.languageRepository = languageRepository;
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
      RunnerJobPayload job = buildJob(submission, hiddenTests, limits, workspaceLayout(submission));
      String jobJson = jsonMapper.writeValueAsString(job);
      Path mountDir = challengeDir.toAbsolutePath().normalize();
      List<String> command =
          buildDockerRunCommand(
              mountDir, resolveRunnerImage(runnerImage), limits, job.workspaceLayout());

      ProcessBuilder builder = new ProcessBuilder(command);
      Process process = builder.start();
      process.getOutputStream().write(jobJson.getBytes(StandardCharsets.UTF_8));
      process.getOutputStream().close();

      String line;
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        line = reader.readLine();
      }
      process.getErrorStream().transferTo(java.io.OutputStream.nullOutputStream());

      boolean finished = process.waitFor(limits.wallSeconds() + 10L, TimeUnit.SECONDS);
      if (!finished) {
        process.destroyForcibly();
        return failedResult("Runner timed out");
      }
      if (line == null || line.isBlank()) {
        return failedResult("Runner produced no output");
      }
      return jsonMapper.readValue(line, RunnerResult.class);
    } catch (Exception e) {
      return failedResult(e.getMessage() == null ? "Runner error" : e.getMessage());
    }
  }

  private RunnerJobPayload buildJob(
      SubmissionEntity submission,
      List<ChallengeHiddenTestEntity> hiddenTests,
      RunnerJobPayload.RunnerLimits limits,
      String workspaceLayout) {
    List<RunnerJobPayload.HiddenTest> hidden =
        hiddenTests.stream()
            .map(h -> new RunnerJobPayload.HiddenTest(h.getName(), h.getTestSource()))
            .toList();
    return new RunnerJobPayload(
        submission.getId().toString(),
        workspaceLayout,
        submission.getSolutionCode(),
        submission.getCustomTestsCode(),
        hidden,
        limits);
  }

  private String workspaceLayout(SubmissionEntity submission) {
    LanguageRuntimeEntity runtime =
        runtimeRepository.findById(submission.getRuntimeId()).orElseThrow();
    LanguageEntity language =
        languageRepository.findById(runtime.getLanguageId()).orElseThrow();
    return WorkspaceLayout.forLanguage(language.getName()).id();
  }

  List<String> buildDockerRunCommand(
      Path challengeMountDir,
      String image,
      RunnerJobPayload.RunnerLimits limits,
      String workspaceLayout) {
    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("run");
    command.add("--rm");
    command.add("-i");
    command.add("--network");
    command.add("none");
    command.add("--memory");
    command.add(limits.memoryMb() + "m");
    command.add("--cpus");
    command.add(String.valueOf(limits.cpus()));
    command.add("--pids-limit");
    command.add(String.valueOf(limits.pids()));
    command.add("--read-only");
    command.add("--tmpfs");
    command.add("/tmp:rw,size=768m,mode=1777");
    String mavenCacheVolume = properties.runnerMavenCacheVolume();
    if (WorkspaceLayout.MAVEN.id().equals(workspaceLayout)
        && mavenCacheVolume != null
        && !mavenCacheVolume.isBlank()) {
      command.add("-v");
      command.add(mavenCacheVolume + ":/tmp/home/.m2:rw");
    }
    command.add("-v");
    command.add(challengeMountDir + ":/challenge:ro");
    command.add(image);
    return command;
  }

  private String resolveRunnerImage(String runnerImage) {
    if (runnerImage != null && !runnerImage.isBlank()) {
      return runnerImage;
    }
    return properties.runnerJava26Image();
  }

  private RunnerResult failedResult(String message) {
    return new RunnerResult(
        RunnerStatus.FAILED.name(),
        List.of(
            new RunnerResult.TestOutcome(
                "runner", TestOutcomeStatus.FAIL.name(), message, 0)),
        new RunnerResult.CoverageOutcome(0, 0),
        new RunnerResult.CheckstyleOutcome(0, 0),
        null);
  }
}
