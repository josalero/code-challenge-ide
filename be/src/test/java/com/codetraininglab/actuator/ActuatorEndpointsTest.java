package com.codetraininglab.actuator;

import static org.assertj.core.api.Assertions.assertThat;

import com.codetraininglab.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;

class ActuatorEndpointsTest extends IntegrationTestBase {

  @Autowired TestRestTemplate rest;

  @Test
  void healthAndInfoArePublic() {
    assertThat(rest.getForEntity("/actuator/health", String.class).getStatusCode())
        .isEqualTo(HttpStatus.OK);
    assertThat(rest.getForEntity("/actuator/health/readiness", String.class).getStatusCode())
        .isEqualTo(HttpStatus.OK);
    assertThat(rest.getForEntity("/actuator/info", String.class).getStatusCode())
        .isEqualTo(HttpStatus.OK);
  }
}
