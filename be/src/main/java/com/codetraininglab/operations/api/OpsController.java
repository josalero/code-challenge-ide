package com.codetraininglab.operations.api;

import com.codetraininglab.operations.application.DeadLetterQueueService;
import com.codetraininglab.platform.web.ApiPaths;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpsController {

  private final DeadLetterQueueService deadLetterQueueService;

  public OpsController(DeadLetterQueueService deadLetterQueueService) {
    this.deadLetterQueueService = deadLetterQueueService;
  }

  @GetMapping(ApiPaths.OPS_DEAD_LETTER_SUBMISSIONS)
  public List<DeadLetterSubmissionResponse> deadLetterSubmissions(
      @RequestParam(defaultValue = "20") int limit) {
    return deadLetterQueueService.peek(limit);
  }

  @PostMapping(ApiPaths.OPS_DEAD_LETTER_REPLAY)
  public DeadLetterReplayResponse replayDeadLetterSubmissions(
      @RequestParam(defaultValue = "10") int limit) {
    return deadLetterQueueService.replay(limit);
  }
}
