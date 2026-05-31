package com.codetraininglab.coach.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AiResponseParserTest {

  @Test
  void parsesOpenRouterMessageContent() {
    String body =
        """
        {"choices":[{"message":{"content":"Try extracting the substring first."}}]}
        """;
    assertThat(AiResponseParser.parse(com.codetraininglab.domain.AiProvider.OPENROUTER, body))
        .isEqualTo("Try extracting the substring first.");
  }

  @Test
  void parsesOllamaResponseField() {
    String body = "{\"response\":\"Use a two-pointer approach.\"}";
    assertThat(AiResponseParser.parse(com.codetraininglab.domain.AiProvider.OLLAMA, body))
        .isEqualTo("Use a two-pointer approach.");
  }
}
