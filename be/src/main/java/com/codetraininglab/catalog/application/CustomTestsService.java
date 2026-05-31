package com.codetraininglab.catalog.application;

import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.CustomTestsEntity;
import com.codetraininglab.platform.persistence.CustomTestsRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomTestsService {

  private final CustomTestsRepository customTestsRepository;
  private final ChallengeRepository challengeRepository;
  private final Clock clock;

  public CustomTestsService(
      CustomTestsRepository customTestsRepository,
      ChallengeRepository challengeRepository,
      Clock clock) {
    this.customTestsRepository = customTestsRepository;
    this.challengeRepository = challengeRepository;
    this.clock = clock;
  }

  public CustomTestsResponse get(UUID userId, String slug) {
    ChallengeEntity challenge = findChallenge(slug);
    return customTestsRepository
        .findByUserIdAndChallengeId(userId, challenge.getId())
        .map(e -> new CustomTestsResponse(e.getCode()))
        .orElse(new CustomTestsResponse(""));
  }

  @Transactional
  public CustomTestsResponse save(UUID userId, String slug, CustomTestsRequest request) {
    ChallengeEntity challenge = findChallenge(slug);
    Instant now = clock.instant();
    CustomTestsEntity entity =
        customTestsRepository
            .findByUserIdAndChallengeId(userId, challenge.getId())
            .orElse(
                new CustomTestsEntity(UUID.randomUUID(), userId, challenge.getId(), request.code(), now));
    entity.setCode(request.code());
    entity.setUpdatedAt(now);
    customTestsRepository.save(entity);
    return new CustomTestsResponse(entity.getCode());
  }

  private ChallengeEntity findChallenge(String slug) {
    return challengeRepository
        .findBySlug(slug)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
  }

  public record CustomTestsRequest(String code) {}

  public record CustomTestsResponse(String code) {}
}
