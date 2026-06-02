package com.codetraininglab.submission.application;

import com.codetraininglab.catalog.application.ChallengeLanguageSupport;
import com.codetraininglab.domain.JavaRuntimeVersion;
import com.codetraininglab.platform.persistence.ChallengeEntity;
import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LanguageRuntimeResolver {

  private final LanguageRepository languageRepository;
  private final LanguageRuntimeRepository runtimeRepository;

  public LanguageRuntimeResolver(
      LanguageRepository languageRepository, LanguageRuntimeRepository runtimeRepository) {
    this.languageRepository = languageRepository;
    this.runtimeRepository = runtimeRepository;
  }

  public LanguageRuntimeEntity resolve(ChallengeEntity challenge, String requestedVersion) {
    String languageName = challenge.getLanguage();
    LanguageEntity language =
        languageRepository
            .findByName(languageName)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Language not configured: " + languageName));
    String version = defaultVersion(languageName, requestedVersion);
    return runtimeRepository
        .findByLanguageIdAndVersion(language.getId(), version)
        .filter(LanguageRuntimeEntity::isActive)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Runtime not available for " + languageName + " " + version));
  }

  private static String defaultVersion(String languageName, String requestedVersion) {
    if (requestedVersion != null && !requestedVersion.isBlank()) {
      return requestedVersion.trim();
    }
    if ("java".equalsIgnoreCase(languageName)) {
      return JavaRuntimeVersion.DEFAULT;
    }
    return ChallengeLanguageSupport.defaultRuntimeVersion(languageName);
  }
}
