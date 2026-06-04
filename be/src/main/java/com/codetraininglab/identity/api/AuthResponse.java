package com.codetraininglab.identity.api;

import java.util.UUID;

public record AuthResponse(
    String accessToken,
    UUID userId,
    String email,
    String role,
    boolean mustChangePassword) {}
