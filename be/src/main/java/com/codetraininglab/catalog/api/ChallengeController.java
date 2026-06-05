package com.codetraininglab.catalog.api;

import com.codetraininglab.catalog.application.ChallengeIntegrityService;
import com.codetraininglab.catalog.application.ChallengeService;
import com.codetraininglab.catalog.application.ChallengeService.ChallengeDetail;
import com.codetraininglab.catalog.application.ChallengeService.ChallengeSummary;
import com.codetraininglab.catalog.application.ChallengeSessionService;
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
  private final ChallengeIntegrityService integrityService;
  private final ChallengeSessionService sessionService;

  public ChallengeController(
      ChallengeService challengeService,
      ChallengeWorkspaceService workspaceService,
      ChallengeIntegrityService integrityService,
      ChallengeSessionService sessionService) {
    this.challengeService = challengeService;
    this.workspaceService = workspaceService;
    this.integrityService = integrityService;
    this.sessionService = sessionService;
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

  @GetMapping("/{slug}/session")
  ChallengeSessionResponse activeSession(Authentication authentication, @PathVariable String slug) {
    return sessionService.activeSession((UUID) authentication.getPrincipal(), slug);
  }

  @PostMapping("/{slug}/session/start")
  ChallengeSessionResponse startSession(Authentication authentication, @PathVariable String slug) {
    return sessionService.startSession((UUID) authentication.getPrincipal(), slug);
  }

  @PostMapping("/{slug}/session/abandon")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void abandonSession(Authentication authentication, @PathVariable String slug) {
    sessionService.abandonSession((UUID) authentication.getPrincipal(), slug);
  }

  @PostMapping("/{slug}/session/sync")
  ChallengeSessionResponse syncSession(
      Authentication authentication,
      @PathVariable String slug,
      @RequestBody(required = false) @Valid ChallengeSessionSyncRequest request) {
    UUID userId = (UUID) authentication.getPrincipal();
    if (request != null) {
      integrityService.recordSyncCheckpoints(userId, slug, request);
    }
    return sessionService.activeSession(userId, slug);
  }

  @PostMapping({"/{slug}/integrity-events", "/{slug}/clipboard-events"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void recordIntegrityEvents(
      Authentication authentication,
      @PathVariable String slug,
      @Valid @RequestBody RecordIntegrityEventsRequest request) {
    integrityService.recordEvents((UUID) authentication.getPrincipal(), slug, request);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  ChallengeSummary create(@Valid @RequestBody CreateChallengeRequest request) {
    return challengeService.create(request);
  }
}
