package com.codetraininglab.integration.lsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/** Bridges a WebSocket client to a language server process (pooled {@code docker exec} or legacy run). */
public final class LspDockerSession implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(LspDockerSession.class);

  /** Legacy one-shot containers (pre pool); kept for orphan cleanup. */
  static final String LSP_SESSION_LABEL = "ctl.lsp-session=true";

  static final String LSP_LANGUAGE_LABEL = "ctl.lsp-language";

  private final WebSocketSession clientSession;
  private final String containerName;
  private final Process process;
  private final Path workspace;
  private final boolean pooled;
  private final UUID userId;
  private final String language;
  private final LspUserLanguagePool pool;
  private final ExecutorService ioPool = Executors.newVirtualThreadPerTaskExecutor();
  private final List<Future<?>> tasks = new ArrayList<>();
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private volatile Instant lastActivity = Instant.now();
  private final LspContentLengthFramer stdoutFramer = new LspContentLengthFramer();

  private LspDockerSession(
      WebSocketSession clientSession,
      String containerName,
      Process process,
      Path workspace,
      boolean pooled,
      UUID userId,
      String language,
      LspUserLanguagePool pool) {
    this.clientSession = clientSession;
    this.containerName = containerName;
    this.process = process;
    this.workspace = workspace;
    this.pooled = pooled;
    this.userId = userId;
    this.language = language;
    this.pool = pool;
  }

  static LspDockerSession attachFromPool(
      WebSocketSession clientSession,
      LspUserLanguagePool pool,
      UUID userId,
      String language,
      String containerName,
      Path workspace)
      throws IOException {
    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("exec");
    command.add("-i");
    for (String env : LspPoolExecCommands.execEnvironment(language)) {
      command.add("-e");
      command.add(env);
    }
    command.add(containerName);
    command.addAll(LspPoolExecCommands.command(language));
    Process process =
        new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.PIPE).start();
    LspDockerSession session =
        new LspDockerSession(
            clientSession, containerName, process, workspace, true, userId, language, pool);
    session.startPump();
    session.watchProcessExit();
    return session;
  }

  private void watchProcessExit() {
    tasks.add(
        ioPool.submit(
            () -> {
              try {
                int exit = process.waitFor();
                if (!closed.get()) {
                  log.warn("LSP process exited with code {}", exit);
                  if (pooled) {
                    close();
                  } else {
                    closeWithReason(
                        CloseStatus.SERVER_ERROR.withReason(
                            "Language server process exited (code " + exit + ")"),
                        true);
                  }
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            }));
    tasks.add(
        ioPool.submit(
            () -> {
              try (InputStream err = process.getErrorStream()) {
                byte[] buffer = new byte[4096];
                int read;
                while (!closed.get() && (read = err.read(buffer)) != -1) {
                  String chunk = new String(buffer, 0, read);
                  if (!chunk.isBlank()) {
                    log.debug("LSP stderr: {}", chunk.trim());
                  }
                }
              } catch (IOException e) {
                if (!closed.get()) {
                  log.debug("LSP stderr pump ended: {}", e.getMessage());
                }
              }
            }));
  }

  private void startPump() {
    tasks.add(
        ioPool.submit(
            () -> {
              byte[] buffer = new byte[8192];
              try (InputStream input = process.getInputStream()) {
                int read;
                while (!closed.get() && (read = input.read(buffer)) != -1) {
                  touch();
                  for (String message : stdoutFramer.feed(buffer, 0, read)) {
                    if (message.isEmpty() || !clientSession.isOpen()) {
                      continue;
                    }
                    synchronized (clientSession) {
                      clientSession.sendMessage(new TextMessage(message));
                    }
                  }
                }
              } catch (Exception e) {
                if (!closed.get()) {
                  log.debug("LSP stdout pump ended: {}", e.getMessage());
                }
              }
            }));
  }

  public void forwardClientMessage(String payload) throws IOException {
    touch();
    OutputStream output = process.getOutputStream();
    output.write(LspContentLengthFramer.toStdioFrame(payload));
    output.flush();
  }

  public boolean isIdleLongerThan(Duration duration) {
    return Duration.between(lastActivity, Instant.now()).compareTo(duration) > 0;
  }

  private void touch() {
    lastActivity = Instant.now();
  }

  public String containerName() {
    return containerName;
  }

  /** Stops the LSP stdio bridge only (pooled container and workspace stay). */
  void closeBridgeOnly() {
    closeWithReason(CloseStatus.NORMAL, false);
  }

  @Override
  public void close() {
    if (pooled && pool != null) {
      pool.releaseBridge(userId, language, this);
      return;
    }
    closeWithReason(CloseStatus.NORMAL, true);
  }

  private void closeWithReason(CloseStatus status, boolean closeClient) {
    if (!closed.compareAndSet(false, true)) {
      return;
    }
    for (Future<?> task : tasks) {
      task.cancel(true);
    }
    ioPool.shutdownNow();
    process.destroyForcibly();
    if (!pooled) {
      LspContainerCleanup.forceRemoveQuietly(containerName);
    }
    if (closeClient && clientSession.isOpen()) {
      try {
        clientSession.close(status);
      } catch (IOException ignored) {
        // ignore
      }
    }
  }
}
