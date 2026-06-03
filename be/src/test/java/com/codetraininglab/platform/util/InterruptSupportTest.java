package com.codetraininglab.platform.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class InterruptSupportTest {

  @Test
  void runWithInterruptCleared_runsActionWhenThreadWasNotInterrupted() {
    AtomicBoolean ran = new AtomicBoolean(false);

    InterruptSupport.runWithInterruptCleared(() -> ran.set(true));

    assertThat(ran).isTrue();
    assertThat(Thread.currentThread().isInterrupted()).isFalse();
  }

  @Test
  void runWithInterruptCleared_runsActionWhenThreadWasInterrupted() {
    Thread.currentThread().interrupt();
    AtomicBoolean ran = new AtomicBoolean(false);

    InterruptSupport.runWithInterruptCleared(() -> ran.set(true));

    assertThat(ran).isTrue();
    assertThat(Thread.currentThread().isInterrupted()).isTrue();
    Thread.interrupted();
  }

  @Test
  void clearInterrupted_clearsFlagWithoutRestoring() {
    Thread.currentThread().interrupt();

    InterruptSupport.clearInterrupted();

    assertThat(Thread.currentThread().isInterrupted()).isFalse();
  }
}
