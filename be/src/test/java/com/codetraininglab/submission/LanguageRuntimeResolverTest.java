package com.codetraininglab.submission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class LanguageRuntimeResolverTest {

  @Mock private LanguageRepository languageRepository;
  @Mock private LanguageRuntimeRepository runtimeRepository;

  @InjectMocks private LanguageRuntimeResolver resolver;

  private final UUID langId = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private final UUID runtimeId = UUID.fromString("00000000-0000-0000-0000-000000003112");

  @Test
  void resolvesPythonDefaultVersion() {
    ChallengeEntity challenge = challenge("python");
    when(languageRepository.findByName("python"))
        .thenReturn(Optional.of(new LanguageEntity(langId, "python", "Python")));
    when(runtimeRepository.findByLanguageIdAndVersion(langId, "3.12"))
        .thenReturn(
            Optional.of(
                new LanguageRuntimeEntity(
                    runtimeId, langId, "3.12", "code-challenge-ide-runner-python-312:local", true)));

    LanguageRuntimeEntity runtime = resolver.resolve(challenge, null);

    assertThat(runtime.getVersion()).isEqualTo("3.12");
    assertThat(runtime.getDockerImage()).contains("python");
  }

  @Test
  void rejectsInactiveRuntime() {
    ChallengeEntity challenge = challenge("java");
    UUID javaId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    when(languageRepository.findByName("java"))
        .thenReturn(Optional.of(new LanguageEntity(javaId, "java", "Java")));
    when(runtimeRepository.findByLanguageIdAndVersion(javaId, "99"))
        .thenReturn(
            Optional.of(new LanguageRuntimeEntity(UUID.randomUUID(), javaId, "99", "img", false)));

    assertThatThrownBy(() -> resolver.resolve(challenge, "99"))
        .isInstanceOf(ResponseStatusException.class);
  }

  private static ChallengeEntity challenge(String language) {
    return new ChallengeEntity(
        UUID.randomUUID(),
        "slug",
        "Title",
        "desc",
        "starter",
        "{}",
        "git",
        "easy",
        language,
        Instant.EPOCH,
        Instant.EPOCH);
  }
}
