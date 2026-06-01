# Implementation contracts

**Product scope:** [code_training_lab_mvp_specification.md](./code_training_lab_mvp_specification.md)  
**Backend packages:** [be/ARCHITECTURE.md](../be/ARCHITECTURE.md)

When this doc disagrees with code, **trust the code**.

## Runner (stdin / stdout)

One JSON job line in, one JSON result line out.

### Execution on the API host

By default the API uses **pooled runner containers** (`RunnerContainerPool`) — one long-lived container per runner Docker image, with a line-oriented **daemon** on stdin (Java: `runners/java/daemon.py`). Set `RUNNER_POOL_ENABLED=false` for legacy one-shot `docker run --rm` per job.

Operational details, warm vs built, timings: [runner-ops.md](./runner-ops.md).

### Job payload (`RunnerJobPayload`)

| Field | Purpose |
| --- | --- |
| `submission_id` | Correlation id |
| `challenge_slug` | Incremental workspace in pooled mode (same slug → reuse compile cache) |
| `workspace_layout` | See table below |
| `solution_code` | User solution source |
| `custom_tests_code` | Optional custom tests |
| `hidden_tests` | Server-side test sources |
| `limits` | CPU/memory/wall clock caps |

Pooled containers receive `CTL_RUNNER_POOLED=1` from the API (not from `.env`).

### Languages

| Language | `workspace_layout` | Implementation |
| --- | --- | --- |
| Java | `maven` | `runners/java/run.py` (+ `daemon.py` when pooled) |
| Python | `pytest` | `runners/python/run.py` |
| Go | `go-test` | `runners/go/run.py` |
| Node.js | `node-test` | `runners/node/run.py` |
| C# | `dotnet` | `runners/dotnet/run.py` |
| TypeScript | `typescript-test` | `runners/typescript/run.py` |
| Rust | `cargo-test` | `runners/rust/run.py` |
| C++ | `cmake-test` | `runners/cpp/run.py` |
| React | `vitest-react` | `runners/react/run.py` |
| Vue | `vitest-vue` | `runners/vue/run.py` |
| Angular | `vitest-angular` | `runners/angular/run.py` |

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

**Adding or extending challenges:** [adding-challenges.md](./adding-challenges.md).

## Feedback

Categories include `CORRECTNESS`, `COVERAGE`, `STYLE`. Any failing gate can block completion.  
Default line coverage threshold: 80% (`GatingDefaults` / per-challenge `gating_config`).
