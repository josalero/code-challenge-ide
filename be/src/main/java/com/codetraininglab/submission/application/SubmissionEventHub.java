package com.codetraininglab.submission.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SubmissionEventHub {

  private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

  public SseEmitter subscribe(UUID submissionId) {
    SseEmitter emitter = new SseEmitter(300_000L);
    emitters.computeIfAbsent(submissionId, id -> new CopyOnWriteArrayList<>()).add(emitter);
    emitter.onCompletion(() -> remove(submissionId, emitter));
    emitter.onTimeout(() -> remove(submissionId, emitter));
    return emitter;
  }

  public void publish(UUID submissionId, String type, Object payload) {
    List<SseEmitter> list = emitters.get(submissionId);
    if (list == null) {
      return;
    }
    for (SseEmitter emitter : list) {
      try {
        emitter.send(SseEmitter.event().name(type).data(payload));
      } catch (Exception ignored) {
        remove(submissionId, emitter);
      }
    }
  }

  private void remove(UUID submissionId, SseEmitter emitter) {
    List<SseEmitter> list = emitters.get(submissionId);
    if (list != null) {
      list.remove(emitter);
    }
  }
}
