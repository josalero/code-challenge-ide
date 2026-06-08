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
    boolean runnerPoolWarmOnStartup,
    int userMaxStartedChallenges) {

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

  public String runnerImageFor(String language, String version, String fallbackImage) {
    String configured = runnerImageFromEnvironment(language, version);
    if (configured != null && !configured.isBlank()) {
      return configured;
    }
    if (fallbackImage != null && !fallbackImage.isBlank()) {
      return fallbackImage;
    }
    if ("java".equalsIgnoreCase(language) && "26".equals(version)) {
      return runnerJava26Image;
    }
    return null;
  }

  private static String runnerImageFromEnvironment(String language, String version) {
    if (language == null || language.isBlank()) {
      return null;
    }
    String envName =
        switch (language.trim().toLowerCase()) {
          case "java" ->
              switch (version) {
                case "25" -> "RUNNER_JAVA_25_IMAGE";
                case "26" -> "RUNNER_JAVA_26_IMAGE";
                default -> null;
              };
          case "python" -> "RUNNER_PYTHON_312_IMAGE";
          case "go" -> "RUNNER_GO_123_IMAGE";
          case "node" -> "RUNNER_NODE_22_IMAGE";
          case "csharp" -> "RUNNER_DOTNET_8_IMAGE";
          case "typescript" -> "RUNNER_TYPESCRIPT_57_IMAGE";
          case "rust" -> "RUNNER_RUST_184_IMAGE";
          case "cpp" -> "RUNNER_CPP_20_IMAGE";
          case "react" -> "RUNNER_REACT_19_IMAGE";
          case "vue" -> "RUNNER_VUE_35_IMAGE";
          case "angular" -> "RUNNER_ANGULAR_19_IMAGE";
          case "sql" -> "RUNNER_POSTGRES_17_IMAGE";
          default -> null;
        };
    if (envName == null) {
      return null;
    }
    return System.getenv(envName);
  }

  private static String defaultLspImage(String language) {
    return switch (language) {
      case "java" -> "code-challenge-ide-pro-lsp-java:local";
      case "python" -> "code-challenge-ide-pro-lsp-python:local";
      case "go" -> "code-challenge-ide-pro-lsp-go:local";
      case "node", "typescript", "react", "vue", "angular" ->
          "code-challenge-ide-pro-lsp-typescript:local";
      case "csharp" -> "code-challenge-ide-pro-lsp-dotnet:local";
      case "rust" -> "code-challenge-ide-pro-lsp-rust:local";
      case "cpp" -> "code-challenge-ide-pro-lsp-cpp:local";
      default -> null;
    };
  }
}
