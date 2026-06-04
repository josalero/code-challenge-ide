package com.codetraininglab.identity.api;

import com.codetraininglab.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank @Email @Size(max = 320) String email,
    @NotBlank @Size(max = 200) String fullName,
    @NotBlank @Size(min = 8, max = 128) String temporaryPassword,
    @NotNull UserRole role) {}
