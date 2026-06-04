package com.codetraininglab.identity.application;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Shared password rules for registration, admin provisioning, and password changes. */
public final class PasswordPolicy {

  public static final List<String> REQUIREMENT_DESCRIPTIONS =
      List.of(
          "At least 8 characters long",
          "At least one uppercase letter (A–Z)",
          "At least one lowercase letter (a–z)",
          "At least one digit (0–9)",
          "Must not be the same as your email address");

  private PasswordPolicy() {}

  public static void validateNewPassword(String email, String password) {
    if (password == null || password.length() < 8) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
    }
    if (!containsUppercase(password)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Password must include at least one uppercase letter");
    }
    if (!containsLowercase(password)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Password must include at least one lowercase letter");
    }
    if (!containsDigit(password)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Password must include at least one digit");
    }
    if (email != null && password.equalsIgnoreCase(email.trim())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must not match email");
    }
  }

  /** Temporary passwords from admin may be simpler; still minimum length and not equal to email. */
  public static void validateTemporaryPassword(String email, String password) {
    if (password == null || password.length() < 8) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Temporary password must be at least 8 characters");
    }
    if (email != null && password.equalsIgnoreCase(email.trim())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Temporary password must not match email");
    }
  }

  private static boolean containsUppercase(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (Character.isUpperCase(value.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  private static boolean containsLowercase(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (Character.isLowerCase(value.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  private static boolean containsDigit(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (Character.isDigit(value.charAt(i))) {
        return true;
      }
    }
    return false;
  }
}
