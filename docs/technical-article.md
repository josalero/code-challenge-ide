![Building Code Training Lab](./assets/code-challenge-ide-pro-banner.png)

# Building Code Training Lab: a self-hosted multi-language coding platform

## What is it?

Code Training Lab is a self-hosted coding challenge platform where developers solve problems in eleven different languages, get automated test feedback, and receive AI coaching — all inside a browser-based IDE with real IntelliSense. Think LeetCode or Exercism, but running entirely on your own hardware, with no external SaaS dependency for code execution.

The project has three moving parts:

1. **A Spring Boot 4 API** that accepts code submissions, dispatches them to isolated Docker containers, streams results in real time, and serves challenge content.
2. **A React 19 SPA** with a Monaco editor that looks and feels like VS Code — resizable panels, syntax highlighting, diagnostics, autocompletion — connected to real language servers over WebSocket.
3. **Eleven execution sandboxes + seven LSP containers**, each a separate Docker image, isolated from the network and host filesystem.

---

## Why build it?

Three practical motivations drove the design:

**Full sandbox isolation.** Commercial platforms often use Node.js VMs, Python `exec`, or shared JVM processes for execution. That works for homogeneous stacks but gets messy across languages. Here, every language gets its own container with a real toolchain: `mvn test`, `pytest`, `go test`, `cargo test`, etc. The grading output is exactly what a developer would see in a terminal.

**Real IntelliSense, not a toy.** Most browser code editors ship with basic tokenizer-based highlighting. This project connects Monaco to actual language protocol servers — JDT LS for Java, Pyright for Python, `gopls` for Go — so users get hover docs, completion, and inline error markers driven by the same tools their IDE uses.

**Control over data.** Running this on-premises means challenge source, user progress, submission history, and any PII stay inside your own infrastructure.

---

## High-level architecture

```
Browser (React + Monaco)
    │
    ├──  /api/v1/*                          HTTP/REST (Spring Boot)
    ├──  /api/v1/submissions/{id}/events    SSE stream
    └──  /api/v1/lsp/{language}            WebSocket (LSP)
              │
         Spring Boot API (Java 26)
              │
    ┌─────────┼──────────────────────────────┐
    │         │                              │
 PostgreSQL  RabbitMQ          Docker socket (/var/run/docker.sock)
 (challenge, (submission       │
  user,      queue)            ├── ctl-runner-pool-{image}   (one per language)
  progress,                    │         └── daemon.py on stdin
  submissions)                 │
                               └── ctl-lsp-pool-{user}-{language}  (one per user × language)
                                         └── docker exec -i  ← LSP stdio bridge
```

The API is a **modular monolith** — one deployable JVM process split into bounded-context packages (`catalog`, `submission`, `identity`, `coach`, `operations`, `integration`, `platform`) rather than microservices. That choice keeps operational complexity low for a small team while preserving clean module boundaries.

---

## The execution model: Docker-out-of-Docker

Code execution is the most security-sensitive part. The approach is **Docker-out-of-Docker**: the API container mounts the host's `/var/run/docker.sock` and uses the Docker CLI to spawn sibling containers on the host, rather than launching children inside itself.

```
Host
├── docker.sock
├── code-challenge-ide-pro-api  (Spring Boot, mounts docker.sock)
│   └── $ docker exec -i ctl-runner-pool-java-26-local …
└── ctl-runner-pool-java-26-local  ← sibling, not child
    ├── --network none             ← no outbound network
    ├── --cpus / --memory caps     ← resource limits
    └── /challenge:ro              ← challenge files read-only
```

Runners get `--network none` and a read-only challenge mount. The user solution is the only writable thing inside the container (`/workspace`).

### The pool + daemon pattern

A naive approach — `docker run --rm` per submission — takes 2–5 seconds just for container startup, plus tool initialization. For Java that is an additional 10–20 seconds for the JVM and Maven to cold-start.

The fix is **pooled runner containers**: the API keeps one long-lived container per language image. Inside the Java container, a lightweight Python daemon (`runners/java/daemon.py`) listens on stdin for JSON job lines and returns one JSON result line per job:

```
stdin  → {"submission_id": "…", "solution_code": "…", "hidden_tests": "…", …}
stdout ← {"status": "COMPLETED", "tests": […], "coverage": 0.91, …}
```

The API sends jobs via `docker exec -i` into the running container, so there is no container startup overhead on repeated runs. Challenge files are synced via `docker cp` only when the slug changes; the Maven `target/` directory persists between runs of the same challenge, giving incremental compilation.

**Timings (Java):**

| Scenario | Typical time |
| --- | --- |
| First run (cold pool) | ~15–25 s |
| Repeat run, same challenge, edited solution | ~5–10 s |

