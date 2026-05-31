package com.codetraininglab.domain;

public enum AiProvider {
  OPENROUTER("openrouter"),
  OLLAMA("ollama");

  private final String configValue;

  AiProvider(String configValue) {
    this.configValue = configValue;
  }

  public String configValue() {
    return configValue;
  }

  public static AiProvider fromConfig(String value) {
    if (value == null || value.isBlank()) {
      return OPENROUTER;
    }
    for (AiProvider provider : values()) {
      if (provider.configValue.equalsIgnoreCase(value.trim())) {
        return provider;
      }
    }
    return OPENROUTER;
  }
}
