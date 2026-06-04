package com.codetraininglab.platform.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import jakarta.servlet.FilterChain;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class JwtAuthenticationFilterTest {

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

  private JwtAuthenticationFilter newFilter(UserRepository userRepository) {
    JwtService jwtService =
        new JwtService(
            CtlPropertiesTestFixtures.defaults(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    return new JwtAuthenticationFilter(jwtService, userRepository);
  }

  private UserRepository activeUserRepository(UUID userId) {
    UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    UserEntity user =
        new UserEntity(
            userId,
            "u@test.com",
            passwordEncoder.encode("TempPass1"),
            UserRole.USER,
            Instant.EPOCH,
            Instant.EPOCH);
    org.mockito.Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    return userRepository;
  }

  @Test
  void setsAuthenticationForValidBearerToken() throws Exception {
    UUID userId = UUID.randomUUID();
    JwtAuthenticationFilter filter = newFilter(activeUserRepository(userId));
    JwtService jwtService =
        new JwtService(
            CtlPropertiesTestFixtures.defaults(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
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
    UUID userId = UUID.randomUUID();
    JwtAuthenticationFilter filter = newFilter(activeUserRepository(userId));
    JwtService jwtService =
        new JwtService(
            CtlPropertiesTestFixtures.defaults(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
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

  @Test
  void clearsAuthenticationForDeactivatedUser() throws Exception {
    UUID userId = UUID.randomUUID();
    UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    org.mockito.Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
    JwtAuthenticationFilter filter = newFilter(userRepository);
    JwtService jwtService =
        new JwtService(
            CtlPropertiesTestFixtures.defaults(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    String token = jwtService.createToken(userId, "u@test.com", UserRole.USER);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, (req, res) -> {});

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    SecurityContextHolder.clearContext();
  }
}
