package com.codetraininglab.catalog.application;

import com.codetraininglab.catalog.api.CreateChallengeRequest;
import com.codetraininglab.catalog.api.ChallengeValidationResponse;
import com.codetraininglab.catalog.api.ValidateChallengeRequest;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestRepository;
import com.codetraininglab.platform.persistence.ChallengePublicTestRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChallengeService {

  private final ChallengeRepository challengeRepository;
  private final ChallengePublicTestRepository publicTestRepository;
  private final ChallengeHiddenTestRepository hiddenTestRepository;
  private final LanguageRuntimeRepository runtimeRepository;
  private final ChallengePublisher challengePublisher;
  private final ChallengeDraftValidationService draftValidationService;

  public ChallengeService(
      ChallengeRepository challengeRepository,
      ChallengePublicTestRepository publicTestRepository,
      ChallengeHiddenTestRepository hiddenTestRepository,
      LanguageRuntimeRepository runtimeRepository,
      ChallengePublisher challengePublisher,
      ChallengeDraftValidationService draftValidationService) {
    this.challengeRepository = challengeRepository;
    this.publicTestRepository = publicTestRepository;
    this.hiddenTestRepository = hiddenTestRepository;
    this.runtimeRepository = runtimeRepository;
    this.challengePublisher = challengePublisher;
    this.draftValidationService = draftValidationService;
  }

  public ChallengeSummary create(CreateChallengeRequest request) {
    return challengePublisher.create(request);
  }

  public ChallengeValidationResponse validateDraft(ValidateChallengeRequest request) {
    return draftValidationService.validate(request);
  }

  public Page<ChallengeSummary> list(Pageable pageable) {
    return challengeRepository.findAllByOrderByTitleAsc(pageable).map(this::toSummary);
  }

  public ChallengeDetail get(String slug) {
    ChallengeEntity entity =
        challengeRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    List<PublicTestInfo> publicTests =
        publicTestRepository.findByChallengeIdOrderBySortOrderAsc(entity.getId()).stream()
            .map(t -> new PublicTestInfo(t.getName(), t.getDescription()))
            .toList();
    int hiddenCount = hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(entity.getId()).size();
    List<RuntimeOption> runtimes =
        runtimeRepository.findActiveByLanguageName(entity.getLanguage()).stream()
            .sorted(
                (left, right) ->
                    RuntimeVersionOrder.compare(right.getVersion(), left.getVersion()))
            .map(r -> new RuntimeOption(r.getVersion(), r.isActive()))
            .toList();
    int sessionDurationMinutes =
        ChallengeSessionLimits.resolveMinutes(
            entity.getSessionDurationMinutes(), entity.getDifficulty());
    return new ChallengeDetail(
        entity.getSlug(),
        entity.getTitle(),
        entity.getDescriptionMd(),
        entity.getStarterCode(),
        entity.getDifficulty(),
        entity.getLanguage(),
        sessionDurationMinutes,
        entity.getGatingConfig(),
        publicTests,
        hiddenCount,
        runtimes);
  }

  private ChallengeSummary toSummary(ChallengeEntity entity) {
    return new ChallengeSummary(
        entity.getSlug(), entity.getTitle(), entity.getDifficulty(), entity.getLanguage());
  }

  public record ChallengeSummary(String slug, String title, String difficulty, String language) {}

  public record RuntimeOption(String version, boolean active) {}

  public record PublicTestInfo(String name, String description) {}

  public record ChallengeDetail(
      String slug,
      String title,
      String descriptionMd,
      String starterCode,
      String difficulty,
      String language,
      int sessionDurationMinutes,
      String gatingConfig,
      List<PublicTestInfo> publicTests,
      int hiddenTestCount,
      List<RuntimeOption> runtimes) {}
}
