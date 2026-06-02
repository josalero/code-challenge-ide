# Backend architecture

Modular monolith under `com.codetraininglab` (bounded contexts, not layer-first packages).

## Package map

```
domain/           Shared enums (no Spring)
catalog/          Challenges, custom tests (api + application)
coach/            AI coach (api + application)
feedback/         Post-run feedback aggregation
identity/         Auth, /me (api + application)
operations/       DLQ admin (api + application)
submission/       Runs, reports, SSE (api + application + messaging)
integration/      Docker runners, LSP
platform/         config, persistence, security, web
```

## Layers

| Suffix | Role |
| --- | --- |
| `*.api` | REST controllers + DTOs |
| `*.application` | Services, transactions |
| `*.messaging` | Rabbit listeners, queue payloads |
| `integration.*` | Docker runner pool, LSP adapters |
| `platform.*` | Shared infra (no feature rules) |

## Dependency rules

- `domain` → nothing else in `com.codetraininglab`
- `platform` → `domain` only
- Features → `domain`, `platform`, `integration`, own subpackages
- No `catalog` ↔ `submission` direct coupling via repositories
- `platform` must not depend on feature `application` packages

## Entry points

| Task | Start |
| --- | --- |
| Challenges | `catalog.api.ChallengeController` |
| Submit / report | `submission.api.SubmissionController` |
| Queue worker | `submission.messaging.SubmissionJobListener` |
| Runner | `integration.runner.DockerRunnerClient` → `RunnerContainerPool` (pooled, default) |
| Runner ops (admin) | `operations.application.RunnerOpsService` |
| Runtime by language | `submission.application.LanguageRuntimeResolver` |
| Python image | `runners/python/` |

Verify: `./gradlew :be:check` (Java 26).
