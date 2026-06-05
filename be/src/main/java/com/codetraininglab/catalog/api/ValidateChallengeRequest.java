package com.codetraininglab.catalog.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ValidateChallengeRequest(
    @Size(max = 64)
        @Pattern(
            regexp = "^$|^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "slug must be lowercase letters, numbers, and hyphens")
        String slug,
    @NotBlank String language,
    @NotBlank String defaultRuntimeVersion,
    @NotBlank String starterCode,
    @NotEmpty @Valid List<ChallengeTestPayload> publicTests,
    @NotEmpty @Valid List<ChallengeTestPayload> hiddenTests) {}
