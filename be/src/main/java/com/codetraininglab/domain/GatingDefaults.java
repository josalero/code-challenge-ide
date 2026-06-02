package com.codetraininglab.domain;

/**
 * Default values used by the feedback aggregator when a challenge does not override them.
 *
 * <p>Only tests gate submissions. Coverage and readability are informational and surfaced as
 * {@code warn} feedback items instead of {@code fail}.
 */
public final class GatingDefaults {

  public static final double LINE_COVERAGE_PERCENT = 80.0;
  public static final int MAX_COMPILE_WARNINGS = 0;

  private GatingDefaults() {}
}
