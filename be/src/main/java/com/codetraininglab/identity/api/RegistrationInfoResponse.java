package com.codetraininglab.identity.api;

public record RegistrationInfoResponse(
    boolean registrationOpen,
    boolean bootstrap,
    boolean accessRequestsEnabled,
    boolean accessRequestsConfigured) {}
