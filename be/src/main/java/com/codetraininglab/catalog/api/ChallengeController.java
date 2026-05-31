package com.codetraininglab.catalog.api;

import com.codetraininglab.catalog.application.ChallengeService;
import com.codetraininglab.catalog.application.ChallengeService.ChallengeDetail;
import com.codetraininglab.catalog.application.ChallengeService.ChallengeSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(com.codetraininglab.platform.web.ApiPaths.CHALLENGES)
public class ChallengeController {

  private final ChallengeService challengeService;

  public ChallengeController(ChallengeService challengeService) {
    this.challengeService = challengeService;
  }

  @GetMapping
  Page<ChallengeSummary> list(@PageableDefault(size = 20) Pageable pageable) {
    return challengeService.list(pageable);
  }

  @GetMapping("/{slug}")
  ChallengeDetail get(@PathVariable String slug) {
    return challengeService.get(slug);
  }
}
