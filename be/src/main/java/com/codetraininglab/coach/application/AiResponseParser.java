package com.codetraininglab.coach.application;

import com.codetraininglab.domain.AiProvider;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Extracts human-readable text from OpenRouter / Ollama HTTP JSON bodies. */
public final class AiResponseParser {

  private static final JsonMapper MAPPER = JsonMapper.builder().build();

  private AiResponseParser() {}

  public static String parse(AiProvider provider, String rawBody) {
    return parse(provider.configValue(), rawBody);
  }

  public static String parse(String providerConfig, String rawBody) {
    if (rawBody == null || rawBody.isBlank()) {
      return "AI returned an empty response.";
    }
    AiProvider provider = AiProvider.fromConfig(providerConfig);
    try {
      JsonNode root = MAPPER.readTree(rawBody);
      if (provider == AiProvider.OLLAMA) {
        String response = textOrNull(root.path("response"));
        if (response != null) {
          return response;
        }
      } else {
        String content = textOrNull(root.path("choices").path(0).path("message").path("content"));
        if (content != null) {
          return content;
        }
        content = textOrNull(root.path("choices").path(0).path("text"));
        if (content != null) {
          return content;
        }
      }
    } catch (Exception ignored) {
      // fall through — return trimmed raw body
    }
    return rawBody.trim();
  }

  private static String textOrNull(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    String text = node.asText();
    return text.isBlank() ? null : text;
  }
}
