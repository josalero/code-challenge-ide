package com.codetraininglab.operations.api;

import java.util.List;
import java.util.UUID;

public record RunnerOpsStatusResponse(
    boolean dockerAvailable,
    boolean dockerEnabled,
    boolean mavenCacheWarm,
    boolean lspScriptsAvailable,
    boolean lspWarmStampPresent,
    boolean runnerPoolWarmStampPresent,
    String repoRoot,
    String opsDataDir,
    String mavenCacheVolume,
    List<RunnerImageStatusResponse> runnerImages,
    List<RunnerImageStatusResponse> lspImages,
    UUID activeJobId) {}
