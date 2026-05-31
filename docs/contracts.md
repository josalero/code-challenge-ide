# Implementation contracts

**Product scope:** [code_training_lab_mvp_specification.md](./code_training_lab_mvp_specification.md)  
**Backend packages:** [be/ARCHITECTURE.md](../be/ARCHITECTURE.md)

When this doc disagrees with code, **trust the code**.

## Runner (stdin / stdout)

One JSON job line in, one JSON result line out.

| Language | `workspace_layout` | Implementation |
| --- | --- | --- |
| Java | `maven` | `RunnerJobPayload.java`, `runners/java/run.py` |
| Python | `pytest` | `runners/python/run.py` |

Result shape: `RunnerResult.java` (`status`, `tests`, `coverage`, `checkstyle`, `logs`).

## Submissions

| Status | Meaning |
| --- | --- |
| `PENDING` | Queued |
| `RUNNING` | Runner active |
| `COMPLETED` | Runner finished (gates may still fail) |
| `FAILED` | Infrastructure/runner error |
| `CANCELLED` | User cancelled while `PENDING` or `RUNNING` |

`Idempotency-Key` header: same user + key within 24h returns the existing submission.

## SSE (`GET /api/v1/submissions/{id}/events`)

Event names: `status`, `test_result`, `done`, `error` — see `SubmissionEventType` and `SsePayloadKeys`.

## Challenges (`challenges/{slug}/`)

`challenge.yml` + `starter/` + `public/tests/` + `hidden/tests/`.  
`GET /api/v1/challenges/{slug}` exposes public test **names** only; hidden source stays server-side.

## Feedback

Categories include `CORRECTNESS`, `COVERAGE`, `STYLE`. Any failing gate can block completion.  
Default line coverage threshold: 80% (`GatingDefaults` / per-challenge `gating_config`).
