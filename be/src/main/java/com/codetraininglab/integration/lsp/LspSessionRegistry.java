package com.codetraininglab.integration.lsp;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class LspSessionRegistry {

  private final Map<String, LspJavaSession> sessions = new ConcurrentHashMap<>();

  public void register(WebSocketSession wsSession, LspJavaSession lspSession) {
    sessions.put(wsSession.getId(), lspSession);
  }

  public LspJavaSession get(WebSocketSession wsSession) {
    return sessions.get(wsSession.getId());
  }

  public void remove(WebSocketSession wsSession) {
    LspJavaSession session = sessions.remove(wsSession.getId());
    if (session != null) {
      session.close();
    }
  }

  @Scheduled(fixedRate = 60_000)
  public void reapIdleSessions() {
    Duration idleLimit = Duration.ofMinutes(5);
    sessions
        .entrySet()
        .removeIf(
            entry -> {
              if (entry.getValue().isIdleLongerThan(idleLimit)) {
                entry.getValue().close();
                return true;
              }
              return false;
            });
  }
}
