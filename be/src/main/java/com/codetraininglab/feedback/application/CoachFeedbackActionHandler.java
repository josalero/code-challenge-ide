package com.codetraininglab.feedback.application;

import com.codetraininglab.coach.application.AiCoachService;
import com.codetraininglab.domain.FeedbackActionType;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import org.springframework.stereotype.Component;

@Component
public class CoachFeedbackActionHandler implements FeedbackActionHandler {

  private final AiCoachService aiCoachService;

  public CoachFeedbackActionHandler(AiCoachService aiCoachService) {
    this.aiCoachService = aiCoachService;
  }

  @Override
  public FeedbackActionType type() {
    return FeedbackActionType.COACH;
  }

  @Override
  public String execute(SubmissionEntity submission) {
    return aiCoachService.reviewSubmission(submission.getId());
  }
}
