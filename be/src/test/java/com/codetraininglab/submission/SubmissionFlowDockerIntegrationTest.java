package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codetraininglab.support.IntegrationTestBase;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Optional end-to-end run against a real Docker runner on the host.
 * Enable with {@code CTL_INTEGRATION_DOCKER=true} and built runner images.
 */
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
      "ctl.docker-enabled=true",
      "ctl.runner-java-26-image=code-challenge-ide-pro-runner-java-26:local"
    })
@EnabledIfEnvironmentVariable(named = "CTL_INTEGRATION_DOCKER", matches = "true")
@Disabled("Integration tests temporarily disabled — Testcontainers/CI wiring investigation pending.")
class SubmissionFlowDockerIntegrationTest extends IntegrationTestBase {

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  @Test
  void submitCompletesWithDockerRunner() throws Exception {
    String email = "docker-" + UUID.randomUUID() + "@example.com";
    String registerBody =
        """
        {"email":"%s","password":"password1"}
        """
            .formatted(email);
    String authJson =
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerBody))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String token = jsonMapper.readTree(authJson).get("accessToken").asText();

    mockMvc
        .perform(
            get("/api/v1/challenges/reverse-string").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    String solution =
        """
        package com.challenge;

        public class Solution {
          public String reverse(String input) {
            if (input == null) {
              return null;
            }
            return new StringBuilder(input).reverse().toString();
          }
        }
        """;
    String submitBody =
        """
        {"challengeSlug":"reverse-string","runtimeVersion":"26","solutionCode":%s}
        """
            .formatted(jsonMapper.writeValueAsString(solution));
    String createdJson =
        mockMvc
            .perform(
                post("/api/v1/submissions")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(submitBody))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    UUID submissionId = UUID.fromString(jsonMapper.readTree(createdJson).get("id").asText());

    await()
        .atMost(Duration.ofMinutes(3))
        .untilAsserted(
            () -> {
              String statusJson =
                  mockMvc
                      .perform(
                          get("/api/v1/submissions/" + submissionId)
                              .header("Authorization", "Bearer " + token))
                      .andExpect(status().isOk())
                      .andReturn()
                      .getResponse()
                      .getContentAsString();
              JsonNode node = jsonMapper.readTree(statusJson);
              String status = node.get("status").asText();
              assertThat(status)
                  .withFailMessage("submission %s ended as %s", submissionId, status)
                  .isEqualTo("COMPLETED");
              assertThat(node.get("reportId").isNull()).isFalse();
            });
  }
}
