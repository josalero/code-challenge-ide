package com.codetraininglab.identity.api;

import jakarta.validation.constraints.NotNull;

public record UpdateUserIntegrityMonitoringRequest(
    @NotNull Boolean integrityMonitoringDisabled) {}
