package com.codetraininglab.feedback.application;

import com.codetraininglab.domain.FeedbackActionType;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import org.springframework.stereotype.Component;

/**
 * Placeholder for a future SonarQube scanner integration. Kept as a registered handler so the
 * endpoint and UI can list the action; reports a clear "not yet available" message until the
 * scanner image and Sonar server are wired up.
 */
@Component
public class SonarFeedbackActionHandler implements FeedbackActionHandler {

  @Override
  public FeedbackActionType type() {
    return FeedbackActionType.SONAR;
  }

  @Override
  public String execute(SubmissionEntity submission) {
    throw new UnsupportedOperationException(
        "Sonar analysis is not yet available — coming with the analyzer runner image.");
  }
}