---

## The submission pipeline

Submissions follow an async pipeline rather than a synchronous HTTP request, for two reasons: execution can take 30–60 seconds, and clients may disconnect and reconnect mid-run.

```
POST /api/v1/submissions  →  SubmissionService
      ↓
  save to Postgres (status=PENDING)
  publish SubmissionJobMessage to RabbitMQ
      ↓
  return 202 Accepted with submission ID
      ↓  (client subscribes)
  GET /api/v1/submissions/{id}/events  (SSE)
      ↓
SubmissionJobListener  (RabbitMQ consumer)
  → DockerRunnerClient / RunnerContainerPool
  → AI coach (feedback)
  → persist result
  → push SSE events: status · test_result* · done
```

SSE events are pushed through `SubmissionEventHub`, an in-memory pub/sub keyed by submission ID. The frontend receives `test_result` events as they stream in — the runner flushes each test case as it completes — and a final `done` event.

Idempotency is built in: the same `Idempotency-Key` header from the same user within 24 hours returns the existing submission rather than running the code twice.

---

## IntelliSense: LSP over WebSocket

Monaco Editor natively speaks the Language Server Protocol on a channel — it needs something to talk to. The backend bridges Monaco's WebSocket to a real language server's stdio.

### The evolution: from per-tab containers to per-user pools

The first iteration started a `docker run` for every WebSocket connection — one container per open editor tab. This created immediate container sprawl: three browser tabs for Java = three `ctl-lsp-java-*` containers, each doing a cold JDT LS initialization (~3–4 s).

The current model uses **per-user language pools**:

```
User A opens Java challenge (tab 1)
  → pool key: {userId}:java
  → start ctl-lsp-pool-a1b2c3d4-java  (sleep infinity, /workspace mounted)
  → docker exec -i … /entrypoint.sh   ← JDT LS stdio bridge

User A opens Java challenge (tab 2)
  → pool key: {userId}:java            (same key)
  → container already running
  → docker exec -i … /entrypoint.sh   ← new bridge, same warm container

User B opens Java challenge
  → pool key: {userId-B}:java          (different user → separate container)
  → start ctl-lsp-pool-b2c3d4e5-java
```

The pool container runs `sleep infinity` as entrypoint. Each editor session attaches via `docker exec -i`, which starts the language server process inside the already-warm container. When the tab closes, only the exec bridge is torn down; the pool container stays warm for the next open.

### Workspace sync

Each pool maps to a stable directory on the host:

```
{ops-data-dir}/lsp-workspaces/{userId}/{language}/
```

That directory is volume-mounted into the container at `/workspace`. When a user opens a different challenge, `LspWorkspaceSupport.populate()` rewrites the source files in place — no container restart needed.

For Java that means rewriting `src/main/java/com/challenge/Solution.java` and keeping `pom.xml` stable so JDT LS's project state remains valid across challenge switches.

### Language server wiring

```
Monaco (browser)
  ↓  WebSocket  /api/v1/lsp/java
JwtWebSocketHandshakeInterceptor   (validates JWT, extracts userId)
  ↓
LspWebSocketHandler
  ↓
LspUserLanguagePool.attach(userId, language, image, solution)
  ↓  ensures pool container running, populates /workspace
LspDockerSession.attachFromPool(...)
  ↓  docker exec -i -e CTL_LSP_LANGUAGE=java ctl-lsp-pool-… /entrypoint.sh
stdin  ← Content-Length framed LSP messages from Monaco
stdout → Content-Length framed LSP messages to Monaco
```

`LspContentLengthFramer` handles the LSP stdio framing (each message is preceded by `Content-Length: N\r\n\r\n`) and reassembles partial reads before forwarding to the WebSocket client.

Idle pools are evicted by a 60-second scheduled task using the same `lsp-idle-minutes` threshold configured in `.env`.

---

## The warm-up system

Language toolchains are slow to cold-start. JDT LS needs several seconds to initialize an Eclipse workspace. Maven needs to locate JARs on first compile. The Admin Ops page exists to pay that cost once, ahead of time, so learners do not wait.

**Three warm concepts:**

| Concept | What it does | Artifact |
| --- | --- | --- |
| **Runner warm** | Starts pool container + runs a smoke submission | `.ctl-runner-pool-warm-stamp` |
| **Maven cache warm** | Copies baked `/opt/m2` from the runner image into a shared Docker volume | `code-challenge-ide-pro-runner-m2-cache` volume |
| **LSP warm** | Sends an LSP `initialize` handshake to each unique image | `.ctl-lsp-warm-stamp` |

