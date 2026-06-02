package com.codetraininglab.integration.lsp;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts between LSP stdio framing ({@code Content-Length: …\r\n\r\n} + JSON body) and raw JSON
 * messages used by {@code vscode-ws-jsonrpc} over WebSocket.
 */
public final class LspContentLengthFramer {

  private static final byte[] HEADER_END = new byte[] {'\r', '\n', '\r', '\n'};

  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

  /** Parses stdout bytes from the language server; returns complete JSON-RPC message bodies. */
  public List<String> feed(byte[] chunk, int offset, int length) {
    buffer.write(chunk, offset, length);
    return drainMessages();
  }

  public static byte[] toStdioFrame(String json) {
    byte[] body = json.getBytes(StandardCharsets.UTF_8);
    byte[] header =
        ("Content-Length: " + body.length + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
    byte[] frame = new byte[header.length + body.length];
    System.arraycopy(header, 0, frame, 0, header.length);
    System.arraycopy(body, 0, frame, header.length, body.length);
    return frame;
  }

  private List<String> drainMessages() {
    byte[] data = buffer.toByteArray();
    List<String> messages = new ArrayList<>();
    int pos = 0;
    while (pos < data.length) {
      int headerEnd = indexOf(data, pos, HEADER_END);
      if (headerEnd < 0) {
        break;
      }
      String headerBlock = new String(data, pos, headerEnd - pos, StandardCharsets.UTF_8);
      int contentLength = parseContentLength(headerBlock);
      int bodyStart = headerEnd + HEADER_END.length;
      if (data.length - bodyStart < contentLength) {
        break;
      }
      messages.add(new String(data, bodyStart, contentLength, StandardCharsets.UTF_8));
      pos = bodyStart + contentLength;
    }
    buffer.reset();
    if (pos < data.length) {
      buffer.write(data, pos, data.length - pos);
    }
    return messages;
  }

  private static int parseContentLength(String headerBlock) {
    for (String line : headerBlock.split("\r\n")) {
      if (line.regionMatches(true, 0, "Content-Length:", 0, "Content-Length:".length())) {
        return Integer.parseInt(line.substring("Content-Length:".length()).trim());
      }
    }
    throw new IllegalArgumentException("LSP header missing Content-Length");
  }

  private static int indexOf(byte[] data, int from, byte[] pattern) {
    outer:
    for (int i = from; i <= data.length - pattern.length; i++) {
      for (int j = 0; j < pattern.length; j++) {
        if (data[i + j] != pattern[j]) {
          continue outer;
        }
      }
      return i;
    }
    return -1;
  }
}
