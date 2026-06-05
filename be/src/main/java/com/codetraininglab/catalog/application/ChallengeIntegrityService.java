package com.codetraininglab.catalog.application;

import com.codetraininglab.catalog.api.ChallengeSessionSyncRequest;
import com.codetraininglab.catalog.api.ChallengeSessionSyncRequest.SessionMarkRequest;
import com.codetraininglab.catalog.api.RecordIntegrityEventsRequest;
import com.codetraininglab.catalog.api.RecordIntegrityEventsRequest.IntegrityEventRequest;
import com.codetraininglab.domain.IntegrityEditorSurface;
import com.codetraininglab.domain.IntegrityEventType;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeIntegrityEventEntity;
import com.codetraininglab.platform.persistence.ChallengeIntegrityEventRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChallengeIntegrityService {

  private static final int MAX_CHAR_COUNT = 100_000;
  private static final long MAX_AWAY_MS = 86_400_000L;

  private final ChallengeRepository challengeRepository;
  private final ChallengeIntegrityEventRepository integrityEventRepository;
  private final ChallengeSessionService sessionService;
  private final IntegrityMonitoringService integrityMonitoringService;
  private final Clock clock;

  public ChallengeIntegrityService(
      ChallengeRepository challengeRepository,
      ChallengeIntegrityEventRepository integrityEventRepository,
      ChallengeSessionService sessionService,
      IntegrityMonitoringService integrityMonitoringService,
      Clock clock) {
    this.challengeRepository = challengeRepository;
    this.integrityEventRepository = integrityEventRepository;
    this.sessionService = sessionService;
    this.integrityMonitoringService = integrityMonitoringService;
    this.clock = clock;
  }

  @Transactional
  public void recordSyncCheckpoints(
      UUID userId, String challengeSlug, ChallengeSessionSyncRequest request) {
    if (request == null || request.marks() == null || request.marks().isEmpty()) {
      return;
    }
    List<IntegrityEventRequest> events =
        request.marks().stream().map(this::toIntegrityEventRequest).toList();
    recordEvents(userId, challengeSlug, new RecordIntegrityEventsRequest(events));
  }

  private IntegrityEventRequest toIntegrityEventRequest(SessionMarkRequest mark) {
    return new IntegrityEventRequest(
        mapCheckpointKind(mark.k()),
        mapCheckpointSurface(mark.s()),
        mark.n(),
        mark.d(),
        mark.at());
  }

  private static String mapCheckpointKind(int kind) {
    return switch (kind) {
      case 1 -> "COPY";
      case 2 -> "PASTE";
      case 3 -> "CUT";
      case 4 -> "TAB_HIDDEN";
      case 5 -> "TAB_VISIBLE";
      case 6 -> "WINDOW_BLUR";
      case 7 -> "WINDOW_FOCUS";
      case 8 -> "LARGE_EDIT";
      default ->
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid checkpoint kind");
    };
  }

  private static String mapCheckpointSurface(Integer surface) {
    if (surface == null) {
      return null;
    }
    return switch (surface) {
      case 1 -> "SOLUTION";
      case 2 -> "CUSTOM_TESTS";
      default ->
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid checkpoint surface");
    };
  }

  @Transactional
  public void recordEvents(UUID userId, String challengeSlug, RecordIntegrityEventsRequest request) {
    if (!integrityMonitoringService.isMonitoringEnabled(userId)) {
      return;
    }
    ChallengeEntity challenge =
        challengeRepository
            .findBySlug(challengeSlug)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));

    UUID sessionId = sessionService.activeSessionId(userId, challenge.getId());
    Instant now = clock.instant();
    List<ChallengeIntegrityEventEntity> entities = new ArrayList<>(request.events().size());
    for (IntegrityEventRequest event : request.events()) {
      IntegrityEventType eventType = parseEventType(event.eventType());
      IntegrityEditorSurface editorSurface = parseEditorSurface(event.editorSurface());
      entities.add(
          new ChallengeIntegrityEventEntity(
              UUID.randomUUID(),
              userId,
              challenge.getId(),
              sessionId,
              eventType,
              editorSurface,
              normalizeCharCount(event.charCount()),
              normalizeAwayMs(event.awayMs()),
              normalizeOccurredAt(event.occurredAt(), now)));
    }
    integrityEventRepository.saveAll(entities);
  }

  private static IntegrityEventType parseEventType(String raw) {
    try {
      return IntegrityEventType.valueOf(raw.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid integrity event type");
    }
  }

  private static IntegrityEditorSurface parseEditorSurface(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return IntegrityEditorSurface.valueOf(raw.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid editor surface");
    }
  }

  private static Integer normalizeCharCount(Integer charCount) {
    if (charCount == null || charCount < 0) {
      return null;
    }
    return Math.min(charCount, MAX_CHAR_COUNT);
  }

  private static Long normalizeAwayMs(Long awayMs) {
    if (awayMs == null || awayMs < 0) {
      return null;
    }
    return Math.min(awayMs, MAX_AWAY_MS);
  }

  private static Instant normalizeOccurredAt(Instant clientTime, Instant now) {
    if (clientTime == null) {
      return now;
    }
    if (clientTime.isAfter(now.plusSeconds(30))) {
      return now;
    }
    return clientTime;
  }
}
