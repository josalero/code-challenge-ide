package com.codetraininglab.domain;

public enum LanguageCode {
  JAVA("java"),
  PYTHON("python"),
  NODE("node"),
  CSHARP("csharp"),
  GO("go");

  private final String name;

  LanguageCode(String name) {
    this.name = name;
  }

  public String languageName() {
    return name;
  }

  public static LanguageCode fromName(String languageName) {
    if (languageName == null) {
      throw new IllegalArgumentException("language name required");
    }
    for (LanguageCode code : values()) {
      if (code.name.equalsIgnoreCase(languageName)) {
        return code;
      }
    }
    throw new IllegalArgumentException("Unsupported language: " + languageName);
  }
}
