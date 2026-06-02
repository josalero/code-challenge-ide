package com.codetraininglab.domain;

import java.util.Set;

public final class JavaRuntimeVersion {

  public static final String DEFAULT = "26";
  public static final Set<String> SUPPORTED = Set.of("25", "26");

  private JavaRuntimeVersion() {}
}
