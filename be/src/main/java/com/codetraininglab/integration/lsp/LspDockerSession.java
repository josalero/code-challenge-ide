package com.codetraininglab.integration.lsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/** Bridges a WebSocket client to a language server running in Docker (stdio). */
public final class LspDockerSession implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(LspDockerSession.class);

  private final WebSocketSession clientSession;
  private final Process process;
  private final Path workspace;
  private final ExecutorService ioPool = Executors.newVirtualThreadPerTaskExecutor();
  private final List<Future<?>> tasks = new ArrayList<>();
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private volatile Instant lastActivity = Instant.now();
  private final LspContentLengthFramer stdoutFramer = new LspContentLengthFramer();

  private LspDockerSession(WebSocketSession clientSession, Process process, Path workspace) {
    this.clientSession = clientSession;
    this.process = process;
    this.workspace = workspace;
  }

  public static LspDockerSession start(
      WebSocketSession clientSession, String language, String lspImage, String solutionSource)
      throws IOException {
    Path workspace = LspWorkspaceSupport.create(language, solutionSource);
    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("run");
    command.add("--rm");
    command.add("-i");
    command.add("--network");
    command.add("none");
    command.add("-v");
    command.add(workspace.toAbsolutePath() + ":/workspace");
    command.add("-e");
    command.add("CTL_LSP_LANGUAGE=" + language);
    command.add(lspImage);
    Process process =
        new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.PIPE).start();
    LspDockerSession session = new LspDockerSession(clientSession, process, workspace);
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
                  log.warn("LSP docker process exited with code {}", exit);
                  closeWithReason(
                      CloseStatus.SERVER_ERROR.withReason(
                          "Language server process exited (code " + exit + ")"));
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

  @Override
  public void close() {
    closeWithReason(CloseStatus.NORMAL);
  }

  private void closeWithReason(CloseStatus status) {
    if (!closed.compareAndSet(false, true)) {
      return;
    }
    for (Future<?> task : tasks) {
      task.cancel(true);
    }
    ioPool.shutdownNow();
    process.destroyForcibly();
    deleteWorkspace(workspace);
    if (clientSession.isOpen()) {
      try {
        clientSession.close(status);
      } catch (IOException ignored) {
        // ignore
      }
    }
  }

  private static void deleteWorkspace(Path root) {
    if (root == null || !Files.exists(root)) {
      return;
    }
    try (Stream<Path> walk = Files.walk(root)) {
      walk.sorted(Comparator.reverseOrder()).forEach(LspDockerSession::deleteQuietly);
    } catch (IOException ignored) {
      // best-effort temp cleanup
    }
  }

  private static void deleteQuietly(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException ignored) {
      // ignore
    }
  }
}
