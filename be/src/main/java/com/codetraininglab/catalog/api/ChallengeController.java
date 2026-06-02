package com.codetraininglab.catalog.api;

import com.codetraininglab.catalog.application.ChallengeService;
import com.codetraininglab.catalog.application.ChallengeService.ChallengeDetail;
import com.codetraininglab.catalog.application.ChallengeService.ChallengeSummary;
import com.codetraininglab.catalog.application.ChallengeWorkspaceService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(com.codetraininglab.platform.web.ApiPaths.CHALLENGES)
public class ChallengeController {

  private final ChallengeService challengeService;
  private final ChallengeWorkspaceService workspaceService;

  public ChallengeController(
      ChallengeService challengeService, ChallengeWorkspaceService workspaceService) {
    this.challengeService = challengeService;
    this.workspaceService = workspaceService;
  }

  @GetMapping
  Page<ChallengeSummary> list(@PageableDefault(size = 20) Pageable pageable) {
    return challengeService.list(pageable);
  }

  @GetMapping("/{slug}")
  ChallengeDetail get(@PathVariable String slug) {
    return challengeService.get(slug);
  }

  @PostMapping("/{slug}/redo")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void redo(Authentication authentication, @PathVariable String slug) {
    workspaceService.redo((UUID) authentication.getPrincipal(), slug);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  ChallengeSummary create(@Valid @RequestBody CreateChallengeRequest request) {
    return challengeService.create(request);
  }
}
