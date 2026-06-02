package com.codetraininglab.operations.api;

/** @param warmed {@code true}/{@code false} when warm state applies; {@code null} if not applicable (e.g. non-Java runners). */
public record RunnerImageStatusResponse(
    String label,
    String image,
    boolean present,
    String imageId,
    Boolean warmed) {}
