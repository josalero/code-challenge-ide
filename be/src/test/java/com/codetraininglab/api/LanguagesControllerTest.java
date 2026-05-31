package com.codetraininglab.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.platform.web.LanguagesController;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LanguagesControllerTest {

  @Mock private LanguageRepository languageRepository;
  @Mock private LanguageRuntimeRepository runtimeRepository;

  @InjectMocks private LanguagesController controller;

  @Test
  void listsActiveRuntimesWithLanguageName() {
    UUID javaId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID pythonId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    when(languageRepository.findAll())
        .thenReturn(
            List.of(
                new LanguageEntity(javaId, "java", "Java"),
                new LanguageEntity(pythonId, "python", "Python")));
    when(runtimeRepository.findAllOrdered())
        .thenReturn(
            List.of(
                new LanguageRuntimeEntity(
                    UUID.randomUUID(), javaId, "26", "runner-java-26", true),
                new LanguageRuntimeEntity(
                    UUID.randomUUID(), pythonId, "3.12", "runner-python-312", true)));

    var result = controller.listLanguages();

    assertThat(result).hasSize(2);
    assertThat(result).anyMatch(r -> r.language().equals("java") && r.version().equals("26"));
    assertThat(result).anyMatch(r -> r.language().equals("python") && r.version().equals("3.12"));
  }
}
