package com.codetraininglab.submission.api;

import com.codetraininglab.submission.application.SubmissionEventCatchUp;
import com.codetraininglab.submission.application.SubmissionEventHub;
import com.codetraininglab.submission.application.SubmissionService;
import com.codetraininglab.submission.messaging.SubmissionEventType;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping(com.codetraininglab.platform.web.ApiPaths.BASE)
public class SubmissionController {

  private final SubmissionService submissionService;
  private final SubmissionEventHub eventHub;

  public SubmissionController(SubmissionService submissionService, SubmissionEventHub eventHub) {
    this.submissionService = submissionService;
    this.eventHub = eventHub;
  }

  @PostMapping("/submissions")
  @ResponseStatus(HttpStatus.CREATED)
  SubmissionResponse create(
      Authentication authentication,
      @Valid @RequestBody CreateSubmissionRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
    return submissionService.create(userId(authentication), request, idempotencyKey);
  }

  @GetMapping("/submissions/{id}")
  SubmissionResponse get(Authentication authentication, @PathVariable UUID id) {
    return submissionService.get(userId(authentication), id);
  }

  @DeleteMapping("/submissions/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void cancel(Authentication authentication, @PathVariable UUID id) {
    submissionService.cancel(userId(authentication), id);
  }

  @GetMapping("/submissions/{id}/events")
  SseEmitter events(Authentication authentication, @PathVariable UUID id) {
    SubmissionResponse submission = submissionService.get(userId(authentication), id);
    SseEmitter emitter = eventHub.subscribe(id);
    eventHub.publish(
        id,
        SubmissionEventType.STATUS.eventName(),
        SubmissionEventCatchUp.statusPayload(submission.status()));
    return emitter;
  }

  @GetMapping("/reports/{id}")
  ReportResponse report(Authentication authentication, @PathVariable UUID id) {
    return submissionService.getReport(userId(authentication), id);
  }

  private static UUID userId(Authentication authentication) {
    return (UUID) authentication.getPrincipal();
  }
}
