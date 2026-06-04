package com.codetraininglab.identity.application;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.CreateUserRequest;
import com.codetraininglab.identity.api.CreateUserResponse;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserAdminService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;
  private final UserWelcomeEmailSender welcomeEmailSender;

  public UserAdminService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      Clock clock,
      UserWelcomeEmailSender welcomeEmailSender) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
    this.welcomeEmailSender = welcomeEmailSender;
  }

  @Transactional
  public CreateUserResponse createUser(CreateUserRequest request) {
    String email = request.email().trim().toLowerCase();
    String fullName = request.fullName().trim();
    if (fullName.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
    }
    PasswordPolicy.validateTemporaryPassword(email, request.temporaryPassword());
    if (userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }
    Instant now = clock.instant();
    UserEntity user =
        new UserEntity(
            UUID.randomUUID(),
            email,
            passwordEncoder.encode(request.temporaryPassword()),
            request.role(),
            now,
            now,
            fullName,
            true);
    userRepository.save(user);
    CreateUserResponse response =
        new CreateUserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            request.temporaryPassword(),
            user.getRole().name());
    welcomeEmailSender.sendWelcomeEmail(
        response.email(), response.fullName(), response.temporaryPassword(), response.role());
    return response;
  }
}
