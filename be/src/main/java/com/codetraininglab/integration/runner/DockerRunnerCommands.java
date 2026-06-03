package com.codetraininglab.integration.runner;

import com.codetraininglab.domain.WorkspaceLayout;
import com.codetraininglab.platform.config.CtlProperties;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Builds {@code docker run}/{@code docker exec} argument lists for challenge runners. */
final class DockerRunnerCommands {

  static final String POOL_LABEL = "ctl.runner-pool=true";
  static final String POOL_IMAGE_LABEL = "ctl.runner-image";

  private DockerRunnerCommands() {}

  static List<String> buildEphemeralRunCommand(
      Path challengeMountDir,
      String image,
      RunnerJobPayload.RunnerLimits limits,
      String workspaceLayout,
      CtlProperties properties) {
    List<String> command =
        baseRunFlags(limits, readOnlyRootForLayout(workspaceLayout), workspaceLayout);
    command.add("--rm");
    command.add("-i");
    addMavenCacheMount(command, workspaceLayout, properties);
    command.add("-v");
    command.add(challengeMountDir.toAbsolutePath().normalize() + ":/challenge:ro");
    command.add(image);
    return command;
  }

  static List<String> buildPoolCreateCommand(
      String containerName,
      String image,
      RunnerJobPayload.RunnerLimits limits,
      String workspaceLayout,
      CtlProperties properties) {
    List<String> command = baseRunFlags(limits, false, workspaceLayout);
    command.add("-i");
    command.add("-d");
    command.add("--name");
    command.add(containerName);
    command.add("--label");
    command.add(POOL_LABEL);
    command.add("--label");
    command.add(POOL_IMAGE_LABEL + "=" + image);
    command.add("-e");
    command.add("CTL_RUNNER_POOLED=1");
    addMavenCacheMount(command, workspaceLayout, properties);
    // Idle container — jobs run via `docker exec … run.py` so a broken attach/daemon cannot block.
    command.add("--entrypoint");
    command.add("sleep");
    command.add(image);
    command.add("infinity");
    return command;
  }

  static List<String> buildPoolExecCommand(String containerId) {
    return List.of(
        "docker",
        "exec",
        "-i",
        "-e",
        "PYTHONUNBUFFERED=1",
        containerId,
        "python3",
        "-u",
        "/opt/runner/run.py");
  }

  static String poolContainerName(String image) {
    String normalized = image.trim().toLowerCase().replace('/', '-').replace(':', '-');
    StringBuilder safe = new StringBuilder();
    for (int i = 0; i < normalized.length(); i++) {
      char c = normalized.charAt(i);
      if (Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '-') {
        safe.append(c);
      } else {
        safe.append('-');
      }
    }
    String body = safe.toString();
    if (body.length() > 48) {
      body = body.substring(0, 48);
    }
    if (body.isBlank()) {
      body = "runner";
    }
    return "ctl-runner-pool-" + body;
  }

  private static List<String> baseRunFlags(
      RunnerJobPayload.RunnerLimits limits, boolean readOnlyRoot, String workspaceLayout) {
    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("run");
    command.add("--network");
    command.add("none");
    command.add("--cap-drop");
    command.add("ALL");
    command.add("--cap-add");
    command.add("FOWNER");
    addPostgresSqlSandboxFlags(command, workspaceLayout);
    command.add("--security-opt");
    command.add("no-new-privileges:true");
    command.add("--ipc");
    command.add("none");
    command.add("--memory");
    command.add(limits.memoryMb() + "m");
    command.add("--cpus");
    command.add(String.valueOf(limits.cpus()));
    command.add("--pids-limit");
    command.add(String.valueOf(limits.pids()));
    if (readOnlyRoot) {
      command.add("--read-only");
    }
    command.add("--tmpfs");
    command.add("/tmp:rw,exec,size=768m,mode=1777");
    return command;
  }

  private static boolean readOnlyRootForLayout(String workspaceLayout) {
    return !WorkspaceLayout.POSTGRES_SQL.id().equals(workspaceLayout);
  }

  private static void addPostgresSqlSandboxFlags(List<String> command, String workspaceLayout) {
    if (!WorkspaceLayout.POSTGRES_SQL.id().equals(workspaceLayout)) {
      return;
    }
    command.add("--cap-add");
    command.add("SETUID");
    command.add("--cap-add");
    command.add("SETGID");
    command.add("--cap-add");
    command.add("CHOWN");
  }

  private static void addMavenCacheMount(
      List<String> command, String workspaceLayout, CtlProperties properties) {
    String mavenCacheVolume = properties.runnerMavenCacheVolume();
    if (WorkspaceLayout.MAVEN.id().equals(workspaceLayout)
        && mavenCacheVolume != null
        && !mavenCacheVolume.isBlank()) {
      command.add("-v");
      command.add(mavenCacheVolume + ":/tmp/home/.m2:rw");
    }
  }
}
