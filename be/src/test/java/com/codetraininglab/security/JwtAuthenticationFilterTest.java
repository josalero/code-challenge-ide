package com.codetraininglab.platform.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import jakarta.servlet.FilterChain;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

  @Test
  void setsAuthenticationForValidBearerToken() throws Exception {
    JwtService jwtService =
        new JwtService(
            CtlPropertiesTestFixtures.defaults(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
    UUID userId = UUID.randomUUID();
    String token = jwtService.createToken(userId, "u@test.com", UserRole.USER);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = (req, res) -> {};
    filter.doFilter(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userId);
    SecurityContextHolder.clearContext();
  }

  @Test
  void setsAuthenticationForAccessTokenQueryOnSubmissionEvents() throws Exception {
    JwtService jwtService =
        new JwtService(
            CtlPropertiesTestFixtures.defaults(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
    UUID userId = UUID.randomUUID();
    String token = jwtService.createToken(userId, "u@test.com", UserRole.USER);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("GET");
    request.setRequestURI("/api/v1/submissions/" + UUID.randomUUID() + "/events");
    request.setParameter("access_token", token);
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, (req, res) -> {});

    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userId);
    SecurityContextHolder.clearContext();
  }
}
