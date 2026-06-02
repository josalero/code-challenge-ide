package com.codetraininglab.platform.config;

import com.codetraininglab.platform.web.ApiPaths;
import com.codetraininglab.integration.lsp.JwtWebSocketHandshakeInterceptor;
import com.codetraininglab.integration.lsp.LspWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableScheduling
@EnableAsync
public class WebSocketConfig implements WebSocketConfigurer {

  private final LspWebSocketHandler lspHandler;
  private final JwtWebSocketHandshakeInterceptor handshakeInterceptor;
  private final CtlProperties properties;

  public WebSocketConfig(
      LspWebSocketHandler lspHandler,
      JwtWebSocketHandshakeInterceptor handshakeInterceptor,
      CtlProperties properties) {
    this.lspHandler = lspHandler;
    this.handshakeInterceptor = handshakeInterceptor;
    this.properties = properties;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    String[] origins = properties.corsAllowedOrigins().split(",");
    registry
        .addHandler(lspHandler, ApiPaths.LSP_PREFIX + "{language}")
        .addInterceptors(handshakeInterceptor)
        .setAllowedOrigins(origins);
  }
}
