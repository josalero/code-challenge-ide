package com.codetraininglab.operations.application;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.env.Environment;

/** Resolves repo checkout vs durable ops data paths (warm stamps, etc.). */
final class RunnerOpsPaths {

  static final String POOL_WARM_STAMP = ".ctl-runner-pool-warm-stamp";
  static final String LSP_WARM_STAMP = ".ctl-lsp-warm-stamp";

  private RunnerOpsPaths() {}

  static Path resolveOpsDataDir(Environment environment) {
    String configured = environment.getProperty("ctl.ops-data-dir", "");
    if (configured != null && !configured.isBlank()) {
      return Path.of(configured).toAbsolutePath().normalize();
    }
    return resolveRepoRoot(environment);
  }

  static Path resolveRepoRoot(Environment environment) {
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

  static Path poolWarmStampFile(Environment environment) {
    return resolveOpsDataDir(environment).resolve(POOL_WARM_STAMP);
  }

  static Path lspWarmStampFile(Environment environment) {
    return resolveOpsDataDir(environment).resolve(LSP_WARM_STAMP);
  }
}
