package com.codetraininglab.integration.lsp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Creates a minimal Maven workspace for JDT LS. */
public final class LspWorkspaceFactory {

  private static final String POM =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <project xmlns="http://maven.apache.org/POM/4.0.0"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <groupId>com.challenge</groupId>
        <artifactId>workspace</artifactId>
        <version>1.0-SNAPSHOT</version>
        <properties>
          <maven.compiler.release>21</maven.compiler.release>
          <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        </properties>
      </project>
      """;

  private LspWorkspaceFactory() {}

  public static Path create(String solutionSource) throws IOException {
    Path root = Files.createTempDirectory("ctl-lsp-");
    Path mainDir = root.resolve("src/main/java/com/challenge");
    Files.createDirectories(mainDir);
    Files.writeString(root.resolve("pom.xml"), POM, StandardCharsets.UTF_8);
    String source =
        solutionSource == null || solutionSource.isBlank()
            ? """
            package com.challenge;

            public class Solution {
            }
            """
            : solutionSource;
    Files.writeString(mainDir.resolve("Solution.java"), source, StandardCharsets.UTF_8);
    return root;
  }
}
