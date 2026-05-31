package com.codetraininglab.integration.lsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/** Bridges a WebSocket client to JDT LS running in a Docker container (stdio). */
public final class LspJavaSession implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(LspJavaSession.class);

  private final WebSocketSession clientSession;
  private final Process process;
  private final Path workspace;
  private final ExecutorService ioPool = Executors.newVirtualThreadPerTaskExecutor();
  private final List<Future<?>> tasks = new ArrayList<>();
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private volatile Instant lastActivity = Instant.now();

  private LspJavaSession(WebSocketSession clientSession, Process process, Path workspace) {
    this.clientSession = clientSession;
    this.process = process;
    this.workspace = workspace;
  }

  public static LspJavaSession start(
      WebSocketSession clientSession, String lspImage, String solutionSource) throws IOException {
    Path workspace = LspWorkspaceFactory.create(solutionSource);
    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("run");
    command.add("--rm");
    command.add("-i");
    command.add("--network");
    command.add("none");
    command.add("-v");
    command.add(workspace.toAbsolutePath() + ":/workspace");
    command.add(lspImage);
    Process process = new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD).start();
    LspJavaSession session = new LspJavaSession(clientSession, process, workspace);
    session.startPump();
    return session;
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
                  String chunk = new String(buffer, 0, read, StandardCharsets.UTF_8);
                  if (clientSession.isOpen()) {
                    synchronized (clientSession) {
                      clientSession.sendMessage(new TextMessage(chunk));
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
    output.write(payload.getBytes(StandardCharsets.UTF_8));
    output.flush();
  }

  public boolean isIdleLongerThan(Duration duration) {
    return Duration.between(lastActivity, Instant.now()).compareTo(duration) > 0;
  }

  private void touch() {
    lastActivity = Instant.now();
  }

  @Override
  public void close() {
    if (!closed.compareAndSet(false, true)) {
      return;
    }
    for (Future<?> task : tasks) {
      task.cancel(true);
    }
    ioPool.shutdownNow();
    process.destroyForcibly();
    try {
      Files.deleteIfExists(workspace.resolve("src/main/java/com/challenge/Solution.java"));
      Files.deleteIfExists(workspace.resolve("pom.xml"));
      Files.deleteIfExists(workspace);
    } catch (IOException ignored) {
      // best-effort temp cleanup
    }
    if (clientSession.isOpen()) {
      try {
        clientSession.close(CloseStatus.NORMAL);
      } catch (IOException ignored) {
        // ignore
      }
    }
  }
}
