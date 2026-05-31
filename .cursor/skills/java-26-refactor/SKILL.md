---
name: java-26-refactor
description: >-
  Modernize Java code on JDK 26: prefer records over Lombok for DTOs and value
  types unless Lombok is necessary; also sealed types, pattern matching, null
  annotations, and virtual threads where appropriate. Use when the project
  targets Java 26 and build files confirm toolchain/release.
---

# Java 26 Refactor Skill

Use when modernizing code that **targets Java 26**. Treat **`pom.xml` / `build.gradle(.kts)`** and CI as source of truth for `release`, previews, and static analysis.

Activate JDK before builds:

```bash
jenv shell 26
```

## Goals

Improve readability, safety, and maintainability. Prefer **finalized** language features; use **preview** features only if the whole project and CI explicitly allow them.

## Consider

- **Records first** for immutable DTOs and carriers; **prefer records over Lombok** unless Lombok is necessary.
- Sealed classes and pattern matching where they simplify real structure (not for show).
- `switch` patterns and modern `instanceof` patterns when the compiler level supports them.
- Structured logging, clear exceptions, immutable collections, `java.time`.
- Virtual threads for blocking I/O **when** workload and JDBC/resource limits are understood.
- JSpecify (or project nullness) when already adopted.

## Avoid

- Refactoring only to chase novelty.
- Features that confuse readers or violate project minimum JDK assumptions.
- Silent behavior changes.
- **Lombok on new types** when a **record** suffices; use Lombok **only when necessary** and document the reason.
- Relying on blog snippets that predate JDK 26 — verify against **release notes**.

## Required output

Explain:

- What was modernized and which JDK 26 capabilities it relies on.
- Preview vs finalized feature use (if any).
- Whether behavior changed.
- What to run: tests, `./gradlew check` or `mvn verify`, static analysis.
