package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.codetraininglab.catalog.api.ChallengeSessionSyncRequest;
import com.codetraininglab.catalog.api.ChallengeSessionSyncRequest.SessionMarkRequest;
import com.codetraininglab.catalog.api.RecordIntegrityEventsRequest;
import com.codetraininglab.catalog.api.RecordIntegrityEventsRequest.IntegrityEventRequest;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeIntegrityEventRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ChallengeIntegrityServiceTest {

  @Mock private ChallengeRepository challengeRepository;
  @Mock private ChallengeIntegrityEventRepository integrityEventRepository;
  @Mock private ChallengeSessionService sessionService;
  @Mock private IntegrityMonitoringService integrityMonitoringService;

  private ChallengeIntegrityService service;
  private UUID userId;
  private UUID challengeId;
  private Instant now;

  @BeforeEach
  void setUp() {
    now = Instant.parse("2026-06-04T12:00:00Z");
    service =
        new ChallengeIntegrityService(
            challengeRepository,
            integrityEventRepository,
            sessionService,
            integrityMonitoringService,
            Clock.fixed(now, ZoneOffset.UTC));
    userId = UUID.randomUUID();
    challengeId = UUID.randomUUID();
  }

  @Test
  void recordSyncCheckpointsIgnoresEmptyMarks() {
    service.recordSyncCheckpoints(userId, "two-sum", new ChallengeSessionSyncRequest(1L, List.of()));

    org.mockito.Mockito.verifyNoInteractions(integrityEventRepository);
  }

  @Test
  void recordSyncCheckpointsRejectsInvalidKind() {
    assertThatThrownBy(
            () ->
                service.recordSyncCheckpoints(
                    userId,
                    "two-sum",
                    new ChallengeSessionSyncRequest(
                        1L, List.of(new SessionMarkRequest(99, now, null, null, null)))))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void recordSyncCheckpointsRejectsInvalidSurface() {
    assertThatThrownBy(
            () ->
                service.recordSyncCheckpoints(
                    userId,
                    "two-sum",
                    new ChallengeSessionSyncRequest(
                        1L, List.of(new SessionMarkRequest(1, now, 9, null, null)))))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void recordEventsRejectsInvalidEditorSurface() {
    when(integrityMonitoringService.isMonitoringEnabled(userId)).thenReturn(true);
    ChallengeEntity challenge =
        new ChallengeEntity(
            challengeId,
            "two-sum",
            "Two Sum",
            "desc",
            "code",
            "{}",
            "seed",
            "easy",
            "java",
            45,
            now,
            now);
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));

    assertThatThrownBy(
            () ->
                service.recordEvents(
                    userId,
                    "two-sum",
                    new RecordIntegrityEventsRequest(
                        List.of(new IntegrityEventRequest("COPY", "UNKNOWN", null, null, now)))))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void recordSyncCheckpointsMapsCompactCodes() {
    when(integrityMonitoringService.isMonitoringEnabled(userId)).thenReturn(true);
    ChallengeEntity challenge =
        new ChallengeEntity(
            challengeId,
            "two-sum",
            "Two Sum",
            "desc",
            "code",
            "{}",
            "seed",
            "easy",
            "java",
            45,
            now,
            now);
    UUID sessionId = UUID.randomUUID();
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));
    when(sessionService.activeSessionId(userId, challengeId)).thenReturn(sessionId);

    service.recordSyncCheckpoints(
        userId,
        "two-sum",
        new ChallengeSessionSyncRequest(
            1_700_000_000_000L,
            List.of(new SessionMarkRequest(2, now, 1, 42, null))));

    ArgumentCaptor<List> saved = ArgumentCaptor.forClass(List.class);
    verify(integrityEventRepository).saveAll(saved.capture());
    assertThat(saved.getValue()).hasSize(1);
  }

  @Test
  void recordEventsPersistsWhenMonitoringEnabled() {
    when(integrityMonitoringService.isMonitoringEnabled(userId)).thenReturn(true);
    ChallengeEntity challenge =
        new ChallengeEntity(
            challengeId,
            "two-sum",
            "Two Sum",
            "desc",
            "code",
            "{}",
            "seed",
            "easy",
            "java",
            45,
            now,
            now);
    UUID sessionId = UUID.randomUUID();
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));
    when(sessionService.activeSessionId(userId, challengeId)).thenReturn(sessionId);

    service.recordEvents(
        userId,
        "two-sum",
        new RecordIntegrityEventsRequest(
            List.of(
                new IntegrityEventRequest("TAB_HIDDEN", null, null, null, now.minusSeconds(3)),
                new IntegrityEventRequest("TAB_VISIBLE", null, null, 2500L, now))));

    ArgumentCaptor<List> saved = ArgumentCaptor.forClass(List.class);
    verify(integrityEventRepository).saveAll(saved.capture());
    assertThat(saved.getValue()).hasSize(2);
  }

  @Test
  void recordEventsSkipsWhenMonitoringDisabled() {
    when(integrityMonitoringService.isMonitoringEnabled(userId)).thenReturn(false);

    service.recordEvents(
        userId,
        "two-sum",
        new RecordIntegrityEventsRequest(
            List.of(new IntegrityEventRequest("COPY", "SOLUTION", null, null, now))));

    org.mockito.Mockito.verifyNoInteractions(integrityEventRepository);
  }

  @Test
  void recordEventsRejectsInvalidType() {
    when(integrityMonitoringService.isMonitoringEnabled(userId)).thenReturn(true);
    ChallengeEntity challenge =
        new ChallengeEntity(
            challengeId,
            "two-sum",
            "Two Sum",
            "desc",
            "code",
            "{}",
            "seed",
            "easy",
            "java",
            45,
            now,
            now);
    when(challengeRepository.findBySlug("two-sum")).thenReturn(Optional.of(challenge));

    assertThatThrownBy(
            () ->
                service.recordEvents(
                    userId,
                    "two-sum",
                    new RecordIntegrityEventsRequest(
                        List.of(new IntegrityEventRequest("DRAG", null, null, null, now)))))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
