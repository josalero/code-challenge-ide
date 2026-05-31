package com.codetraininglab.identity.api;

import com.codetraininglab.platform.persistence.UserEntity;
import com.codetraininglab.platform.persistence.UserProgressEntity;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.platform.persistence.UserRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(com.codetraininglab.platform.web.ApiPaths.ME)
public class MeController {

  private final UserRepository userRepository;
  private final UserProgressRepository progressRepository;
  private final ChallengeRepository challengeRepository;

  public MeController(
      UserRepository userRepository,
      UserProgressRepository progressRepository,
      ChallengeRepository challengeRepository) {
    this.userRepository = userRepository;
    this.progressRepository = progressRepository;
    this.challengeRepository = challengeRepository;
  }

  @GetMapping
  MeResponse me(Authentication authentication) {
    UUID userId = (UUID) authentication.getPrincipal();
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return new MeResponse(user.getId(), user.getEmail());
  }

  @GetMapping("/progress")
  List<ProgressEntry> progress(Authentication authentication) {
    UUID userId = (UUID) authentication.getPrincipal();
    return progressRepository.findByUserId(userId).stream()
        .map(this::toEntry)
        .toList();
  }

  private ProgressEntry toEntry(UserProgressEntity entity) {
    String slug =
        challengeRepository
            .findById(entity.getChallengeId())
            .map(c -> c.getSlug())
            .orElse("unknown");
    return new ProgressEntry(slug, entity.getState().name());
  }

  public record MeResponse(UUID id, String email) {}

  public record ProgressEntry(String challengeSlug, String state) {}
}
