package com.codetraininglab.integration.lsp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/** Creates temp workspaces on disk for language-server Docker mounts. */
public final class LspWorkspaceSupport {

  private static final Set<String> SUPPORTED =
      Set.of(
          "java",
          "python",
          "go",
          "node",
          "typescript",
          "csharp",
          "rust",
          "cpp",
          "react",
          "vue",
          "angular");

  private static final String JAVA_POM =
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

  private static final String JAVA_ECLIPSE_PROJECT =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <projectDescription>
        <name>workspace</name>
        <comment></comment>
        <projects></projects>
        <buildSpec>
          <buildCommand>
            <name>org.eclipse.jdt.core.javabuilder</name>
            <arguments></arguments>
          </buildCommand>
          <buildCommand>
            <name>org.eclipse.m2e.core.maven2Builder</name>
            <arguments></arguments>
          </buildCommand>
        </buildSpec>
        <natures>
          <nature>org.eclipse.jdt.core.javanature</nature>
          <nature>org.eclipse.m2e.core.maven2Nature</nature>
        </natures>
      </projectDescription>
      """;

  private static final String JAVA_ECLIPSE_CLASSPATH =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <classpath>
        <classpathentry kind="src" output="target/classes" path="src/main/java">
          <attributes>
            <attribute name="optional" value="true"/>
            <attribute name="maven.pomderived" value="true"/>
          </attributes>
        </classpathentry>
        <classpathentry kind="src" output="target/test-classes" path="src/test/java">
          <attributes>
            <attribute name="optional" value="true"/>
            <attribute name="maven.pomderived" value="true"/>
            <attribute name="test" value="true"/>
          </attributes>
        </classpathentry>
        <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-21/"/>
        <classpathentry kind="output" path="target/classes"/>
      </classpath>
      """;

  private static final String NODE_PACKAGE_JSON =
      """
      {
        "name": "workspace",
        "private": true,
        "devDependencies": {
          "typescript": "5.7.3",
          "@types/node": "22.13.10"
        }
      }
      """;

  private static final String TSCONFIG =
      """
      {
        "compilerOptions": {
          "target": "ES2022",
          "module": "ESNext",
          "moduleResolution": "bundler",
          "strict": true,
          "jsx": "react-jsx",
          "skipLibCheck": true,
          "noEmit": true,
          "allowJs": true
        },
        "include": ["*.ts", "*.tsx", "*.js", "*.vue"]
      }
      """;

  private LspWorkspaceSupport() {}

  public static boolean isSupported(String language) {
    return language != null && SUPPORTED.contains(language.trim().toLowerCase());
  }

  public static Path userWorkspaceRoot(Path opsDataDir, UUID userId, String language) {
    return opsDataDir
        .resolve("lsp-workspaces")
        .resolve(userId.toString())
        .resolve(language.trim().toLowerCase());
  }

  /** Writes challenge starter files into {@code root} (creates directories as needed). */
  public static void populate(Path root, String language, String solutionSource) throws IOException {
    String lang = language.trim().toLowerCase();
    switch (lang) {
      case "java" -> populateJava(root, solutionSource);
      case "python" -> populateSingleFile(root, "solution.py", defaultPython(solutionSource));
      case "go" -> populateGo(root, solutionSource);
      case "node" -> populateJsStack(root, "solution.js", defaultNode(solutionSource));
      case "typescript" -> populateJsStack(root, "solution.ts", defaultTypeScript(solutionSource));
      case "react" -> populateJsStack(root, "solution.tsx", defaultReact(solutionSource));
      case "vue" -> populateJsStack(root, "solution.vue", defaultVue(solutionSource));
      case "angular" -> populateJsStack(root, "solution.ts", defaultAngular(solutionSource));
      case "csharp" -> populateCsharp(root, solutionSource);
      case "rust" -> populateRust(root, solutionSource);
      case "cpp" -> populateCpp(root, solutionSource);
      default -> throw new IllegalArgumentException("Unsupported LSP language: " + language);
    }
  }

  public static Path create(String language, String solutionSource) throws IOException {
    Path root = Files.createTempDirectory("ctl-lsp-");
    populate(root, language, solutionSource);
    return root;
  }

  /** Document path relative to {@code /workspace} — must match Monaco model URI on the client. */
  public static String mainDocumentPath(String language) {
    return switch (language.trim().toLowerCase()) {
      case "java" -> "src/main/java/com/challenge/Solution.java";
      case "python" -> "solution.py";
      case "go" -> "solution.go";
      case "node" -> "solution.js";
      case "typescript" -> "solution.ts";
      case "react" -> "solution.tsx";
      case "vue" -> "solution.vue";
      case "angular" -> "solution.ts";
      case "csharp" -> "Solution.cs";
      case "rust" -> "src/lib.rs";
      case "cpp" -> "solution.cpp";
      default -> throw new IllegalArgumentException("Unsupported LSP language: " + language);
    };
  }

