package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.AccessRequestStatus;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.ApproveAccessRequestRequest;
import com.codetraininglab.identity.api.CreateUserResponse;
import com.codetraininglab.identity.api.RejectAccessRequestRequest;
import com.codetraininglab.platform.persistence.AccessRequestEntity;
import com.codetraininglab.platform.persistence.AccessRequestRepository;
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
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AccessRequestAdminServiceTest {

  @Mock private AccessRequestRepository accessRequestRepository;
  @Mock private UserRepository userRepository;
  @Mock private UserAdminService userAdminService;

  private AccessRequestAdminService service;
  private UUID requestId;
  private UUID adminId;

  @BeforeEach
  void setUp() {
    service =
        new AccessRequestAdminService(
            accessRequestRepository,
            userRepository,
            userAdminService,
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    requestId = UUID.randomUUID();
    adminId = UUID.randomUUID();
  }

  @Test
  void rejectMarksRequestRejected() {
    AccessRequestEntity entity =
        new AccessRequestEntity(
            requestId,
            "learner@x.com",
            "Jane Learner",
            "Hello",
            AccessRequestStatus.PENDING,
            Instant.EPOCH,
            Instant.EPOCH);
    when(accessRequestRepository.findById(requestId)).thenReturn(Optional.of(entity));
    when(accessRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var summary =
        service.reject(requestId, adminId, new RejectAccessRequestRequest("Not a fit right now"));

    assertThat(summary.status()).isEqualTo("REJECTED");
    assertThat(summary.reviewNotes()).isEqualTo("Not a fit right now");
    assertThat(entity.getReviewedByUserId()).isEqualTo(adminId);
  }

  @Test
  void approveCreatesUserAndMarksApproved() {
    AccessRequestEntity entity =
        new AccessRequestEntity(
            requestId,
            "learner@x.com",
            "Jane Learner",
            "Hello",
            AccessRequestStatus.PENDING,
            Instant.EPOCH,
            Instant.EPOCH);
    when(accessRequestRepository.findById(requestId)).thenReturn(Optional.of(entity));
    when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("learner@x.com"))
        .thenReturn(Optional.empty());
    when(userAdminService.createUser(any()))
        .thenReturn(
            new CreateUserResponse(
                UUID.randomUUID(), "learner@x.com", "Jane Learner", "TempPass1", "USER", true));
    when(accessRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    CreateUserResponse created =
        service.approve(requestId, adminId, new ApproveAccessRequestRequest("TempPass1", UserRole.USER));

    assertThat(created.email()).isEqualTo("learner@x.com");
    ArgumentCaptor<AccessRequestEntity> saved = ArgumentCaptor.forClass(AccessRequestEntity.class);
    verify(accessRequestRepository).save(saved.capture());
    assertThat(saved.getValue().getStatus()).isEqualTo(AccessRequestStatus.APPROVED);
  }

  @Test
  void approveRejectsNonPendingRequest() {
    AccessRequestEntity entity =
        new AccessRequestEntity(
            requestId,
            "learner@x.com",
            "Jane Learner",
            null,
            AccessRequestStatus.REJECTED,
            Instant.EPOCH,
            Instant.EPOCH);
    when(accessRequestRepository.findById(requestId)).thenReturn(Optional.of(entity));

    assertThatThrownBy(
            () ->
                service.approve(
                    requestId, adminId, new ApproveAccessRequestRequest("TempPass1", UserRole.USER)))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("pending");
  }
}
