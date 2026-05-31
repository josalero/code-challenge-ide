package com.codetraininglab.platform.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
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
}
