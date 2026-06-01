package com.codetraininglab.integration.lsp;

import com.codetraininglab.platform.config.CtlProperties;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class LspSessionRegistry {

  private final CtlProperties properties;
  private final Map<String, LspDockerSession> sessions = new ConcurrentHashMap<>();

  public LspSessionRegistry(CtlProperties properties) {
    this.properties = properties;
  }

  public void register(WebSocketSession wsSession, LspDockerSession lspSession) {
    sessions.put(wsSession.getId(), lspSession);
  }

  public LspDockerSession get(WebSocketSession wsSession) {
    return sessions.get(wsSession.getId());
  }

  public void remove(WebSocketSession wsSession) {
    LspDockerSession session = sessions.remove(wsSession.getId());
    if (session != null) {
      session.close();
    }
  }

  @Scheduled(fixedRate = 60_000)
  public void reapIdleSessions() {
    Duration idleLimit = Duration.ofMinutes(Math.max(1, properties.lspIdleMinutes()));
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
