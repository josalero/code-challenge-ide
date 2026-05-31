package com.codetraininglab.integration.lsp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.socket.WebSocketHandler;

@ExtendWith(MockitoExtension.class)
class JwtWebSocketHandshakeInterceptorTest {

  @Mock private JwtService jwtService;

  @Test
  void decodesBase64SolutionFromQueryParam() {
    UUID userId = UUID.randomUUID();
    when(jwtService.parseUserId("token")).thenReturn(userId);

    String source = "package com.challenge;\npublic class Solution {}";
    String encoded =
        Base64.getUrlEncoder().encodeToString(source.getBytes(StandardCharsets.UTF_8));
    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    servletRequest.setParameter("access_token", "token");
    servletRequest.setParameter("solution", encoded);
    ServerHttpRequest request = new ServletServerHttpRequest(servletRequest);

    Map<String, Object> attributes = new HashMap<>();
    JwtWebSocketHandshakeInterceptor interceptor =
        new JwtWebSocketHandshakeInterceptor(jwtService);
    boolean accepted =
        interceptor.beforeHandshake(
            request, org.mockito.Mockito.mock(ServerHttpResponse.class),
            org.mockito.Mockito.mock(WebSocketHandler.class),
            attributes);

    assertThat(accepted).isTrue();
    assertThat(attributes.get("solutionCode")).isEqualTo(source);
  }
}
