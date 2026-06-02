package com.codetraininglab.operations.api;

import java.util.List;

public record LspWarmRequest(boolean force, List<String> only) {}
