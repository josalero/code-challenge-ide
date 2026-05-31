---
name: spring-boot-4-feature
description: >-
  Implement new Spring Boot 4 features using a vertical-slice workflow (DTOs,
  controller, service, repository, validation, tests). Use when adding REST
  behavior, services, or persistence on Spring Boot 4 / Jakarta EE.
---

# Spring Boot 4 Feature Implementation Skill

Use this skill when implementing a new Spring Boot 4 feature in **`be/`**.

## Prerequisites

- Confirm **Java 26** toolchain in build files and `.java-version`.
- Use **`jakarta.*`**, not `javax.*`, for web/JPA/validation.
- Follow `.cursor/rules/20-spring-boot-4.mdc` and `12-java-26-style.mdc`.

## Workflow

1. Understand the requested feature (see `docs/code_training_lab_mvp_specification.md` when relevant).
2. Inspect existing packages, controllers, services, repositories, DTOs, and tests.
3. Identify the smallest vertical slice.
4. Create or update:
   - Request DTO (prefer **record**)
   - Response DTO (prefer **record**)
   - Controller
   - Service
   - Repository if needed
   - Mapper if the project uses one
   - Validation (`jakarta.validation`)
   - Tests (JUnit 5/6 per project)
5. Avoid exposing JPA entities directly unless the project already does.
6. Add Flyway/Liquibase migration if schema changes are required.
7. Run quality gates: `./gradlew check` or `mvn verify`.

## Domain areas (this product)

| Area | Typical packages |
| --- | --- |
| Submissions | `POST /submissions`, queue handoff |
| Reports | `GET /reports/{id}`, feedback aggregation |
| Challenges | `GET /challenges` |
| Languages / runtimes | `GET /languages`, `language_runtimes` |
| Custom tests | `POST /custom-tests` |

## Output

After changes, summarize:

- Files changed
- Behavior added
- Tests added
- Commands to run (`jenv shell 26` then `./gradlew check`)
- Risks or migration notes (Jackson 3, Security 6, etc.)
