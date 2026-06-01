package com.codetraininglab.integration.lsp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

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

  public static Path create(String language, String solutionSource) throws IOException {
    String lang = language.trim().toLowerCase();
    return switch (lang) {
      case "java" -> createJava(solutionSource);
      case "python" -> createSingleFile("solution.py", defaultPython(solutionSource));
      case "go" -> createGo(solutionSource);
      case "node" -> createJsStack("solution.js", defaultNode(solutionSource));
      case "typescript" -> createJsStack("solution.ts", defaultTypeScript(solutionSource));
      case "react" -> createJsStack("solution.tsx", defaultReact(solutionSource));
      case "vue" -> createJsStack("solution.vue", defaultVue(solutionSource));
      case "angular" -> createJsStack("solution.ts", defaultAngular(solutionSource));
      case "csharp" -> createCsharp(solutionSource);
      case "rust" -> createRust(solutionSource);
      case "cpp" -> createCpp(solutionSource);
      default -> throw new IllegalArgumentException("Unsupported LSP language: " + language);
    };
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

  private static Path createJava(String solutionSource) throws IOException {
    Path root = Files.createTempDirectory("ctl-lsp-");
    Path mainDir = root.resolve("src/main/java/com/challenge");
    Files.createDirectories(mainDir);
    Files.writeString(root.resolve("pom.xml"), JAVA_POM, StandardCharsets.UTF_8);
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

  private static Path createSingleFile(String fileName, String content) throws IOException {
    Path root = Files.createTempDirectory("ctl-lsp-");
    Files.writeString(root.resolve(fileName), content, StandardCharsets.UTF_8);
    return root;
  }

  private static Path createGo(String solutionSource) throws IOException {
    Path root = Files.createTempDirectory("ctl-lsp-");
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
    return root;
  }

  private static Path createJsStack(String fileName, String content) throws IOException {
    Path root = Files.createTempDirectory("ctl-lsp-");
    Files.writeString(root.resolve("package.json"), NODE_PACKAGE_JSON, StandardCharsets.UTF_8);
    Files.writeString(root.resolve("tsconfig.json"), TSCONFIG, StandardCharsets.UTF_8);
    Files.writeString(root.resolve(fileName), content, StandardCharsets.UTF_8);
    return root;
  }

  private static Path createCsharp(String solutionSource) throws IOException {
    Path root = Files.createTempDirectory("ctl-lsp-");
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
    return root;
  }

  private static Path createRust(String solutionSource) throws IOException {
    Path root = Files.createTempDirectory("ctl-lsp-");
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
    return root;
  }

  private static Path createCpp(String solutionSource) throws IOException {
    Path root = Files.createTempDirectory("ctl-lsp-");
    Files.writeString(
        root.resolve("compile_flags.txt"),
        "-std=c++20\n-I.",
        StandardCharsets.UTF_8);
    Files.writeString(root.resolve("solution.cpp"), defaultCpp(solutionSource), StandardCharsets.UTF_8);
    return root;
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
