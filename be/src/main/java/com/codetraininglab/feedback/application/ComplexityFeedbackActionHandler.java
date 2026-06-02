package com.codetraininglab.feedback.application;

import com.codetraininglab.domain.FeedbackActionType;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import org.springframework.stereotype.Component;

/** Placeholder for per-language complexity reports (cyclomatic / cognitive). */
@Component
public class ComplexityFeedbackActionHandler implements FeedbackActionHandler {

  @Override
  public FeedbackActionType type() {
    return FeedbackActionType.COMPLEXITY;
  }

  @Override
  public String execute(SubmissionEntity submission) {
    throw new UnsupportedOperationException(
        "Complexity analysis is not yet available — wired once language-specific analyzers ship.");
  }
}
