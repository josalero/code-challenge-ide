package com.codetraininglab.identity.application;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.AuthResponse;
import com.codetraininglab.identity.api.ChangePasswordRequest;
import com.codetraininglab.identity.api.LoginRequest;
import com.codetraininglab.identity.api.PasswordRequirementsResponse;
import com.codetraininglab.identity.api.RegisterRequest;
import com.codetraininglab.identity.api.RegistrationInfoResponse;
import com.codetraininglab.platform.config.AccessRequestProperties;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.platform.security.JwtService;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AccessRequestProperties accessRequestProperties;
  private final Clock clock;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AccessRequestProperties accessRequestProperties,
      Clock clock) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.accessRequestProperties = accessRequestProperties;
    this.clock = clock;
  }

  public RegistrationInfoResponse registrationInfo() {
    boolean bootstrap = userRepository.countByDeletedAtIsNull() == 0;
    boolean accessRequestsEnabled = !bootstrap && accessRequestProperties.requestsEnabled();
    boolean accessRequestsConfigured =
        !bootstrap && accessRequestProperties.isAcceptingRequests();
    return new RegistrationInfoResponse(
        bootstrap, bootstrap, accessRequestsEnabled, accessRequestsConfigured);
  }

  public PasswordRequirementsResponse passwordRequirements() {
    return new PasswordRequirementsResponse(PasswordPolicy.REQUIREMENT_DESCRIPTIONS);
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    boolean bootstrap = userRepository.countByDeletedAtIsNull() == 0;
    if (!bootstrap) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Registration is disabled. Contact an administrator.");
    }
    String email = request.email().trim().toLowerCase();
    PasswordPolicy.validateNewPassword(email, request.password());
    if (userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }
    UserRole role = UserRole.ADMIN;
    Instant now = clock.instant();
    UserEntity user =
        new UserEntity(
            UUID.randomUUID(),
            email,
            passwordEncoder.encode(request.password()),
            role,
            now,
            now,
            null,
            false);
    userRepository.save(user);
    return tokenFor(user);
  }

  public AuthResponse login(LoginRequest request) {
    String email = request.email().trim().toLowerCase();
    UserEntity user =
        userRepository
            .findByEmailIgnoreCaseAndDeletedAtIsNull(email)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
    return tokenFor(user);
  }

  @Transactional
  public AuthResponse changePassword(UUID userId, ChangePasswordRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .filter(entity -> entity.getDeletedAt() == null)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
    }
    if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "New password must be different from the current password");
    }
    PasswordPolicy.validateNewPassword(user.getEmail(), request.newPassword());
    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    user.setPasswordMustChange(false);
    user.setUpdatedAt(clock.instant());
    userRepository.save(user);
    return tokenFor(user);
  }

  private AuthResponse tokenFor(UserEntity user) {
    return new AuthResponse(
        jwtService.createToken(user.getId(), user.getEmail(), user.getRole()),
        user.getId(),
        user.getEmail(),
        user.getRole().name(),
        user.isPasswordMustChange());
  }
}