  private static void populateJava(Path root, String solutionSource) throws IOException {
    Path mainDir = root.resolve("src/main/java/com/challenge");
    Files.createDirectories(mainDir);
    Files.writeString(root.resolve("pom.xml"), JAVA_POM, StandardCharsets.UTF_8);
    Files.writeString(root.resolve(".project"), JAVA_ECLIPSE_PROJECT, StandardCharsets.UTF_8);
    Files.writeString(root.resolve(".classpath"), JAVA_ECLIPSE_CLASSPATH, StandardCharsets.UTF_8);
    String source =
        solutionSource == null || solutionSource.isBlank()
            ? """
            package com.challenge;

            public class Solution {
            }
            """
            : solutionSource;
    Files.writeString(mainDir.resolve("Solution.java"), source, StandardCharsets.UTF_8);
  }

  private static void populateSingleFile(Path root, String fileName, String content)
      throws IOException {
    Files.createDirectories(root);
    Files.writeString(root.resolve(fileName), content, StandardCharsets.UTF_8);
  }

  private static void populateGo(Path root, String solutionSource) throws IOException {
    Files.createDirectories(root);
    Files.writeString(
        root.resolve("go.mod"),
        """
        module challenge

        go 1.23
        """,
        StandardCharsets.UTF_8);
    Files.writeString(
        root.resolve("solution.go"),
        defaultGo(solutionSource),
        StandardCharsets.UTF_8);
  }

  private static void populateJsStack(Path root, String fileName, String content)
      throws IOException {
    Files.createDirectories(root);
    Files.writeString(root.resolve("package.json"), NODE_PACKAGE_JSON, StandardCharsets.UTF_8);
    Files.writeString(root.resolve("tsconfig.json"), TSCONFIG, StandardCharsets.UTF_8);
    Files.writeString(root.resolve(fileName), content, StandardCharsets.UTF_8);
  }

  private static void populateCsharp(Path root, String solutionSource) throws IOException {
    Files.createDirectories(root);
    Files.writeString(
        root.resolve("workspace.csproj"),
        """
        <Project Sdk="Microsoft.NET.Sdk">
          <PropertyGroup>
            <TargetFramework>net8.0</TargetFramework>
            <ImplicitUsings>enable</ImplicitUsings>
            <Nullable>enable</Nullable>
          </PropertyGroup>
        </Project>
        """,
        StandardCharsets.UTF_8);
    Files.writeString(
        root.resolve("Solution.cs"),
        defaultCsharp(solutionSource),
        StandardCharsets.UTF_8);
  }

  private static void populateRust(Path root, String solutionSource) throws IOException {
    Path src = root.resolve("src");
    Files.createDirectories(src);
    Files.writeString(
        root.resolve("Cargo.toml"),
        """
        [package]
        name = "workspace"
        version = "0.1.0"
        edition = "2021"

        [lib]
        path = "src/lib.rs"
        """,
        StandardCharsets.UTF_8);
    Files.writeString(src.resolve("lib.rs"), defaultRust(solutionSource), StandardCharsets.UTF_8);
  }

  private static void populateCpp(Path root, String solutionSource) throws IOException {
    Files.createDirectories(root);
    Files.writeString(
        root.resolve("compile_flags.txt"),
        "-std=c++20\n-I.",
        StandardCharsets.UTF_8);
    Files.writeString(root.resolve("solution.cpp"), defaultCpp(solutionSource), StandardCharsets.UTF_8);
  }

  private static String defaultPython(String source) {
    return source == null || source.isBlank() ? "def solve():\n    pass\n" : source;
  }

  private static String defaultGo(String source) {
    return source == null || source.isBlank()
        ? """
        package main

        func Solve() {}
        """
        : source;
  }

  private static String defaultNode(String source) {
    return source == null || source.isBlank() ? "export function solve() {}\n" : source;
  }

  private static String defaultTypeScript(String source) {
    return source == null || source.isBlank() ? "export function solve(): void {}\n" : source;
  }

  private static String defaultReact(String source) {
    return source == null || source.isBlank()
        ? """
        export function Solution() {
          return null;
        }
        """
        : source;
  }

  private static String defaultVue(String source) {
    return source == null || source.isBlank()
        ? """
        <script setup lang="ts">
        </script>

        <template>
          <div />
        </template>
        """
        : source;
  }

  private static String defaultAngular(String source) {
    return source == null || source.isBlank()
        ? """
        export function solve(): void {}
        """
        : source;
  }

  private static String defaultCsharp(String source) {
    return source == null || source.isBlank()
        ? """
        public static class Solution {
        }
        """
        : source;
  }

  private static String defaultRust(String source) {
    return source == null || source.isBlank() ? "pub fn solve() {}\n" : source;
  }

  private static String defaultCpp(String source) {
    return source == null || source.isBlank() ? "int solve() { return 0; }\n" : source;
  }
}
