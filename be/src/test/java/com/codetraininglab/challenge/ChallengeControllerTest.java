package com.codetraininglab.catalog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codetraininglab.catalog.application.ChallengeService;
import com.codetraininglab.catalog.application.ChallengeService.ChallengeDetail;
import com.codetraininglab.catalog.application.ChallengeService.ChallengeSummary;
import com.codetraininglab.catalog.application.ChallengeService.PublicTestInfo;
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
                "{}",
                List.of(new PublicTestInfo("T1", "")),
                2,
                List.of()));
    assertThat(controller.get("slug").hiddenTestCount()).isEqualTo(2);
  }
}
