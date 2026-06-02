package com.codetraininglab.catalog.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateChallengeRequest(
    @NotBlank
        @Size(min = 2, max = 64)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug must be lowercase letters, numbers, and hyphens")
        String slug,
    @NotBlank @Size(max = 200) String title,
    @NotBlank String descriptionMd,
    @NotBlank
        @Pattern(regexp = "^(?i)(easy|medium|hard)$", message = "difficulty must be easy, medium, or hard")
        String difficulty,
    @NotBlank String language,
    @NotBlank String defaultRuntimeVersion,
    @NotBlank String starterCode,
    @Min(0) @Max(100) int lineCoveragePercent,
    @NotEmpty @Valid List<ChallengeTestPayload> publicTests,
    @NotEmpty @Valid List<ChallengeTestPayload> hiddenTests) {}
