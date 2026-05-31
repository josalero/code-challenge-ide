package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.submission.messaging.SubmissionEventType;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.FeedbackItemRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import com.codetraininglab.platform.persistence.SubmissionReportRepository;
import com.codetraininglab.platform.persistence.SubmissionRepository;
import com.codetraininglab.platform.persistence.UserProgressRepository;
import com.codetraininglab.integration.runner.RunnerClient;
import com.codetraininglab.integration.runner.RunnerResult;
import tools.jackson.databind.json.JsonMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.TaskScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubmissionProcessorTest {

  @Mock private SubmissionRepository submissionRepository;
  @Mock private SubmissionReportRepository reportRepository;
  @Mock private FeedbackItemRepository feedbackItemRepository;
  @Mock private ChallengeRepository challengeRepository;
  @Mock private ChallengeHiddenTestRepository hiddenTestRepository;
  @Mock private LanguageRuntimeRepository runtimeRepository;
  @Mock private UserProgressRepository progressRepository;
  @Mock private RunnerClient runnerClient;
  @Mock private SubmissionEventHub eventHub;
  @Mock private TaskScheduler taskScheduler;

  private SubmissionProcessor processor;
  private final UUID submissionId = UUID.randomUUID();
  private final UUID challengeId = UUID.randomUUID();

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
    when(taskScheduler.scheduleAtFixedRate(any(), any(), any()))
        .thenReturn(org.mockito.Mockito.mock(ScheduledFuture.class));
    processor =
        new SubmissionProcessor(
            submissionRepository,
            reportRepository,
            feedbackItemRepository,
            challengeRepository,
            hiddenTestRepository,
            runtimeRepository,
            progressRepository,
            runnerClient,
            properties,
            eventHub,
            JsonMapper.builder().build(),
            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
            taskScheduler);
  }

  @Test
  void processesSubmission() {
    SubmissionEntity submission =
        new SubmissionEntity(
            submissionId,
            UUID.randomUUID(),
            challengeId,
            UUID.randomUUID(),
            SubmissionStatus.PENDING,
            "code",
            null,
            null,
            Instant.EPOCH,
            Instant.EPOCH);
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
    when(challengeRepository.findById(challengeId))
        .thenReturn(
            Optional.of(
                new ChallengeEntity(
                    challengeId,
                    "reverse-string",
                    "title",
                    "desc",
                    "starter",
                    "{\"line_coverage_percent\":80}",
                    "git",
                    "easy",
                    "java",
                    Instant.EPOCH,
                    Instant.EPOCH)));
    when(hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(challengeId)).thenReturn(List.of());
    UUID runtimeId = submission.getRuntimeId();
    UUID langId = UUID.randomUUID();
    when(runtimeRepository.findById(runtimeId))
        .thenReturn(
            Optional.of(
                new LanguageRuntimeEntity(
                    runtimeId,
                    langId,
                    "26",
                    "code-challenge-ide-runner-java-26:local",
                    true)));
    when(runnerClient.execute(any(), any(), any(), any(), any()))
        .thenReturn(
            new RunnerResult(
                "COMPLETED",
                List.of(new RunnerResult.TestOutcome("t", "PASS", null, 1)),
                new RunnerResult.CoverageOutcome(90, 80),
                new RunnerResult.CheckstyleOutcome(0, 0),
                null));
    when(progressRepository.findByUserIdAndChallengeId(any(), any())).thenReturn(Optional.empty());
    when(progressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    processor.process(submissionId);

    ArgumentCaptor<SubmissionEntity> captor = ArgumentCaptor.forClass(SubmissionEntity.class);
    verify(submissionRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
    assertThat(captor.getAllValues().getLast().getStatus()).isEqualTo(SubmissionStatus.COMPLETED);
    verify(reportRepository).save(any());
    verify(feedbackItemRepository).saveAll(any());
  }

  @Test
  void marksInfrastructureFailure() {
    SubmissionEntity submission =
        new SubmissionEntity(
            submissionId,
            UUID.randomUUID(),
            challengeId,
            UUID.randomUUID(),
            SubmissionStatus.PENDING,
            "code",
            null,
            null,
            Instant.EPOCH,
            Instant.EPOCH);
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
    when(challengeRepository.findById(challengeId))
        .thenReturn(
            Optional.of(
                new ChallengeEntity(
                    challengeId,
                    "reverse-string",
                    "title",
                    "desc",
                    "starter",
                    "{}",
                    "git",
                    "easy",
                    "java",
                    Instant.EPOCH,
                    Instant.EPOCH)));
    when(hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(challengeId)).thenReturn(List.of());
    UUID runtimeId = submission.getRuntimeId();
    when(runtimeRepository.findById(runtimeId))
        .thenReturn(
            Optional.of(
                new LanguageRuntimeEntity(
                    runtimeId, UUID.randomUUID(), "26", "runner:local", true)));
    when(runnerClient.execute(any(), any(), any(), any(), any()))
        .thenReturn(
            new RunnerResult(
                com.codetraininglab.domain.RunnerStatus.FAILED.name(),
                List.of(
                    new RunnerResult.TestOutcome(
                        "runner",
                        com.codetraininglab.domain.TestOutcomeStatus.FAIL.name(),
                        "Docker error",
                        0)),
                new RunnerResult.CoverageOutcome(0, 0),
                new RunnerResult.CheckstyleOutcome(0, 0),
                new RunnerResult.LogsOutcome("out", "err")));
    when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    processor.process(submissionId);

    ArgumentCaptor<SubmissionEntity> captor = ArgumentCaptor.forClass(SubmissionEntity.class);
    verify(submissionRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
    assertThat(captor.getAllValues().getLast().getStatus()).isEqualTo(SubmissionStatus.FAILED);
    verify(eventHub)
        .publish(
            org.mockito.Mockito.eq(submissionId),
            org.mockito.Mockito.eq(SubmissionEventType.ERROR.eventName()),
            org.mockito.Mockito.argThat(
                (java.util.Map<?, ?> map) -> "Docker error".equals(map.get("message"))));
  }

  @Test
  void skipsCancelledSubmission() {
    SubmissionEntity submission =
        new SubmissionEntity(
            submissionId,
            UUID.randomUUID(),
            challengeId,
            UUID.randomUUID(),
            SubmissionStatus.CANCELLED,
            "code",
            null,
            null,
            Instant.EPOCH,
            Instant.EPOCH);
    when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
    processor.process(submissionId);
    verify(submissionRepository, org.mockito.Mockito.never()).save(any());
  }
}
