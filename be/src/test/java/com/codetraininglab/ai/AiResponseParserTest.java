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

  @Test
  void returnsFallbackForEmptyBody() {
    assertThat(AiResponseParser.parse("openrouter", " "))
        .isEqualTo("AI returned an empty response.");
  }

  @Test
  void parsesOpenRouterTextField() {
    String body = "{\"choices\":[{\"text\":\"Hint: sort the input first.\"}]}";
    assertThat(AiResponseParser.parse("openrouter", body))
        .isEqualTo("Hint: sort the input first.");
  }

  @Test
  void returnsTrimmedRawBodyWhenJsonDoesNotMatch() {
    assertThat(AiResponseParser.parse("openrouter", "plain-text-response"))
        .isEqualTo("plain-text-response");
  }

  @Test
  void returnsTrimmedRawBodyWhenJsonIsInvalid() {
    assertThat(AiResponseParser.parse("openrouter", "{not-json"))
        .isEqualTo("{not-json");
  }
}
