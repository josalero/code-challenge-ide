package com.codetraininglab;

import com.codetraininglab.support.IntegrationTestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Integration tests temporarily disabled — Testcontainers/CI wiring investigation pending.")
class CodeTrainingLabApplicationTest extends IntegrationTestBase {

  @Test
  void contextLoads() {
    // Verifies the Spring Boot application context starts with Testcontainers
  }
}
