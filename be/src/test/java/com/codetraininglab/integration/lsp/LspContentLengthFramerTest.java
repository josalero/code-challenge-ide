package com.codetraininglab.integration.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LspContentLengthFramerTest {

  @Test
  void wrapsWebSocketJsonAsStdioFrame() {
    String json = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\"}";
    byte[] frame = LspContentLengthFramer.toStdioFrame(json);
    String text = new String(frame, StandardCharsets.UTF_8);
    assertThat(text).startsWith("Content-Length: " + json.length() + "\r\n\r\n");
    assertThat(text).endsWith(json);
  }

  @Test
  void extractsCompleteMessagesFromStdioStream() {
    String json = "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{}}";
    byte[] stdout =
        ("Content-Length: " + json.length() + "\r\n\r\n" + json).getBytes(StandardCharsets.UTF_8);

    LspContentLengthFramer framer = new LspContentLengthFramer();
    assertThat(framer.feed(stdout, 0, stdout.length)).containsExactly(json);
  }

  @Test
  void handlesChunkedStdioReads() {
    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"window/logMessage\"}";
    byte[] stdout =
        ("Content-Length: " + json.length() + "\r\n\r\n" + json).getBytes(StandardCharsets.UTF_8);

    LspContentLengthFramer framer = new LspContentLengthFramer();
    assertThat(framer.feed(stdout, 0, 10)).isEmpty();
    assertThat(framer.feed(stdout, 10, stdout.length - 10)).containsExactly(json);
  }
}
