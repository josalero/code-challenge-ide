package com.codetraininglab.coach.api;

import com.codetraininglab.coach.application.AiCoachService;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(com.codetraininglab.platform.web.ApiPaths.BASE)
public class AiCoachController {

  private final AiCoachService aiCoachService;

  public AiCoachController(AiCoachService aiCoachService) {
    this.aiCoachService = aiCoachService;
  }

  @PostMapping("/feedback/{itemId}/explain")
  AiCoachService.ExplainResponse explain(Authentication authentication, @PathVariable UUID itemId) {
    return aiCoachService.explain((UUID) authentication.getPrincipal(), itemId);
  }

  @PostMapping("/challenges/{slug}/alternatives")
  AiCoachService.AlternativesResponse alternatives(
      Authentication authentication, @PathVariable String slug) {
    return aiCoachService.alternatives((UUID) authentication.getPrincipal(), slug);
  }
}
