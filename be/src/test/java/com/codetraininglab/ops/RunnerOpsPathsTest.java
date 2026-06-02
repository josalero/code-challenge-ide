package com.codetraininglab.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class RunnerOpsPathsTest {

  @Mock private Environment environment;

  @TempDir Path tempDir;

  @Test
  void opsDataDirOverridesRepoRootForWarmStamp() {
    Path repo = tempDir.resolve("repo");
    Path ops = tempDir.resolve("ops-data");
    when(environment.getProperty("ctl.ops-data-dir", "")).thenReturn(ops.toString());
    when(environment.getProperty("ctl.repo-root", "")).thenReturn(repo.toString());

    assertThat(RunnerOpsPaths.poolWarmStampFile(environment))
        .isEqualTo(ops.resolve(".ctl-runner-pool-warm-stamp").toAbsolutePath().normalize());
    assertThat(RunnerOpsPaths.resolveRepoRoot(environment))
        .isEqualTo(repo.toAbsolutePath().normalize());
  }
}