The Ops page reads these stamps, compares stored image IDs against `docker image inspect` output, and marks a language **cold** when the stamp image ID does not match (i.e., `make runners` rebuilt the image). Clicking **Warm everything** or **Re-warm everything** triggers `RunnerOpsService`, which orchestrates all three in the right order and streams progress back to the admin UI via SSE.

---

## The backend package structure

The backend is organized as a **bounded-context monolith** rather than layers:

```
com.codetraininglab
├── domain/          Shared enums (RunnerStatus, SubmissionStatus, …) — no Spring
├── catalog/         Challenge CRUD, custom tests
│   ├── api/         ChallengeController, DTOs
│   └── application/ ChallengeService, ChallengeGitLoader
├── submission/      Run, Submit, SSE, report
│   ├── api/
│   ├── application/ SubmissionService, LanguageRuntimeResolver
│   └── messaging/   SubmissionJobListener (RabbitMQ consumer)
├── coach/           AI feedback (OpenRouter / Ollama adapter)
├── feedback/        Post-run feedback aggregation
├── identity/        Auth, JWT, /me
├── operations/      Admin Ops, DLQ endpoint
├── integration/
│   ├── runner/      DockerRunnerClient, RunnerContainerPool, DockerRunnerCommands
│   └── lsp/         LspUserLanguagePool, LspDockerSession, LspWebSocketHandler
└── platform/        Config, security, persistence infra, web constants
```

Dependency rules are enforced by convention:
- `domain` imports nothing inside `com.codetraininglab`
- `platform` imports only `domain`
- Feature packages (`submission`, `catalog`, …) may import `domain`, `platform`, `integration`, and their own subpackages — never each other's `application` layer

This keeps the model clean without introducing a microservice network boundary.

---

## The frontend: a browser IDE

The frontend is a React 19 + Vite + TypeScript SPA. The coding workspace is the most complex screen.

### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│  ← Challenges   Two Sum (Java 26)   ● autosaved   [Run] [Submit] │
├────────────────┬─────────────────────────────────────────────────┤
│                │                                                  │
│  Problem       │   Monaco Editor                                  │
│  statement     │   (Solution tab / Custom tests tab)             │
│  stats         │                                                  │
│  public tests  │                                                  │
│                ├─────────────────────────────────────────────────┤
│                │  Tests │ Compiler │ Analysis │ Feedback │ History│
│                │  (resizable output panel)                        │
└────────────────┴─────────────────────────────────────────────────┘
```

Both the horizontal split (problem ↔ editor) and the vertical split (editor ↔ output) are draggable via `react-resizable-panels`. On mobile the layout collapses to a tab strip and a bottom output sheet.

### Why Monaco + LSP instead of CodeMirror

Monaco is the editor engine of VS Code. It ships with first-class support for the Language Server Protocol client — you register an LSP WebSocket connection and it handles all protocol negotiation, document synchronization, and capability negotiation. The alternative (CodeMirror) would have required implementing LSP client logic from scratch. Monaco's main downside is bundle size (~2.5 MB gzipped), which is acceptable for a coding platform where users expect a full IDE.

### The run state machine

The output panel is driven by an explicit state machine (`domain/workspaceRunState.ts`) rather than scattered boolean flags:

```
idle → loading → running → {
  compilation_error,
  failed_test,
  timeout,
  service_unavailable,
  successful_run,
  successful_submission
}
```

Each state maps to a specific UI: a spinner during `running`, a diff view during `failed_test`, an AI coach card after `successful_submission`.

---

## The AI coach

After a **Submit** (not a practice **Run**), the platform calls an AI provider to generate coaching feedback. The coach receives the solution, test results, coverage data, and style analysis, and returns structured feedback categorized as `CORRECTNESS`, `COVERAGE`, or `STYLE`.

The AI layer is abstracted behind a port interface. Two adapters ship out of the box:

- **OpenRouter** — cloud API, works with any OpenAI-compatible model
- **Ollama** — local inference, zero cost, works offline

Configure via `.env`:

```bash
AI_PROVIDER=openrouter
OPENROUTER_API_KEY=sk-…
OPENROUTER_MODEL=openai/gpt-4o-mini

# or for fully local inference:
AI_PROVIDER=ollama
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=qwen2.5-coder
```

---

## Challenge authoring

Challenges live as directories under `challenges/` and are loaded at API startup by `ChallengeGitLoader`:

```
challenges/
└── two-sum/
    ├── challenge.yml       # metadata: title, slug, languages, gating_config
    ├── starter/
    │   ├── java/Solution.java
    │   └── python/solution.py
    ├── public/tests/       # test descriptions shown to the user
    └── hidden/tests/       # full test sources — never sent to browser
