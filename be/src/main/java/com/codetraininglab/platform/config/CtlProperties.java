package com.codetraininglab.platform.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ctl")
public record CtlProperties(
    boolean registrationEnabled,
    String jwtSecret,
    int jwtExpirationHours,
    String corsAllowedOrigins,
    String challengesPath,
    String runnerJava26Image,
    String runnerMavenCacheVolume,
    boolean runnerPoolEnabled,
    int runnerPoolIdleMinutes,
    Map<String, String> lspImages,
    int lspIdleMinutes,
    int idempotencyTtlHours,
    String aiProvider,
    String openrouterApiKey,
    String openrouterModel,
    String ollamaBaseUrl,
    String ollamaModel,
    boolean dockerEnabled,
    boolean lspEnabled,
    boolean runnerPoolWarmOnStartup) {

  public String lspImageFor(String language) {
    if (language == null || language.isBlank()) {
      return null;
    }
    String key = language.trim().toLowerCase();
    if (lspImages != null) {
      String configured = lspImages.get(key);
      if (configured != null && !configured.isBlank()) {
        return configured;
      }
    }
    return defaultLspImage(key);
  }

  private static String defaultLspImage(String language) {
    return switch (language) {
      case "java" -> "code-challenge-ide-lsp-java:local";
      case "python" -> "code-challenge-ide-lsp-python:local";
      case "go" -> "code-challenge-ide-lsp-go:local";
      case "node", "typescript", "react", "vue", "angular" ->
          "code-challenge-ide-lsp-typescript:local";
      case "csharp" -> "code-challenge-ide-lsp-dotnet:local";
      case "rust" -> "code-challenge-ide-lsp-rust:local";
      case "cpp" -> "code-challenge-ide-lsp-cpp:local";
      default -> null;
    };
  }
}
