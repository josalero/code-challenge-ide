package com.codetraininglab.domain;

public enum UserRole {
  ADMIN,
  USER;

  public String authority() {
    return "ROLE_" + name();
  }
}
