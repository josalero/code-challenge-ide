package com.codetraininglab.operations.api;

import java.util.List;

public record RunnerWarmRequest(boolean force, List<String> only) {}
