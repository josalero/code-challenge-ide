package com.codetraininglab.catalog.application;

import com.codetraininglab.catalog.api.ChallengeTestPayload;
import com.codetraininglab.catalog.api.ChallengeValidationResponse;
import com.codetraininglab.catalog.api.CreateChallengeRequest;
import com.codetraininglab.catalog.api.ValidateChallengeRequest;
import com.codetraininglab.domain.SubmissionKind;
import com.codetraininglab.domain.SubmissionStatus;
import com.codetraininglab.integration.runner.RunnerClient;
import com.codetraininglab.integration.runner.RunnerResult;
import com.codetraininglab.platform.config.CtlProperties;
import com.codetraininglab.platform.persistence.ChallengeHiddenTestEntity;
import com.codetraininglab.platform.persistence.LanguageEntity;
import com.codetraininglab.platform.persistence.LanguageRepository;
import com.codetraininglab.platform.persistence.LanguageRuntimeEntity;
import com.codetraininglab.platform.persistence.LanguageRuntimeRepository;
import com.codetraininglab.platform.persistence.SubmissionEntity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChallengeDraftValidationService {

  private static final Logger log = LoggerFactory.getLogger(ChallengeDraftValidationService.class);

  private final CtlProperties properties;
  private final ChallengePublisher challengePublisher;
  private final LanguageRepository languageRepository;
  private final LanguageRuntimeRepository runtimeRepository;
  private final RunnerClient runnerClient;
  private final Clock clock;

  public ChallengeDraftValidationService(
      CtlProperties properties,
      ChallengePublisher challengePublisher,
      LanguageRepository languageRepository,
      LanguageRuntimeRepository runtimeRepository,
      RunnerClient runnerClient,
      Clock clock) {
    this.properties = properties;
    this.challengePublisher = challengePublisher;
    this.languageRepository = languageRepository;
    this.runtimeRepository = runtimeRepository;
    this.runnerClient = runnerClient;
    this.clock = clock;
  }

  public ChallengeValidationResponse validate(ValidateChallengeRequest request) {
    String language = request.language().trim().toLowerCase();
    LanguageRuntimeEntity runtime = activeRuntime(language, request.defaultRuntimeVersion().trim());
    String draftSlug = draftSlug(request.slug());
    Path validationRoot = validationRoot();
    Path challengeDir = null;
    try {
      Files.createDirectories(validationRoot);
      challengeDir = Files.createTempDirectory(validationRoot, draftSlug + "-");
      CreateChallengeRequest writeRequest = writableRequest(request, draftSlug, language);
      challengePublisher.writeChallengeTree(
          challengeDir,
          writeRequest,
          draftSlug,
          language,
          ChallengeSessionLimits.defaultMinutesForDifficulty("easy"));

      UUID challengeId = UUID.randomUUID();
      SubmissionEntity draftSubmission = draftSubmission(request, runtime.getId(), challengeId);
      List<ChallengeHiddenTestEntity> hiddenTests = hiddenTests(request.hiddenTests(), challengeId);

      log.info(
          "Validating challenge draft slug={} language={} runtime={} publicTests={} hiddenTests={}",
          draftSlug,
          language,
          runtime.getVersion(),
          request.publicTests().size(),
          request.hiddenTests().size());
      RunnerResult result =
          runnerClient.execute(
              draftSubmission, draftSlug, hiddenTests, challengeDir, runtime.getDockerImage());
      ChallengeValidationResponse response = toResponse(result);
      log.info(
          "Challenge draft validation slug={} status={} compiled={} tests={}",
          draftSlug,
          response.status(),
          response.compiled(),
          response.tests().size());
      return response;
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to prepare draft validation workspace", e);
    } finally {
      if (challengeDir != null) {
        deleteRecursively(challengeDir);
      }
    }
  }

  private LanguageRuntimeEntity activeRuntime(String language, String runtimeVersion) {
    LanguageEntity languageEntity =
        languageRepository
            .findByName(language)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Unsupported language: " + language));
    return runtimeRepository
        .findByLanguageIdAndVersion(languageEntity.getId(), runtimeVersion)
        .filter(LanguageRuntimeEntity::isActive)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Runtime not available for " + language + " " + runtimeVersion));
  }

  private Path validationRoot() {
    return Path.of(properties.challengesPath())
        .toAbsolutePath()
        .normalize()
        .resolve(".draft-validation");
  }

  private static String draftSlug(String requestedSlug) {
    String base =
        requestedSlug == null || requestedSlug.isBlank()
            ? "draft"
            : requestedSlug.trim().toLowerCase();
    return base + "-draft-" + UUID.randomUUID();
  }

  private static CreateChallengeRequest writableRequest(
      ValidateChallengeRequest request, String draftSlug, String language) {
    return new CreateChallengeRequest(
        draftSlug,
        "Draft validation",
        "Draft validation",
        "easy",
        language,
        request.defaultRuntimeVersion().trim(),
        request.starterCode(),
        80,
        null,
        request.publicTests(),
        request.hiddenTests());
  }

  private SubmissionEntity draftSubmission(
      ValidateChallengeRequest request, UUID runtimeId, UUID challengeId) {
    Instant now = clock.instant();
    return new SubmissionEntity(
        UUID.randomUUID(),
        UUID.randomUUID(),
        challengeId,
        runtimeId,
        SubmissionStatus.RUNNING,
        SubmissionKind.RUN,
        request.starterCode(),
        "",
        null,
        now,
        now);
  }

  private static List<ChallengeHiddenTestEntity> hiddenTests(
      List<ChallengeTestPayload> tests, UUID challengeId) {
    int[] order = {0};
    return tests.stream()
        .map(
            test ->
                new ChallengeHiddenTestEntity(
                    UUID.randomUUID(), challengeId, test.name(), test.source(), order[0]++))
        .toList();
  }

  private static ChallengeValidationResponse toResponse(RunnerResult result) {
    List<RunnerResult.TestOutcome> runnerTests =
        result.tests() == null ? List.of() : result.tests();
    List<ChallengeValidationResponse.TestResult> tests =
        runnerTests.stream()
            .map(
                test ->
                    new ChallengeValidationResponse.TestResult(
                        test.name(), test.status(), test.message(), test.durationMs()))
            .toList();
    boolean passed =
        "COMPLETED".equalsIgnoreCase(result.status())
            && !tests.isEmpty()
            && tests.stream().allMatch(test -> "PASS".equalsIgnoreCase(test.status()));
    boolean compiled = "COMPLETED".equalsIgnoreCase(result.status());
    String message =
        tests.stream()
            .filter(test -> test.message() != null && !test.message().isBlank())
            .map(ChallengeValidationResponse.TestResult::message)
            .findFirst()
            .orElse(null);
    return new ChallengeValidationResponse(
        result.status(),
        compiled,
        passed,
        message,
        new ChallengeValidationResponse.CompileSummary(
            result.compile().warnings(),
            result.compile().messages().stream()
                .map(
                    messageItem ->
                        new ChallengeValidationResponse.CompileMessage(
                            messageItem.file(), messageItem.line(), messageItem.message()))
                .toList()),
        tests,
        new ChallengeValidationResponse.Logs(
            result.logs().stdoutTruncated(), result.logs().stderrTruncated()));
  }

  private static void deleteRecursively(Path root) {
    try (Stream<Path> paths = Files.walk(root)) {
      paths.sorted(Comparator.reverseOrder())
          .forEach(
              path -> {
                try {
                  Files.deleteIfExists(path);
                } catch (IOException e) {
                  log.warn("Could not delete draft validation path {}", path, e);
                }
              });
    } catch (IOException e) {
      log.warn("Could not clean draft validation workspace {}", root, e);
    }
  }
}
