package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.catalog.application.ChallengeQuotaService;
import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.CreateUserRequest;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserWelcomeEmailSender welcomeEmailSender;
  @Mock private SubmissionRepository submissionRepository;
  @Mock private UserProgressRepository progressRepository;
  @Mock private ChallengeQuotaService challengeQuotaService;

  private UserAdminService userAdminService;
  private PasswordEncoder passwordEncoder;
  private Clock clock;

  @BeforeEach
  void setUp() {
    passwordEncoder = new BCryptPasswordEncoder(12);
    clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
    userAdminService =
        new UserAdminService(
            userRepository,
            passwordEncoder,
            clock,
            welcomeEmailSender,
            submissionRepository,
            progressRepository,
            challengeQuotaService);
  }

  @Test
  void createUserSetsMustChangeFlag() {
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("learner@x.com"))
        .thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(welcomeEmailSender.sendWelcomeEmail(
            eq("learner@x.com"), eq("Ada Lovelace"), eq("TempPass1"), eq("USER")))
        .thenReturn(true);

    var response =
        userAdminService.createUser(
            new CreateUserRequest("learner@x.com", "Ada Lovelace", "TempPass1", UserRole.USER));

    assertThat(response.email()).isEqualTo("learner@x.com");
    assertThat(response.fullName()).isEqualTo("Ada Lovelace");
    assertThat(response.temporaryPassword()).isEqualTo("TempPass1");
    assertThat(response.role()).isEqualTo("USER");
    assertThat(response.welcomeEmailSent()).isTrue();
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
    when(welcomeEmailSender.sendWelcomeEmail(any(), any(), any(), any())).thenReturn(false);

    var response =
        userAdminService.createUser(
            new CreateUserRequest("admin@x.com", "Grace Hopper", "TempPass1", UserRole.ADMIN));

    assertThat(response.role()).isEqualTo("ADMIN");
    assertThat(response.welcomeEmailSent()).isFalse();
    ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(saved.capture());
    assertThat(saved.getValue().getRole()).isEqualTo(UserRole.ADMIN);
  }

  @Test
  void createUserPropagatesWelcomeEmailFailure() {
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("learner@x.com"))
        .thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(welcomeEmailSender.sendWelcomeEmail(any(), any(), any(), any()))
        .thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Welcome email could not be sent"));

    assertThatThrownBy(
            () ->
                userAdminService.createUser(
                    new CreateUserRequest(
                        "learner@x.com", "Ada Lovelace", "TempPass1", UserRole.USER)))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.BAD_GATEWAY);
  }

  @Test
  void listUsersReturnsMetricsSortedByLastActivity() {
    UUID activeId = UUID.randomUUID();
    UserEntity active =
        new UserEntity(
            activeId,
            "active@x.com",
            passwordEncoder.encode("TempPass1"),
            UserRole.USER,
            Instant.EPOCH,
            Instant.EPOCH,
            "Active User",
            false);
    when(userRepository.findAllByDeletedAtIsNullOrderByEmailAsc()).thenReturn(List.of(active));
    when(submissionRepository.aggregateByUserIds(List.of(activeId))).thenReturn(List.of());
    when(submissionRepository.countStartedChallengesByUserIds(List.of(activeId))).thenReturn(List.of());
    when(progressRepository.countPassedByUserIds(List.of(activeId), ProgressState.PASSED))
        .thenReturn(List.of());
    when(challengeQuotaService.platformDefaultMax()).thenReturn(5);
    when(challengeQuotaService.effectiveChallengeLimit(org.mockito.ArgumentMatchers.any()))
        .thenReturn(5);

    var summaries = userAdminService.listUsers(false);

    assertThat(summaries).hasSize(1);
    assertThat(summaries.getFirst().email()).isEqualTo("active@x.com");
    assertThat(summaries.getFirst().active()).isTrue();
    assertThat(summaries.getFirst().challengesStarted()).isZero();
    assertThat(summaries.getFirst().completionPercent()).isZero();
  }

  @Test
  void deactivateUserSoftDeletesAccount() {
    UUID actorId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();
    UserEntity target =
        new UserEntity(
            targetId,
            "learner@x.com",
            passwordEncoder.encode("TempPass1"),
            UserRole.USER,
            Instant.EPOCH,
            Instant.EPOCH,
            "Learner",
            false);
    when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    userAdminService.deactivateUser(actorId, targetId);

    ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(saved.capture());
    assertThat(saved.getValue().getDeletedAt()).isEqualTo(Instant.EPOCH);
  }

  @Test
  void deactivateUserRejectsSelfDeactivation() {
    UUID userId = UUID.randomUUID();

    assertThatThrownBy(() -> userAdminService.deactivateUser(userId, userId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void deactivateUserRejectsLastAdmin() {
    UUID actorId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    UserEntity admin =
        new UserEntity(
            adminId,
            "admin@x.com",
            passwordEncoder.encode("TempPass1"),
            UserRole.ADMIN,
            Instant.EPOCH,
            Instant.EPOCH,
            "Admin",
            false);
    when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
    when(userRepository.countByDeletedAtIsNullAndRoleAndIdNot(UserRole.ADMIN, adminId))
        .thenReturn(0L);

    assertThatThrownBy(() -> userAdminService.deactivateUser(actorId, adminId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.CONFLICT);
  }
}