```

The `hidden/tests/` directory is the critical security boundary: test code never leaves the server. The frontend only receives test names from `public/tests/`, so users cannot read the assertions they need to satisfy.

A Python seed script (`scripts/seed-challenges/generate.py`) can bulk-generate challenge directory trees for all 11 languages from a catalog definition.

---

## Security model

| Concern | Approach |
| --- | --- |
| Code execution isolation | `--network none`, read-only challenge mount, resource caps (`--cpus`, `--memory`) |
| Hidden tests | Never included in API responses; passed to runner as server-side payload only |
| Auth | JWT (HS256, configurable secret and expiry) |
| CORS | Explicit origin whitelist in `.env` (`ctl.cors`) |
| Docker socket access | API container only; runner/LSP containers do not receive socket access |
| LSP WebSocket | JWT validated by `JwtWebSocketHandshakeInterceptor` before handshake completes |
| Registration | `REGISTRATION_ENABLED` flag — disable after first admin is created |

---

## Technology choices at a glance

| Component | Choice | Reason |
| --- | --- | --- |
| API language | Java 26 | Records, pattern matching, virtual threads, strong JVM ecosystem |
| API framework | Spring Boot 4 / Spring Framework 7 | Jakarta EE 11, first-class WebSocket + RabbitMQ integration |
| Execution isolation | Docker sibling containers | Real per-language toolchains; no shared JVM/interpreter state |
| Runner job protocol | stdin/stdout JSON (daemon) | Minimal overhead, language-agnostic, easy to test in isolation |
| Async submission | RabbitMQ | Decouples HTTP from 30–60 s execution; survives API restarts mid-run |
| Real-time updates | SSE | Simpler than WebSocket for server-push; firewall-friendly |
| Database | PostgreSQL + Flyway | Schema migrations on startup; typed JSON for runner result payloads |
| Editor | Monaco | First-class LSP client, VS Code familiarity for developers |
| IntelliSense transport | WebSocket (LSP wire protocol) | Monaco expects a stream; Docker exec provides stdio; API proxies between them |
| LSP containers | Per-user pool + docker exec | Avoids container-per-tab sprawl; warm language server reused across tabs |
| Frontend state | TanStack Query + React 19 | Server state caching; streaming SSE via native `EventSource` |
| UI components | shadcn/ui (workspace) + Ant Design (chrome) | shadcn for composable workspace primitives; Ant Design for admin/list pages |

---

## What makes this technically interesting

**The LSP pooling problem** is the most novel design challenge. The Language Server Protocol is designed for a single long-running process per project. Spawning one per browser tab violates that assumption, wastes memory, and incurs cold-start penalties on every tab open. The `docker exec` bridge model treats the container as a warm process pool and the WebSocket connection as an ephemeral client attachment — closer to how desktop IDEs actually work.

**The runner daemon pattern** solves a different version of the same problem: keeping toolchain state (JVM, Maven dependency resolution, incremental compilation cache) hot between submissions without trusting user code to persist across a shared process. Each language container runs a tiny Python dispatcher that is the only long-lived process; the actual test execution runs as a subprocess with a wall-clock timeout and is discarded after completion.

**The challenge on-disk format** deliberately separates public and hidden test trees. The same Git repository that stores challenge content also acts as the runtime test corpus — no separate database table for test source. `ChallengeGitLoader` scans the `challenges/` tree on startup and upserts metadata. Adding a challenge is a Git operation, not a database migration.

**The warm-up stamp system** handles a subtle ops problem: after rebuilding runner images (`make runners`), the API needs to know which containers reflect the new image and which are stale. Comparing the stored image ID (written to `.ctl-runner-pool-warm-stamp` at warm time) against the current `docker image inspect` output gives a reliable staleness signal without querying any external registry.

---

## Running it yourself

```bash
git clone <repo>
cp .env.example .env
# edit: PG_PASSWORD, RMQ_PASSWORD, JWT_SECRET (≥ 32 chars)

make up         # full stack in Docker — no JDK or Node needed on the host
```

Then open http://localhost:3000, register the first user (becomes admin automatically), and go to **Ops → Warm everything**.

For local development with hot reload:

```bash
make infra      # postgres + rabbitmq only
make runners    # build execution + LSP images (one-time)
./gradlew :be:bootRun &
cd fe && npm run dev    # http://localhost:5173
```

Full setup guide: [user-guide.md](./user-guide.md)  
Backend architecture: [../be/ARCHITECTURE.md](../be/ARCHITECTURE.md)  
Runner operations: [runner-ops.md](./runner-ops.md)
