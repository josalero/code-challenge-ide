package com.codetraininglab.integration.lsp;

import com.codetraininglab.platform.config.CtlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class LspJavaWebSocketHandler extends TextWebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(LspJavaWebSocketHandler.class);

  private final CtlProperties properties;
  private final LspSessionRegistry registry;

  public LspJavaWebSocketHandler(CtlProperties properties, LspSessionRegistry registry) {
    this.properties = properties;
    this.registry = registry;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    if (!properties.lspEnabled()) {
      session.close(CloseStatus.SERVICE_RESTARTED.withReason("LSP is disabled"));
      return;
    }
    String solution = (String) session.getAttributes().getOrDefault("solutionCode", "");
    try {
      LspJavaSession lspSession =
          LspJavaSession.start(session, properties.lspJavaImage(), solution);
      registry.register(session, lspSession);
    } catch (Exception e) {
      log.warn("Failed to start Java LSP session: {}", e.getMessage());
      session.close(CloseStatus.SERVER_ERROR.withReason("Could not start Java language server"));
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    LspJavaSession lspSession = registry.get(session);
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
}
