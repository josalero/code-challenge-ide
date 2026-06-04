package com.codetraininglab.identity.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class PasswordPolicyTest {

  @Test
  void validateNewPasswordAcceptsStrongPassword() {
    assertThatCode(() -> PasswordPolicy.validateNewPassword("user@x.com", "Secret1!"))
        .doesNotThrowAnyException();
  }

  @Test
  void validateNewPasswordRejectsMissingDigit() {
    assertThatThrownBy(() -> PasswordPolicy.validateNewPassword("user@x.com", "Secretonly"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("digit");
  }

  @Test
  void validateTemporaryPasswordAllowsSimplerRules() {
    assertThatCode(() -> PasswordPolicy.validateTemporaryPassword("user@x.com", "temp-pass"))
        .doesNotThrowAnyException();
  }
}
