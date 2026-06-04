package com.codetraininglab.identity.api;

import com.codetraininglab.domain.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApproveAccessRequestRequest(
    @NotBlank @Size(min = 8, max = 128) String temporaryPassword, UserRole role) {

  public ApproveAccessRequestRequest(String temporaryPassword) {
    this(temporaryPassword, UserRole.USER);
  }
}
