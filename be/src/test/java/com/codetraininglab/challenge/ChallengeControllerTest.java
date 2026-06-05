package com.codetraininglab.catalog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codetraininglab.catalog.application.ChallengeService;
import com.codetraininglab.catalog.application.ChallengeService.ChallengeDetail;
import com.codetraininglab.catalog.application.ChallengeService.ChallengeSummary;
import com.codetraininglab.catalog.application.ChallengeService.PublicTestInfo;
import com.codetraininglab.catalog.api.ChallengeValidationResponse;
import com.codetraininglab.catalog.api.ChallengeTestPayload;
import com.codetraininglab.catalog.api.ValidateChallengeRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ChallengeControllerTest {

  @Mock private ChallengeService challengeService;

  @InjectMocks private ChallengeController controller;

  @Test
  void listsChallenges() {
    when(challengeService.list(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(new ChallengeSummary("slug", "Title", "easy", "java"))));
    assertThat(controller.list(Pageable.ofSize(20)).getContent()).hasSize(1);
  }

  @Test
  void getsChallenge() {
    when(challengeService.get("slug"))
        .thenReturn(
            new ChallengeDetail(
                "slug",
                "Title",
                "desc",
                "starter",
                "easy",
                "java",
                30,
                "{}",
                List.of(new PublicTestInfo("T1", "")),
                2,
                List.of()));
    assertThat(controller.get("slug").hiddenTestCount()).isEqualTo(2);
  }

  @Test
  void validatesDraftChallenge() {
    var request =
        new ValidateChallengeRequest(
            "draft",
            "java",
            "26",
            "class Solution {}",
            List.of(new ChallengeTestPayload("Public", "public test")),
            List.of(new ChallengeTestPayload("Hidden", "hidden test")));
    var response =
        new ChallengeValidationResponse(
            "COMPLETED",
            true,
            false,
            "assertion",
            new ChallengeValidationResponse.CompileSummary(0, List.of()),
            List.of(
                new ChallengeValidationResponse.TestResult(
                    "sample", "FAIL", "assertion", 1)),
            new ChallengeValidationResponse.Logs("", ""));
    when(challengeService.validateDraft(request)).thenReturn(response);

    assertThat(controller.validate(request).compiled()).isTrue();
  }
}
