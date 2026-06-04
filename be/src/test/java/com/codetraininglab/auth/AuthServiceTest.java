package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.AuthResponse;
import com.codetraininglab.identity.api.ChangePasswordRequest;
import com.codetraininglab.identity.api.LoginRequest;
import com.codetraininglab.identity.api.RegisterRequest;
import com.codetraininglab.platform.config.AccessRequestProperties;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  private AuthService authService;
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    passwordEncoder = new BCryptPasswordEncoder(12);
    CtlProperties ctlProperties = CtlPropertiesTestFixtures.defaults();
    AccessRequestProperties accessRequestProperties =
        new AccessRequestProperties(true, "admin@example.com");
    JwtService jwtService =
        new JwtService(
            ctlProperties, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    authService =
        new AuthService(
            userRepository,
            passwordEncoder,
            jwtService,
            accessRequestProperties,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
  }

  @Test
  void registerForbiddenWhenUsersExist() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(1L);

    assertThatThrownBy(() -> authService.register(new RegisterRequest("a@b.com", "Password1")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Registration is disabled");
  }

  @Test
  void bootstrapRegisterCreatesAdmin() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(0L);
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("admin@x.com")).thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    AuthResponse response = authService.register(new RegisterRequest("admin@x.com", "Password1"));

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
    assertThat(info.accessRequestsEnabled()).isFalse();
    assertThat(info.accessRequestsConfigured()).isFalse();
  }

  @Test
  void registrationInfoReportsAccessRequestsWhenEnabledAndUsersExist() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(2L);

    var info = authService.registrationInfo();

    assertThat(info.registrationOpen()).isFalse();
    assertThat(info.bootstrap()).isFalse();
    assertThat(info.accessRequestsEnabled()).isTrue();
    assertThat(info.accessRequestsConfigured()).isTrue();
  }

  @Test
  void registrationInfoShowsAccessRequestsLinkWhenNotifyEmailMissing() {
    CtlProperties ctlProperties = CtlPropertiesTestFixtures.defaults();
    AccessRequestProperties missingNotify = new AccessRequestProperties(true, "");
    AuthService service =
        new AuthService(
            userRepository,
            passwordEncoder,
            new JwtService(ctlProperties, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)),
            missingNotify,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    when(userRepository.countByDeletedAtIsNull()).thenReturn(2L);

    var info = service.registrationInfo();

    assertThat(info.accessRequestsEnabled()).isTrue();
    assertThat(info.accessRequestsConfigured()).isFalse();
  }

  @Test
  void registrationInfoClosedWhenAccessRequestsDisabled() {
    CtlProperties ctlProperties = CtlPropertiesTestFixtures.defaults();
    AccessRequestProperties disabledAccess = new AccessRequestProperties(false, "admin@example.com");
    AuthService closed =
        new AuthService(
            userRepository,
            passwordEncoder,
            new JwtService(ctlProperties, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)),
            disabledAccess,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    when(userRepository.countByDeletedAtIsNull()).thenReturn(1L);

    var info = closed.registrationInfo();

    assertThat(info.registrationOpen()).isFalse();
    assertThat(info.bootstrap()).isFalse();
    assertThat(info.accessRequestsEnabled()).isFalse();
    assertThat(info.accessRequestsConfigured()).isFalse();
  }


  @Test
  void registerRejectsDuplicateEmail() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(0L);
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("a@b.com"))
        .thenReturn(
            Optional.of(
                new UserEntity(
                    UUID.randomUUID(),
                    "a@b.com",
                    passwordEncoder.encode("Password1"),
                    UserRole.ADMIN,
                    Instant.EPOCH,
                    Instant.EPOCH)));

    assertThatThrownBy(() -> authService.register(new RegisterRequest("a@b.com", "Password1")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Email already registered");
  }

  @Test
  void registerRejectsShortPassword() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(0L);

    assertThatThrownBy(() -> authService.register(new RegisterRequest("a@b.com", "short")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("at least 8 characters");
  }

  @Test
  void registerRejectsPasswordWithoutDigit() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(0L);

    assertThatThrownBy(() -> authService.register(new RegisterRequest("a@b.com", "Password")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("digit");
  }

  @Test
  void registerRejectsPasswordEqualToEmail() {
    when(userRepository.countByDeletedAtIsNull()).thenReturn(0L);

    String email = "Test1@x.com";
    assertThatThrownBy(() -> authService.register(new RegisterRequest(email, email)))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("must not match email");
  }

  @Test
  void loginReturnsTokenForValidUser() {
    UserEntity user =
        new UserEntity(
            UUID.randomUUID(),
            "a@b.com",
            passwordEncoder.encode("Password1"),
            UserRole.USER,
            Instant.EPOCH,
            Instant.EPOCH);
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("a@b.com"))
        .thenReturn(Optional.of(user));
    AuthResponse response = authService.login(new LoginRequest("A@B.com", "Password1"));
    assertThat(response.accessToken()).isNotBlank();
    assertThat(response.role()).isEqualTo("USER");
    assertThat(response.mustChangePassword()).isFalse();
  }

  @Test
  void loginSignalsMustChangePassword() {
    UserEntity user =
        new UserEntity(
            UUID.randomUUID(),
            "temp@x.com",
            passwordEncoder.encode("TempPass1"),
            UserRole.USER,
            Instant.EPOCH,
            Instant.EPOCH,
            "Temp User",
            true);
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("temp@x.com"))
        .thenReturn(Optional.of(user));

    AuthResponse response = authService.login(new LoginRequest("temp@x.com", "TempPass1"));

    assertThat(response.mustChangePassword()).isTrue();
  }

  @Test
  void changePasswordClearsMustChangeFlag() {
    UUID userId = UUID.randomUUID();
    UserEntity user =
        new UserEntity(
            userId,
            "temp@x.com",
            passwordEncoder.encode("TempPass1"),
            UserRole.USER,
            Instant.EPOCH,
            Instant.EPOCH,
            "Temp User",
            true);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    AuthResponse response =
        authService.changePassword(
            userId, new ChangePasswordRequest("TempPass1", "NewSecret9"));

    assertThat(response.mustChangePassword()).isFalse();
    assertThat(user.isPasswordMustChange()).isFalse();
    assertThat(passwordEncoder.matches("NewSecret9", user.getPasswordHash())).isTrue();
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
            passwordEncoder.encode("Password1"),
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
