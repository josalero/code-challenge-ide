package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codetraininglab.identity.api.AuthResponse;
import com.codetraininglab.identity.api.LoginRequest;
import com.codetraininglab.identity.api.RegisterRequest;
import com.codetraininglab.identity.application.AuthService;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.platform.security.JwtService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import com.codetraininglab.platform.persistence.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    CtlProperties properties =
        new CtlProperties(
            true,
            "test-jwt-secret-must-be-at-least-32-characters-long",
            24,
            "http://localhost:5173",
            "challenges",
            "runner",
            "",
            "lsp",
            5,
            24,
            "openrouter",
            "",
            "model",
            "http://localhost:11434",
            "ollama", false, false);
    JwtService jwtService = new JwtService(properties, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    authService =
        new AuthService(
            userRepository,
            new BCryptPasswordEncoder(),
            jwtService,
            properties,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
  }

  @Test
  void registerCreatesUser() {
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("a@b.com")).thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    AuthResponse response = authService.register(new RegisterRequest("a@b.com", "password1"));
    assertThat(response.accessToken()).isNotBlank();
    assertThat(response.email()).isEqualTo("a@b.com");
  }

  @Test
  void loginReturnsTokenForValidUser() {
    UserEntity user =
        new UserEntity(
            UUID.randomUUID(),
            "a@b.com",
            new BCryptPasswordEncoder().encode("password1"),
            Instant.EPOCH,
            Instant.EPOCH);
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("a@b.com"))
        .thenReturn(Optional.of(user));
    AuthResponse response = authService.login(new LoginRequest("a@b.com", "password1"));
    assertThat(response.accessToken()).isNotBlank();
  }

  @Test
  void registerRejectsPasswordEqualToEmail() {
    assertThatThrownBy(() -> authService.register(new RegisterRequest("same@x.com", "same@x.com")))
        .isInstanceOf(ResponseStatusException.class);
  }
}
