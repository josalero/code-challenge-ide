package com.codetraininglab.identity.api;

import java.util.UUID;

public record CreateUserResponse(
    UUID id, String email, String fullName, String temporaryPassword, String role) {}
