package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.AccessRequestStatus;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.AccessRequest;
import com.codetraininglab.platform.config.AccessRequestProperties;
import com.codetraininglab.platform.persistence.AccessRequestEntity;
import com.codetraininglab.platform.persistence.AccessRequestRepository;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserRepository;
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
class AccessRequestServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private AccessRequestRepository accessRequestRepository;
  @Mock private AccessRequestEmailSender emailSender;

  private AccessRequestService accessRequestService;

  @BeforeEach
  void setUp() {
    accessRequestService =
        new AccessRequestService(
            userRepository,
            accessRequestRepository,
            new AccessRequestProperties(true, "admin@itjobopportunities.io"),
            emailSender,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
  }

  @Test
  void submitPersistsRequestAndSendsEmail() {
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("learner@x.com"))
        .thenReturn(Optional.empty());
    when(accessRequestRepository.existsByEmailIgnoreCaseAndStatus(
            "learner@x.com", AccessRequestStatus.PENDING))
        .thenReturn(false);
    when(accessRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var response =
        accessRequestService.submit(
            new AccessRequest("learner@x.com", "Jane Learner", "I want to try the lab."));

    assertThat(response.message()).contains("Your request was sent");
    ArgumentCaptor<AccessRequestEntity> saved = ArgumentCaptor.forClass(AccessRequestEntity.class);
    verify(accessRequestRepository).save(saved.capture());
    assertThat(saved.getValue().getEmail()).isEqualTo("learner@x.com");
    assertThat(saved.getValue().getStatus()).isEqualTo(AccessRequestStatus.PENDING);
    verify(emailSender)
        .sendAccessRequestEmails(
            "learner@x.com", "Jane Learner", "I want to try the lab.");
  }

  @Test
  void submitRejectsExistingAccountEmail() {
    String passwordHash = new BCryptPasswordEncoder(12).encode("Password1");
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("existing@x.com"))
        .thenReturn(
            Optional.of(
                new UserEntity(
                    UUID.randomUUID(),
                    "existing@x.com",
                    passwordHash,
                    UserRole.USER,
                    Instant.EPOCH,
                    Instant.EPOCH)));

    assertThatThrownBy(
            () ->
                accessRequestService.submit(
                    new AccessRequest("existing@x.com", "Existing User", null)))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("already exists");
  }

  @Test
  void submitRejectsDuplicatePendingRequest() {
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("new@x.com"))
        .thenReturn(Optional.empty());
    when(accessRequestRepository.existsByEmailIgnoreCaseAndStatus(
            "new@x.com", AccessRequestStatus.PENDING))
        .thenReturn(true);

    assertThatThrownBy(
            () -> accessRequestService.submit(new AccessRequest("new@x.com", "New User", "Hello")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("pending access request");
  }

  @Test
  void submitUnavailableWhenAccessRequestsDisabled() {
    AccessRequestService closed =
        new AccessRequestService(
            userRepository,
            accessRequestRepository,
            new AccessRequestProperties(false, "admin@itjobopportunities.io"),
            emailSender,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

    assertThatThrownBy(
            () -> closed.submit(new AccessRequest("new@x.com", "New User", "Hello")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("not available");
  }

  @Test
  void submitStoresRequestWhenEmailNotConfigured() {
    AccessRequestService withoutEmail =
        new AccessRequestService(
            userRepository,
            accessRequestRepository,
            new AccessRequestProperties(true, ""),
            emailSender,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("new@x.com"))
        .thenReturn(Optional.empty());
    when(accessRequestRepository.existsByEmailIgnoreCaseAndStatus(
            "new@x.com", AccessRequestStatus.PENDING))
        .thenReturn(false);
    when(accessRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var response =
        withoutEmail.submit(new AccessRequest("new@x.com", "New User", "Hello"));

    assertThat(response.message()).contains("Your request was sent");
    verify(accessRequestRepository).save(any());
  }
}
