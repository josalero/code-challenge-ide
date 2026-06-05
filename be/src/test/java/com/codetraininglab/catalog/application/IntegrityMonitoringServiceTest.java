package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class IntegrityMonitoringServiceTest {

  @Mock private UserRepository userRepository;

  private IntegrityMonitoringService service;
  private UUID userId;
  private String passwordHash;
  private Instant now;

  @BeforeEach
  void setUp() {
    service = new IntegrityMonitoringService(userRepository);
    userId = UUID.randomUUID();
    passwordHash = new BCryptPasswordEncoder(12).encode("Password1");
    now = Instant.parse("2026-06-04T12:00:00Z");
  }

  @Test
  void monitoringDisabledForAdmins() {
    UserEntity admin = user(userId, UserRole.ADMIN);

    assertThat(service.isMonitoringEnabled(admin)).isFalse();
  }

  @Test
  void monitoringEnabledForLearnerByDefault() {
    UserEntity learner = user(userId, UserRole.USER);

    assertThat(service.isMonitoringEnabled(learner)).isTrue();
  }

  @Test
  void monitoringDisabledWhenFlagSet() {
    UserEntity learner = user(userId, UserRole.USER);
    learner.setIntegrityMonitoringDisabled(true);

    assertThat(service.isMonitoringEnabled(learner)).isFalse();
  }

  @Test
  void monitoringLookupThrowsWhenUserMissing() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.isMonitoringEnabled(userId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  private UserEntity user(UUID id, UserRole role) {
    return new UserEntity(id, "user@example.com", passwordHash, role, now, now);
  }
}
