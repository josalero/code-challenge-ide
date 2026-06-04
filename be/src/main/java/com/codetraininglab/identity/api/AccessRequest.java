package com.codetraininglab.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccessRequest(
    @NotBlank @Email @Size(max = 320) String email,
    @NotBlank @Size(max = 200) String fullName,
    @Size(max = 2000) String message) {}
