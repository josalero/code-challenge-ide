package com.codetraininglab.integration.lsp;

import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.web.ApiPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class LspWebSocketHandler extends TextWebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(LspWebSocketHandler.class);

  private final CtlProperties properties;
  private final LspSessionRegistry registry;

  public LspWebSocketHandler(CtlProperties properties, LspSessionRegistry registry) {
    this.properties = properties;
    this.registry = registry;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    if (!properties.lspEnabled()) {
      session.close(CloseStatus.SERVICE_RESTARTED.withReason("LSP is disabled"));
      return;
    }
    String language = languageFrom(session);
    if (!LspWorkspaceSupport.isSupported(language)) {
      session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unsupported LSP language: " + language));
      return;
    }
    String image = properties.lspImageFor(language);
    if (image == null || image.isBlank()) {
      session.close(CloseStatus.SERVER_ERROR.withReason("No LSP image configured"));
      return;
    }
    String solution = (String) session.getAttributes().getOrDefault("solutionCode", "");
    try {
      LspDockerSession lspSession = LspDockerSession.start(session, language, image, solution);
      registry.register(session, lspSession);
    } catch (Exception e) {
      log.warn("Failed to start {} LSP session: {}", language, e.getMessage());
      session.close(
          CloseStatus.SERVER_ERROR.withReason("Could not start " + language + " language server"));
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    LspDockerSession lspSession = registry.get(session);
    if (lspSession == null) {
      session.close(CloseStatus.SERVER_ERROR);
      return;
    }
    lspSession.forwardClientMessage(message.getPayload());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    registry.remove(session);
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    log.debug("LSP WebSocket transport error: {}", exception.getMessage());
    registry.remove(session);
  }

  static String languageFrom(WebSocketSession session) {
    String path = session.getUri() != null ? session.getUri().getPath() : "";
    if (!path.startsWith(ApiPaths.LSP_PREFIX)) {
      throw new IllegalArgumentException("Invalid LSP path: " + path);
    }
    String language = path.substring(ApiPaths.LSP_PREFIX.length());
    if (language.isBlank() || language.contains("/")) {
      throw new IllegalArgumentException("Invalid LSP language segment: " + language);
    }
    return language.toLowerCase();
  }
}
