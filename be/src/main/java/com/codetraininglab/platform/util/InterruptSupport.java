package com.codetraininglab.platform.util;

/** Helpers for virtual-thread / docker jobs that must not leave the interrupt flag set for JDBC. */
public final class InterruptSupport {

  private InterruptSupport() {}

  /**
   * Runs {@code action} with the current thread's interrupt flag cleared, then restores it.
   */
  public static void runWithInterruptCleared(Runnable action) {
    boolean interrupted = Thread.interrupted();
    try {
      action.run();
    } finally {
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /** Clears the interrupt flag after handling {@link InterruptedException} without rethrowing. */
  public static void clearInterrupted() {
    Thread.interrupted();
  }
}
