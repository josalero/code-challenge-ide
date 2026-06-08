---
name: code-challenge-ide-pro-java-spring-boot
description: >-
  Java 26 / Spring Boot 4 backend for Code Training Lab. Use for be/ work;
  follow 12-java-26-style.mdc, 20-spring-boot-4.mdc, and be/ARCHITECTURE.md.
---

# Code Training Lab — backend

- **Layout:** [be/ARCHITECTURE.md](../../../be/ARCHITECTURE.md)
- **Contracts:** [docs/contracts.md](../../../docs/contracts.md)
- **MVP spec:** [docs/code_training_lab_mvp_specification.md](../../../docs/code_training_lab_mvp_specification.md)
- **Quality:** `./gradlew :be:check` (Java 26)
- **User code runs only in Docker runners** — never in the API JVM

For new features, use the `spring-boot-4-feature` skill workflow and existing bounded-context packages.
