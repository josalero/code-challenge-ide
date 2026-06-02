package com.codetraininglab.catalog.application;

import com.codetraininglab.domain.ProgressState;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChallengeWorkspaceService {

  private final ChallengeRepository challengeRepository;
  private final UserProgressRepository progressRepository;
  private final Clock clock;

  public ChallengeWorkspaceService(
      ChallengeRepository challengeRepository,
      UserProgressRepository progressRepository,
      Clock clock) {
    this.challengeRepository = challengeRepository;
    this.progressRepository = progressRepository;
    this.clock = clock;
  }

  @Transactional
  public void redo(UUID userId, String slug) {
    ChallengeEntity challenge =
        challengeRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
    UserProgressEntity progress =
        progressRepository
            .findByUserIdAndChallengeId(userId, challenge.getId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.CONFLICT, "Nothing to redo — submit the exercise first."));
    if (!progress.isSubmitted()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Exercise is not locked — you can still edit and submit.");
    }
    Instant now = clock.instant();
    progress.setSubmittedAt(null);
    progress.setState(ProgressState.ATTEMPTED);
    progress.setUpdatedAt(now);
    progressRepository.save(progress);
  }
}
