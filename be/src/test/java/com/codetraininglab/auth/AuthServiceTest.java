package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.AuthResponse;
import com.codetraininglab.identity.api.LoginRequest;
import com.codetraininglab.identity.api.RegisterRequest;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.platform.security.JwtService;
import com.codetraininglab.testsupport.CtlPropertiesTestFixtures;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  private AuthService authService;
  private BCryptPasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    JwtService jwtService =
        new JwtService(
            CtlPropertiesTestFixtures.defaults(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    authService =
        new AuthService(
            userRepository,
            passwordEncoder,
            jwtService,
            CtlPropertiesTestFixtures.defaults(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
  }

  @Test
  void registerCreatesUser() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(1L);
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("a@b.com")).thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    AuthResponse response = authService.register(new RegisterRequest("A@B.com", "password1"));
    assertThat(response.accessToken()).isNotBlank();
    assertThat(response.email()).isEqualTo("a@b.com");
    assertThat(response.role()).isEqualTo("USER");
  }

  @Test
  void bootstrapRegisterCreatesAdmin() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(0L);
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("admin@x.com")).thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    AuthResponse response = authService.register(new RegisterRequest("admin@x.com", "password1"));

    assertThat(response.role()).isEqualTo("ADMIN");
    ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(saved.capture());
    assertThat(saved.getValue().getRole()).isEqualTo(UserRole.ADMIN);
  }

  @Test
  void registrationInfoReportsBootstrapMode() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(0L);

    var info = authService.registrationInfo();

    assertThat(info.registrationOpen()).isTrue();
    assertThat(info.bootstrap()).isTrue();
  }

  @Test
  void registrationInfoHonorsRegistrationFlagWhenUsersExist() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(2L);

    var info = authService.registrationInfo();

    assertThat(info.registrationOpen()).isTrue();
    assertThat(info.bootstrap()).isFalse();
  }

  @Test
  void registrationInfoClosedWhenDisabledAndNotBootstrap() {
    CtlProperties closedRegistration =
        new CtlProperties(
            false,
            CtlPropertiesTestFixtures.defaults().jwtSecret(),
            24,
            "http://localhost:5173",
            "challenges",
            "runner",
            "",
            true,
            60,
            CtlPropertiesTestFixtures.TEST_LSP_IMAGES,
            5,
            24,
            "openrouter",
            "",
            "model",
            "http://localhost:11434",
            "ollama",
            false,
            false,
            false);
    AuthService closed =
        new AuthService(
            userRepository,
            passwordEncoder,
            new JwtService(closedRegistration, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)),
            closedRegistration,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    when(userRepository.countByDeletedAtIsNull()).thenReturn(1L);

    var info = closed.registrationInfo();

    assertThat(info.registrationOpen()).isFalse();
    assertThat(info.bootstrap()).isFalse();
  }

  @Test
  void registerForbiddenWhenRegistrationDisabled() {
    CtlProperties closedRegistration =
        new CtlProperties(
            false,
            CtlPropertiesTestFixtures.defaults().jwtSecret(),
            24,
            "http://localhost:5173",
            "challenges",
            "runner",
            "",
            true,
            60,
            CtlPropertiesTestFixtures.TEST_LSP_IMAGES,
            5,
            24,
            "openrouter",
            "",
            "model",
            "http://localhost:11434",
            "ollama",
            false,
            false,
            false);
    AuthService closed =
        new AuthService(
            userRepository,
            passwordEncoder,
            new JwtService(closedRegistration, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)),
            closedRegistration,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    when(userRepository.countByDeletedAtIsNull()).thenReturn(1L);

    assertThatThrownBy(() -> closed.register(new RegisterRequest("a@b.com", "password1")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Registration is disabled");
  }

  @Test
  void registerRejectsDuplicateEmail() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(1L);
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("a@b.com"))
        .thenReturn(
            Optional.of(
                new UserEntity(
                    UUID.randomUUID(),
                    "a@b.com",
                    "hash",
                    UserRole.USER,
                    Instant.EPOCH,
                    Instant.EPOCH)));

    assertThatThrownBy(() -> authService.register(new RegisterRequest("a@b.com", "password1")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Email already registered");
  }

  @Test
  void registerRejectsShortPassword() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(1L);

    assertThatThrownBy(() -> authService.register(new RegisterRequest("a@b.com", "short")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("at least 8 characters");
  }

  @Test
  void registerRejectsPasswordEqualToEmail() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(1L);

    assertThatThrownBy(() -> authService.register(new RegisterRequest("same@x.com", "same@x.com")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("must not match email");
  }

  @Test
  void loginReturnsTokenForValidUser() {
    UserEntity user =
        new UserEntity(
            UUID.randomUUID(),
            "a@b.com",
            passwordEncoder.encode("password1"),
            UserRole.USER,
            Instant.EPOCH,
            Instant.EPOCH);
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("a@b.com"))
        .thenReturn(Optional.of(user));
    AuthResponse response = authService.login(new LoginRequest("A@B.com", "password1"));
    assertThat(response.accessToken()).isNotBlank();
    assertThat(response.role()).isEqualTo("USER");
  }

  @Test
  void loginRejectsUnknownUser() {
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("missing@x.com"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(new LoginRequest("missing@x.com", "password1")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Invalid credentials");
  }

  @Test
  void loginRejectsWrongPassword() {
    UserEntity user =
        new UserEntity(
            UUID.randomUUID(),
            "a@b.com",
            passwordEncoder.encode("password1"),
            UserRole.USER,
            Instant.EPOCH,
            Instant.EPOCH);
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("a@b.com"))
        .thenReturn(Optional.of(user));

    assertThatThrownBy(() -> authService.login(new LoginRequest("a@b.com", "wrong-pass")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Invalid credentials");
  }
}
