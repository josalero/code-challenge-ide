package com.codetraininglab.platform.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

class ProblemDetailsAdviceTest {

  private final ProblemDetailsAdvice advice = new ProblemDetailsAdvice();

  @Test
  void mapsResponseStatusException() {
    var detail =
        advice.handleResponseStatus(new ResponseStatusException(HttpStatus.BAD_REQUEST, "nope"));
    assertThat(detail.getStatus()).isEqualTo(400);
    assertThat(detail.getDetail()).isEqualTo("nope");
  }

  @Test
  void mapsMethodArgumentNotValidException() {
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.getFieldErrors())
        .thenReturn(java.util.List.of(new FieldError("req", "slug", "must not be blank")));
    var ex = new MethodArgumentNotValidException(null, bindingResult);

    var detail = advice.handleValidation(ex);

    assertThat(detail.getStatus()).isEqualTo(400);
    assertThat(detail.getTitle()).isEqualTo("Validation failed");
    assertThat(detail.getDetail()).contains("slug");
  }

  @Test
  void mapsConstraintViolationException() {
    @SuppressWarnings("unchecked")
    ConstraintViolation<String> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("invalid slug");

    var detail = advice.handleConstraint(new ConstraintViolationException(Set.of(violation)));

    assertThat(detail.getStatus()).isEqualTo(400);
    assertThat(detail.getDetail()).contains("invalid slug");
  }

  @Test
  void mapsAccessDenied() {
    var detail = advice.handleAccessDenied();
    assertThat(detail.getStatus()).isEqualTo(403);
  }
}
