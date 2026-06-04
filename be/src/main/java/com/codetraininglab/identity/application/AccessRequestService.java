package com.codetraininglab.identity.application;

import com.codetraininglab.domain.AccessRequestStatus;
import com.codetraininglab.identity.api.AccessRequest;
import com.codetraininglab.identity.api.AccessRequestResponse;
import com.codetraininglab.platform.config.AccessRequestProperties;
import com.codetraininglab.platform.mail.CtlMailSendException;
import com.codetraininglab.platform.persistence.AccessRequestEntity;
import com.codetraininglab.platform.persistence.AccessRequestRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccessRequestService {

  private static final Logger log = LoggerFactory.getLogger(AccessRequestService.class);

  private final UserRepository userRepository;
  private final AccessRequestRepository accessRequestRepository;
  private final AccessRequestProperties accessRequestProperties;
  private final AccessRequestEmailSender emailSender;
  private final Clock clock;

  public AccessRequestService(
      UserRepository userRepository,
      AccessRequestRepository accessRequestRepository,
      AccessRequestProperties accessRequestProperties,
      AccessRequestEmailSender emailSender,
      Clock clock) {
    this.userRepository = userRepository;
    this.accessRequestRepository = accessRequestRepository;
    this.accessRequestProperties = accessRequestProperties;
    this.emailSender = emailSender;
    this.clock = clock;
  }

  @Transactional
  public AccessRequestResponse submit(AccessRequest request) {
    if (!accessRequestProperties.requestsEnabled()) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "Access requests are not available. Contact the site administrator.");
    }

    String email = request.email().trim().toLowerCase();
    String fullName = request.fullName().trim();
    if (fullName.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
    }

    if (userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email).isPresent()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "An account with this email already exists. Sign in or use a different email.");
    }

    if (accessRequestRepository.existsByEmailIgnoreCaseAndStatus(email, AccessRequestStatus.PENDING)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "You already have a pending access request. We will email you after it is reviewed.");
    }

    Instant now = clock.instant();
    String message = normalizeMessage(request.message());
    accessRequestRepository.save(
        new AccessRequestEntity(
            UUID.randomUUID(), email, fullName, message, AccessRequestStatus.PENDING, now, now));

    notifyAdminByEmail(email, fullName, message);

    return new AccessRequestResponse(
        "Your request was sent. We will review it and email you if access is approved.");
  }

  private void notifyAdminByEmail(String email, String fullName, String message) {
    if (!accessRequestProperties.isAcceptingRequests()) {
      log.info("Access request stored for {} (email notification not configured)", email);
      return;
    }
    try {
      emailSender.sendAccessRequestEmails(email, fullName, message);
    } catch (IllegalStateException | CtlMailSendException ex) {
      log.warn("Access request stored for {} but notification email failed", email, ex);
    }
  }

  private static String normalizeMessage(String message) {
    if (message == null) {
      return null;
    }
    String trimmed = message.trim();
    return trimmed.isBlank() ? null : trimmed;
  }
}
