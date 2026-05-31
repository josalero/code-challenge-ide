package com.codetraininglab.platform.web;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ProblemDetailsAdvice {

  @ExceptionHandler(ResponseStatusException.class)
  ProblemDetail handleResponseStatus(ResponseStatusException ex) {
    ProblemDetail detail = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
    detail.setTitle(ex.getStatusCode().toString());
    detail.setType(URI.create("about:blank"));
    return detail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("Validation failed");
    detail.setDetail(
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Invalid request"));
    return detail;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ProblemDetail handleConstraint(ConstraintViolationException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("Validation failed");
    detail.setDetail(ex.getMessage());
    return detail;
  }

  @ExceptionHandler(AccessDeniedException.class)
  ProblemDetail handleAccessDenied() {
    return ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
  }
}
