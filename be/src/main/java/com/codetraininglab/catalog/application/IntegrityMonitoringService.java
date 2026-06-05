package com.codetraininglab.catalog.application;

import com.codetraininglab.domain.UserRole;
import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IntegrityMonitoringService {

  private final UserRepository userRepository;

  public IntegrityMonitoringService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public boolean isMonitoringEnabled(UUID userId) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    return isMonitoringEnabled(user);
  }

  public boolean isMonitoringEnabled(UserEntity user) {
    if (user.getRole() == UserRole.ADMIN) {
      return false;
    }
    return !user.isIntegrityMonitoringDisabled();
  }
}
