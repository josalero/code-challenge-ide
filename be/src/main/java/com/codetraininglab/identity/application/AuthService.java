package com.codetraininglab.identity.application;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.identity.api.AuthResponse;
import com.codetraininglab.identity.api.LoginRequest;
import com.codetraininglab.identity.api.RegisterRequest;
import com.codetraininglab.identity.api.RegistrationInfoResponse;
import com.codetraininglab.platform.config.CtlProperties;
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
  private final CtlProperties properties;
  private final Clock clock;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      CtlProperties properties,
      Clock clock) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.properties = properties;
    this.clock = clock;
  }

  public RegistrationInfoResponse registrationInfo() {
    boolean bootstrap = userRepository.countByDeletedAtIsNull() == 0;
    boolean registrationOpen = bootstrap || properties.registrationEnabled();
    return new RegistrationInfoResponse(registrationOpen, bootstrap);
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    boolean bootstrap = userRepository.countByDeletedAtIsNull() == 0;
    if (!bootstrap && !properties.registrationEnabled()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Registration is disabled");
    }
    String email = request.email().trim().toLowerCase();
    validatePassword(email, request.password());
    if (userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }
    UserRole role = bootstrap ? UserRole.ADMIN : UserRole.USER;
    Instant now = clock.instant();
    UserEntity user =
        new UserEntity(
            UUID.randomUUID(),
            email,
            passwordEncoder.encode(request.password()),
            role,
            now,
            now);
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

  private AuthResponse tokenFor(UserEntity user) {
    return new AuthResponse(
        jwtService.createToken(user.getId(), user.getEmail(), user.getRole()),
        user.getId(),
        user.getEmail(),
        user.getRole().name());
  }

  private void validatePassword(String email, String password) {
    if (password == null || password.length() < 8) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
    }
    if (password.equalsIgnoreCase(email)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must not match email");
    }
  }
}
