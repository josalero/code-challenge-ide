package com.codetraininglab.coach.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.SubmissionReportRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AiCoachServiceAlternativesTest {

  @Mock private FeedbackItemRepository feedbackItemRepository;
  @Mock private SubmissionReportRepository reportRepository;
  @Mock private SubmissionRepository submissionRepository;
  @Mock private ChallengeRepository challengeRepository;

  private AiCoachService service;

  @BeforeEach
  void setUp() {
    CtlProperties properties =
        new CtlProperties(
            true,
            "test-jwt-secret-must-be-at-least-32-characters-long",
            24,
            "http://localhost:5173",
            "challenges",
            "runner",
            "",
            "lsp",
            5,
            24,
            "openrouter",
            "",
            "model",
            "http://localhost:11434",
            "ollama", false, false);
    service =
        new AiCoachService(
            feedbackItemRepository,
            reportRepository,
            submissionRepository,
            challengeRepository,
            properties);
  }

  @Test
  void alternativesRequirePassingSubmission() {
    UUID challengeId = UUID.randomUUID();
    when(challengeRepository.findBySlug("slug"))
        .thenReturn(
            Optional.of(
                new ChallengeEntity(
                    challengeId,
                    "slug",
                    "t",
                    "d",
                    "s",
                    "{}",
                    "git",
                    "easy",
                    "java",
                    Instant.EPOCH,
                    Instant.EPOCH)));
    when(submissionRepository.findAll()).thenReturn(java.util.List.of());
    assertThatThrownBy(() -> service.alternatives(UUID.randomUUID(), "slug"))
        .isInstanceOf(ResponseStatusException.class);
  }
}
