package com.codetraininglab.identity.api;

import java.util.UUID;

public record UserIntegrityMonitoringResponse(
    UUID userId, boolean integrityMonitoringDisabled, boolean effectiveMonitoringEnabled) {}
