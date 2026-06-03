package com.codetraininglab.operations.application;

import java.util.Map;

/** Canonical smoke challenge slug per language for runner pool warm-up. */
public final class RunnerSmokeChallenges {

  private static final Map<String, String> BY_LANGUAGE =
      Map.ofEntries(
          Map.entry("java", "reverse-string"),
          Map.entry("python", "armstrong-number"),
          Map.entry("go", "anagram-check-go"),
          Map.entry("node", "anagram-check-node"),
          Map.entry("typescript", "anagram-check-typescript"),
          Map.entry("csharp", "anagram-check-csharp"),
          Map.entry("rust", "anagram-check-rust"),
          Map.entry("cpp", "anagram-check-cpp"),
          Map.entry("react", "accordion-react"),
          Map.entry("vue", "computed-filter-vue"),
          Map.entry("angular", "double-service-angular"),
          Map.entry("sql", "sql-count-engineering"));

  private RunnerSmokeChallenges() {}

  public static String slugFor(String language) {
    if (language == null || language.isBlank()) {
      return BY_LANGUAGE.get("java");
    }
    return BY_LANGUAGE.getOrDefault(language.trim().toLowerCase(), BY_LANGUAGE.get("java"));
  }
}
