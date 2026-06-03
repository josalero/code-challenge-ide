package com.codetraininglab.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestRepository;
import com.codetraininglab.platform.persistence.ChallengePublicTestEntity;
import com.codetraininglab.platform.persistence.ChallengePublicTestRepository;
import com.codetraininglab.platform.persistence.ChallengeRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

  @Mock private ChallengeRepository challengeRepository;
  @Mock private ChallengePublicTestRepository publicTestRepository;
  @Mock private ChallengeHiddenTestRepository hiddenTestRepository;
  @Mock private LanguageRuntimeRepository runtimeRepository;

  @InjectMocks private ChallengeService challengeService;

  @Test
  void returnsChallengeDetail() {
    UUID id = UUID.randomUUID();
    ChallengeEntity entity =
        new ChallengeEntity(
            id, "slug", "Title", "desc", "starter", "{}", "git", "easy", "java", null, Instant.EPOCH, Instant.EPOCH);
    when(challengeRepository.findBySlug("slug")).thenReturn(Optional.of(entity));
    when(publicTestRepository.findByChallengeIdOrderBySortOrderAsc(id))
        .thenReturn(List.of(new ChallengePublicTestEntity(UUID.randomUUID(), id, "TestA", "", 0)));
    when(hiddenTestRepository.findByChallengeIdOrderBySortOrderAsc(id)).thenReturn(List.of());
    when(runtimeRepository.findActiveByLanguageName("java"))
        .thenReturn(
            List.of(
                new LanguageRuntimeEntity(UUID.randomUUID(), UUID.randomUUID(), "26", "img", true)));

    var detail = challengeService.get("slug");
    assertThat(detail.language()).isEqualTo("java");
    assertThat(detail.sessionDurationMinutes()).isEqualTo(30);
    assertThat(detail.publicTests()).extracting("name").containsExactly("TestA");
    assertThat(detail.hiddenTestCount()).isZero();
  }

  @Test
  void listsChallenges() {
    ChallengeEntity entity =
        new ChallengeEntity(
            UUID.randomUUID(), "slug", "Title", "d", "s", "{}", "git", "easy", "java", null, Instant.EPOCH, Instant.EPOCH);
    when(challengeRepository.findAllByOrderByTitleAsc(Pageable.unpaged()))
        .thenReturn(new PageImpl<>(List.of(entity)));
    assertThat(challengeService.list(Pageable.unpaged()).getContent()).hasSize(1);
  }
}
