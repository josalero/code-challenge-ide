package com.codetraininglab.feedback.api;

import com.codetraininglab.domain.FeedbackActionType;
import com.codetraininglab.feedback.application.FeedbackActionService;
import com.codetraininglab.platform.persistence.SubmissionFeedbackActionEntity;
import com.codetraininglab.platform.web.ApiPaths;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class FeedbackActionController {

  private final FeedbackActionService service;

  public FeedbackActionController(FeedbackActionService service) {
    this.service = service;
  }

  @PostMapping(ApiPaths.SUBMISSION_FEEDBACK_ACTIONS)
  public ResponseEntity<FeedbackActionResponse> request(
      Authentication authentication,
      @PathVariable UUID submissionId,
      @RequestBody @Valid FeedbackActionRequest body) {
    FeedbackActionType action;
    try {
      action = FeedbackActionType.fromString(body.action());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    SubmissionFeedbackActionEntity entity =
        service.request((UUID) authentication.getPrincipal(), submissionId, action);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(FeedbackActionResponse.from(entity));
  }

  @GetMapping(ApiPaths.SUBMISSION_FEEDBACK_ACTIONS)
  public List<FeedbackActionResponse> list(
      Authentication authentication, @PathVariable UUID submissionId) {
    return service.list((UUID) authentication.getPrincipal(), submissionId).stream()
        .map(FeedbackActionResponse::from)
        .toList();
  }

  @GetMapping(ApiPaths.FEEDBACK_ACTIONS + "/{actionId}")
  public FeedbackActionResponse find(Authentication authentication, @PathVariable UUID actionId) {
    return FeedbackActionResponse.from(
        service.find((UUID) authentication.getPrincipal(), actionId));
  }
}
