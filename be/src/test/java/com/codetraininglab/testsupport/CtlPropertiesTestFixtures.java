package com.codetraininglab.testsupport;

import com.codetraininglab.platform.config.CtlProperties;
import java.util.Map;

public final class CtlPropertiesTestFixtures {

  public static final Map<String, String> TEST_LSP_IMAGES = Map.of("java", "lsp");

  private CtlPropertiesTestFixtures() {}

  public static CtlProperties defaults() {
    return defaults("challenges");
  }

  public static CtlProperties defaults(String challengesPath) {
    return new CtlProperties(
        true,
        "test-jwt-secret-must-be-at-least-32-characters-long",
        24,
        "http://localhost:5173",
        challengesPath,
        "runner",
        "",
        true,
        60,
        TEST_LSP_IMAGES,
        5,
        24,
        "openrouter",
        "",
        "model",
        "http://localhost:11434",
        "ollama",
        false,
        false,
        false,
        0);
  }
}
