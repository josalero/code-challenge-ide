package com.codetraininglab.integration.lsp;

import com.codetraininglab.platform.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class JwtWebSocketHandshakeInterceptor implements HandshakeInterceptor {

  private final JwtService jwtService;

  public JwtWebSocketHandshakeInterceptor(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes) {
    if (!(request instanceof ServletServerHttpRequest servletRequest)) {
      return false;
    }
    String token = servletRequest.getServletRequest().getParameter("access_token");
    if (token == null || token.isBlank()) {
      return false;
    }
    try {
      UUID userId = jwtService.parseUserId(token);
      attributes.put("userId", userId);
      String solution = servletRequest.getServletRequest().getParameter("solution");
      if (solution != null && !solution.isBlank()) {
        attributes.put("solutionCode", decodeSolution(solution));
      }
      return true;
    } catch (RuntimeException e) {
      return false;
    }
  }

  private static String decodeSolution(String encoded) {
    try {
      return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      return encoded;
    }
  }

  @Override
  public void afterHandshake(
      ServerHttpRequest request,
      ServerHttpResponse response,
      WebSocketHandler wsHandler,
      Exception exception) {}
}
