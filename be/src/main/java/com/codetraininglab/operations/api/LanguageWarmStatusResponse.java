package com.codetraininglab.operations.api;

/** Per-language runner + editor (LSP) preload state for the Ops dashboard. */
public record LanguageWarmStatusResponse(
    String language,
    String version,
    String label,
    String runnerImage,
    boolean runnerPresent,
    Boolean runnerReady,
    Boolean editorReady,
    boolean ready) {}
