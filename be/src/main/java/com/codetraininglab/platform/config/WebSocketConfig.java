package com.codetraininglab.platform.config;

import com.codetraininglab.platform.web.ApiPaths;
import com.codetraininglab.integration.lsp.JwtWebSocketHandshakeInterceptor;
import com.codetraininglab.integration.lsp.LspJavaWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

  private final LspJavaWebSocketHandler lspJavaHandler;
  private final JwtWebSocketHandshakeInterceptor handshakeInterceptor;
  private final CtlProperties properties;

  public WebSocketConfig(
      LspJavaWebSocketHandler lspJavaHandler,
      JwtWebSocketHandshakeInterceptor handshakeInterceptor,
      CtlProperties properties) {
    this.lspJavaHandler = lspJavaHandler;
    this.handshakeInterceptor = handshakeInterceptor;
    this.properties = properties;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    String[] origins = properties.corsAllowedOrigins().split(",");
    registry
        .addHandler(lspJavaHandler, ApiPaths.LSP_JAVA)
        .addInterceptors(handshakeInterceptor)
        .setAllowedOrigins(origins);
  }
}
