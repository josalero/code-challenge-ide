package com.codetraininglab.feedback.application;

import com.codetraininglab.domain.FeedbackActionType;
import com.codetraininglab.platform.persistence.SubmissionEntity;

/**
 * SPI for on-demand feedback analyzers. Each handler executes the analysis for one {@link
 * FeedbackActionType} and returns a free-form text result.
 */
public interface FeedbackActionHandler {

  FeedbackActionType type();

  /**
   * Run the analyzer and return its result text. Implementations may throw to mark the action as
   * {@code FAILED} — the orchestrator captures the message.
   */
  String execute(SubmissionEntity submission);
}
