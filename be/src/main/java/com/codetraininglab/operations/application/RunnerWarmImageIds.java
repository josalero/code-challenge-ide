package com.codetraininglab.operations.application;

/** Normalizes Docker image IDs for warm-stamp comparison. */
final class RunnerWarmImageIds {

  private RunnerWarmImageIds() {}

  static boolean matches(String stampedId, String currentId) {
    if (stampedId == null || currentId == null) {
      return false;
    }
    return normalize(stampedId).equals(normalize(currentId));
  }

  static String normalize(String imageId) {
    String trimmed = imageId.trim().toLowerCase();
    if (trimmed.startsWith("sha256:")) {
      return trimmed.substring("sha256:".length());
    }
    return trimmed;
  }
}
