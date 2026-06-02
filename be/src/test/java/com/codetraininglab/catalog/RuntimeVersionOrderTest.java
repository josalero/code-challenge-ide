package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RuntimeVersionOrderTest {

  @Test
  void ordersJavaVersionsNewestLastInAscendingCompare() {
    assertThat(RuntimeVersionOrder.compare("25", "26")).isNegative();
    assertThat(RuntimeVersionOrder.compare("26", "25")).isPositive();
    assertThat(RuntimeVersionOrder.newestFirst().compare("25", "26")).isPositive();
  }

  @Test
  void ordersDottedVersionsNumerically() {
    assertThat(RuntimeVersionOrder.compare("5.7", "5.10")).isNegative();
    assertThat(RuntimeVersionOrder.compare("3.12", "3.11")).isPositive();
    assertThat(RuntimeVersionOrder.newestFirst().compare("3.11", "3.12")).isPositive();
  }
}
