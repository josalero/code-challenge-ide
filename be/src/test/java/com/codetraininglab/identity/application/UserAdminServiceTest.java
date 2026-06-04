package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.CreateUserRequest;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserWelcomeEmailSender welcomeEmailSender;

  private UserAdminService userAdminService;
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    passwordEncoder = new BCryptPasswordEncoder(12);
    userAdminService =
        new UserAdminService(
            userRepository,
            passwordEncoder,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
            welcomeEmailSender);
  }

  @Test
  void createUserSetsMustChangeFlag() {
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("learner@x.com"))
        .thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var response =
        userAdminService.createUser(
            new CreateUserRequest("learner@x.com", "Ada Lovelace", "TempPass1", UserRole.USER));

    assertThat(response.email()).isEqualTo("learner@x.com");
    assertThat(response.fullName()).isEqualTo("Ada Lovelace");
    assertThat(response.temporaryPassword()).isEqualTo("TempPass1");
    assertThat(response.role()).isEqualTo("USER");
    ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(saved.capture());
    assertThat(saved.getValue().isPasswordMustChange()).isTrue();
    assertThat(saved.getValue().getRole()).isEqualTo(UserRole.USER);
    assertThat(saved.getValue().getPasswordHash()).startsWith("$2");
    assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("TempPass1");
    assertThat(passwordEncoder.matches("TempPass1", saved.getValue().getPasswordHash())).isTrue();
    verify(welcomeEmailSender)
        .sendWelcomeEmail("learner@x.com", "Ada Lovelace", "TempPass1", "USER");
  }

  @Test
  void createUserCanAssignAdminRole() {
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("admin@x.com"))
        .thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var response =
        userAdminService.createUser(
            new CreateUserRequest("admin@x.com", "Grace Hopper", "TempPass1", UserRole.ADMIN));

    assertThat(response.role()).isEqualTo("ADMIN");
    ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(saved.capture());
    assertThat(saved.getValue().getRole()).isEqualTo(UserRole.ADMIN);
  }
}
