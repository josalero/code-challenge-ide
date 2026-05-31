package com.codetraininglab.platform.web;

import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(com.codetraininglab.platform.web.ApiPaths.LANGUAGES)
public class LanguagesController {

  private final LanguageRepository languageRepository;
  private final LanguageRuntimeRepository runtimeRepository;

  public LanguagesController(
      LanguageRepository languageRepository, LanguageRuntimeRepository runtimeRepository) {
    this.languageRepository = languageRepository;
    this.runtimeRepository = runtimeRepository;
  }

  @GetMapping
  public List<LanguageRuntimeResponse> listLanguages() {
    Map<String, String> names =
        languageRepository.findAll().stream()
            .collect(Collectors.toMap(l -> l.getId().toString(), LanguageEntity::getName));
    List<LanguageRuntimeResponse> result = new ArrayList<>();
    for (LanguageRuntimeEntity runtime : runtimeRepository.findAllOrdered()) {
      String language = names.get(runtime.getLanguageId().toString());
      if (language != null) {
        result.add(
            new LanguageRuntimeResponse(language, runtime.getVersion(), runtime.isActive()));
      }
    }
    return result;
  }
}
