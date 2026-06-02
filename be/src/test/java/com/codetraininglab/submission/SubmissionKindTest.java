package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codetraininglab.domain.SubmissionKind;
import org.junit.jupiter.api.Test;

class SubmissionKindTest {

  @Test
  void fromRequestDefaultsToSubmit() {
    assertThat(SubmissionKind.fromRequest(null)).isEqualTo(SubmissionKind.SUBMIT);
    assertThat(SubmissionKind.fromRequest("")).isEqualTo(SubmissionKind.SUBMIT);
  }

  @Test
  void fromRequestParsesRun() {
    assertThat(SubmissionKind.fromRequest("run")).isEqualTo(SubmissionKind.RUN);
  }

  @Test
  void fromRequestRejectsUnknown() {
    assertThatThrownBy(() -> SubmissionKind.fromRequest("practice"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
