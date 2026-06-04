package com.codetraininglab.identity.application;

import com.codetraininglab.domain.AccessRequestStatus;
import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.AccessRequestSummary;
import com.codetraininglab.identity.api.ApproveAccessRequestRequest;
import com.codetraininglab.identity.api.CreateUserRequest;
import com.codetraininglab.identity.api.CreateUserResponse;
import com.codetraininglab.identity.api.RejectAccessRequestRequest;
import com.codetraininglab.platform.persistence.AccessRequestEntity;
import com.codetraininglab.platform.persistence.AccessRequestRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccessRequestAdminService {

  private final AccessRequestRepository accessRequestRepository;
  private final UserRepository userRepository;
  private final UserAdminService userAdminService;
  private final Clock clock;

  public AccessRequestAdminService(
      AccessRequestRepository accessRequestRepository,
      UserRepository userRepository,
      UserAdminService userAdminService,
      Clock clock) {
    this.accessRequestRepository = accessRequestRepository;
    this.userRepository = userRepository;
    this.userAdminService = userAdminService;
    this.clock = clock;
  }

  public List<AccessRequestSummary> list(String statusFilter) {
    AccessRequestStatus status = parseStatusFilter(statusFilter);
    List<AccessRequestEntity> rows =
        status == null
            ? accessRequestRepository.findAllByOrderByCreatedAtDesc()
            : accessRequestRepository.findByStatusOrderByCreatedAtDesc(status);
    return rows.stream().map(this::toSummary).toList();
  }

  public long countPending() {
    return accessRequestRepository.countByStatus(AccessRequestStatus.PENDING);
  }

  @Transactional
  public AccessRequestSummary reject(UUID id, UUID adminUserId, RejectAccessRequestRequest request) {
    AccessRequestEntity entity = requirePending(id);
    Instant now = clock.instant();
    entity.setStatus(AccessRequestStatus.REJECTED);
    entity.setReviewNotes(normalizeNotes(request.reviewNotes()));
    entity.setReviewedByUserId(adminUserId);
    entity.setReviewedAt(now);
    entity.setUpdatedAt(now);
    accessRequestRepository.save(entity);
    return toSummary(entity);
  }

  @Transactional
  public CreateUserResponse approve(
      UUID id, UUID adminUserId, ApproveAccessRequestRequest request) {
    AccessRequestEntity entity = requirePending(id);
    if (userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(entity.getEmail()).isPresent()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "A user account already exists for this email");
    }
    UserRole role = request.role() == null ? UserRole.USER : request.role();
    CreateUserResponse created =
        userAdminService.createUser(
            new CreateUserRequest(
                entity.getEmail(),
                entity.getFullName(),
                request.temporaryPassword(),
                role));
    Instant now = clock.instant();
    entity.setStatus(AccessRequestStatus.APPROVED);
    entity.setReviewedByUserId(adminUserId);
    entity.setReviewedAt(now);
    entity.setUpdatedAt(now);
    accessRequestRepository.save(entity);
    return created;
  }

  private AccessRequestEntity requirePending(UUID id) {
    AccessRequestEntity entity =
        accessRequestRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Access request not found"));
    if (entity.getStatus() != AccessRequestStatus.PENDING) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Only pending access requests can be reviewed");
    }
    return entity;
  }

  private static AccessRequestStatus parseStatusFilter(String statusFilter) {
    if (statusFilter == null || statusFilter.isBlank()) {
      return null;
    }
    try {
      return AccessRequestStatus.valueOf(statusFilter.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status filter");
    }
  }

  private static String normalizeNotes(String reviewNotes) {
    if (reviewNotes == null) {
      return null;
    }
    String trimmed = reviewNotes.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  private AccessRequestSummary toSummary(AccessRequestEntity entity) {
    return new AccessRequestSummary(
        entity.getId(),
        entity.getEmail(),
        entity.getFullName(),
        entity.getMessage(),
        entity.getStatus().name(),
        entity.getCreatedAt(),
        entity.getReviewedAt(),
        entity.getReviewNotes());
  }
}
