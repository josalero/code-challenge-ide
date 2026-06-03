# Code Training Lab

Multi-language code challenge platform.

**Docs:** [docs/README.md](docs/README.md) · **User guide:** [docs/user-guide.md](docs/user-guide.md) · **Backend:** [be/ARCHITECTURE.md](be/ARCHITECTURE.md) · **Agents:** [AGENTS.md](AGENTS.md)

## Prerequisites

Pick **one** workflow below. You do not need every tool on every path.

### Option A — Full stack in Docker (simplest)

Run Postgres, RabbitMQ, API, frontend, and runner orchestration without installing JDK or Node on the host.

| Requirement | Version / notes |
| --- | --- |
| **Docker Engine** | 24+ with **Compose v2** (`docker compose`, not legacy `docker-compose`) |
| **Disk space** | ~15–25 GB free for images (DB, API, FE, 11 runner images, 7 LSP images, Maven cache volume) |
| **Git** | Clone this repo |
| **Make** | Optional but recommended (`make up`, `make runners`, …) |

**Linux:** set `DOCKER_GID` in `.env` to the group that owns `/var/run/docker.sock` (see [.env.example](.env.example)). **macOS** (Docker Desktop): `DOCKER_GID=0` is usually fine.

**Not required on the host:** JDK, Node.js, Gradle, Python.

---

### Option B — Local development (API + UI on host, infra in Docker)

Best for backend/frontend iteration with fast reload. **Run** and **Submit** in the workspace need **runner images** built on the host (`make runners`).

| Requirement | Version / notes |
| --- | --- |
| **JDK** | **26** (matches `be/build.gradle` toolchain). [Eclipse Temurin](https://adoptium.net/) recommended. Verify: `java -version` |
| **Gradle** | **Not installed separately** — repo includes `./gradlew` (Gradle **9.4.1** wrapper) |
| **Node.js** | **20 LTS or 22 LTS** (npm included). Verify: `node -v` / `npm -v` |
| **Docker Engine** | 24+ with Compose v2 — Postgres, RabbitMQ, and **all language runners** |
| **Git** | Clone this repo |
| **Make** | Optional (`make infra`, `make runners`, …) |

**Optional but useful:**

| Tool | Purpose |
| --- | --- |
| **jenv** / **SDKMAN** | Pin JDK 26 per shell (`jenv shell 26`) |
| **Python 3.10+** | Challenge catalog seed scripts only (`scripts/seed-challenges/`) |
| **AI provider key** | OpenRouter or Ollama for coach feedback — see `.env` / `be/src/main/resources/application.yml` |

**Linux:** same `DOCKER_GID` note as Option A if the API cannot access `docker.sock`.

**First-time setup checklist (Option B):**

```bash
java -version          # openjdk 26 …
node -v && npm -v      # v20+ or v22+
docker compose version # Compose v2.x
cp .env.example .env   # set PG_PASSWORD, RMQ_PASSWORD, JWT_SECRET (≥32 chars)
```

---

### Shared configuration

Copy [.env.example](.env.example) to `.env` before `make up`, `make infra`, or host `bootRun`.

| Variable | Required | Purpose |
| --- | --- | --- |
| `PG_PASSWORD` | Yes | PostgreSQL |
| `RMQ_PASSWORD` | Yes | RabbitMQ |
| `JWT_SECRET` | Yes | API auth (min 32 characters) |
| `DOCKER_GID` | Linux | API spawns runner/LSP containers |
| `OPENROUTER_API_KEY` | Optional | AI coach (or configure Ollama in `.env`) |

Flyway migrations run on API startup (including warm-state tables). Reset local DB: `make down-infra` or `make reset`.

---

## Using the platform

Sign-up, challenge workspace (**Run** / **Submit**), admin **Ops** warm-up, and troubleshooting are documented in **[docs/user-guide.md](docs/user-guide.md)**.

---

## Make commands

The [Makefile](Makefile) wraps common Docker and runner tasks. There is **no single `make` target** for host-based local dev (infra in Docker, API + UI on the host) — run those steps below. For a **one-command full stack in Docker**, use `make up`.

| Command | What it does |
| --- | --- |
| **`make up`** | Build runner/LSP images, then build and start the **full stack** in Docker (Postgres, RabbitMQ, API, frontend). API startup waits for Maven cache warm via compose. UI at http://localhost:3000, API at http://localhost:8080. Requires `.env`. |
| **`make infra`** | Start **local infra only**: Postgres + RabbitMQ (`docker-compose.local.yml`, `--remove-orphans`). |
| **`make runners`** | **Build** all runner + LSP Docker images only (no warm). Required before **Run tests** / IntelliSense. Re-run after Dockerfile or `run.py` changes. |
| **`make lsp-warm`** | Warm LSP images (`initialize` handshake per unique image). Optional; also available in Admin Ops. |
| **`make lsp-warm-force`** | Re-run LSP warm even if images are unchanged. |
| **`make down`** | Stop the full Docker stack (`docker compose down`). |
| **`make down-infra`** | Stop local infra and **remove volumes** (wipes DB). |
| **`make reset-infra`** | Alias for `make down-infra`. |
| **`make reset`** | Stop full Docker stack and remove volumes (wipes DB). |
| **`make logs`** | Follow logs for the full stack. |
| **`make ps`** | List running compose services. |
| **`make config`** | Print resolved compose config. |
| **`make cursor-vendors`** | Install Cursor vendor assets (`scripts/install-cursor-vendors.sh`). |

**Admin Ops:** warm runners and LSP from the UI — [docs/user-guide.md#administrator-flows](docs/user-guide.md#administrator-flows). Backend details: [docs/runner-ops.md](docs/runner-ops.md).

---

## Local development (infra in Docker, API + UI on host)

Uses **Option B** prerequisites above. Postgres and RabbitMQ run in Docker; API and UI run on the host:

```bash
cp .env.example .env
make infra       # postgres + rabbitmq (same as: docker compose -f docker-compose.local.yml up -d)
make runners     # build runner + LSP images (warm separately: Admin Ops or make lsp-warm)

npm install                    # repo root — installs Husky pre-commit hooks (once per clone)
cd fe && npm ci                # frontend dependencies

# Java 26 required on PATH (or JAVA_HOME). If you use jenv, optionally: jenv shell 26
./gradlew :be:bootRun          # http://localhost:8080

cd fe && npm run dev           # http://localhost:5173 (proxies /api → 8080)
```

`docker-compose.local.yml` runs **postgres** and **rabbitmq** only — not `api`, `fe`, or runners.

**Run** (practice) and **Submit** (final, locks editing) both execute in **pooled runner containers** — see [docs/runner-ops.md](docs/runner-ops.md). Configure with `RUNNER_POOL_ENABLED` (default `true`) and `RUNNER_MAVEN_CACHE_VOLUME=ctl-runner-m2-cache` in `.env`. Rebuild after runner changes: `make runners`.

**IntelliSense** (Monaco + LSP for all 11 languages) uses the same Docker host: `make runners` builds LSP images; run `make lsp-warm` (or Admin Ops) to warm them. Details: [docs/frontend.md](docs/frontend.md#intellisense-lsp).

Stop infra (wipes DB): `make down-infra`

---

## Run the full stack (Docker only)

Uses **Option A** prerequisites — Docker (+ Compose v2) and `.env` only; no host JDK, Node, or Gradle.

```bash
cp .env.example .env
# Edit .env: PG_PASSWORD, RMQ_PASSWORD, JWT_SECRET (min 32 chars)
# Linux: set DOCKER_GID=$(stat -c '%g' /var/run/docker.sock) so the API can spawn runners

make up
# or: ./scripts/compose-up.sh   (runs make runners, then docker compose up --build -d)
# or: docker compose up --build -d
```

| URL | Service |
| --- | --- |
| http://localhost:3000 | Frontend (nginx → API proxy at `/api/`) |
| http://localhost:8080 | API (direct) |
| http://localhost:15672 | RabbitMQ management |

Stop: `make down` · Reset DB: `make reset`

Compose runs Postgres, RabbitMQ, API (spawns runners and LSP containers via `docker.sock`), and the frontend. Runner and LSP images are built with `make runners` (or `./scripts/compose-up.sh`, which runs it before `up`), not as long-lived services.

For IntelliSense in the full stack, run `make runners` once on the host before opening challenges (same as local dev).

### Production images (GHCR)

[`.github/workflows/build.yml`](.github/workflows/build.yml) publishes **`be`**, **`fe`**, all **11 language runners**, and **`lsp-java`** to GHCR. The other six LSP images (`lsp-python`, `lsp-go`, `lsp-typescript`, `lsp-dotnet`, `lsp-rust`, `lsp-cpp`) are built locally today — run `./scripts/build-lsp-images.sh` or `make runners` on the deployment host until they are added to the workflow.

```bash
cp .env.example .env
# Set CTL_IMAGE_OWNER, PG_PASSWORD, RMQ_PASSWORD, JWT_SECRET
docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
./scripts/pull-runner-images.sh   # 11 language runners + lsp-java from GHCR
make runners                      # or ./scripts/build-lsp-images.sh — non-Java LSP images
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

Map pulled GHCR tags in `.env` (`LSP_*_IMAGE`, `RUNNER_*_IMAGE`) or rely on `:local` tags from `make runners`. Verify execution runners: `./scripts/smoke-runners.sh`.

## Deploy (Coolify / VPS)

Use **`docker-compose.coolify.yml`** in Coolify (one Compose file) and **`./scripts/coolify-post-deploy.sh`** after each deploy.

See **[docs/coolify.md](docs/coolify.md)** for UI fields, environment variables, domains, volumes, and troubleshooting.

## Repository layout

| Folder | Stack |
| --- | --- |
| **`be/`** | Java 26, Spring Boot 4, PostgreSQL, RabbitMQ |
| **`fe/`** | React 19, Vite, TypeScript, Monaco, shadcn/ui, Ant Design, Tailwind — [docs/frontend.md](docs/frontend.md) |
| **`runners/java/`** | Java Maven/JUnit/JaCoCo sandbox (25/26) |
| **`runners/python/`** | Python 3.12 pytest/coverage/ruff sandbox |
| **`runners/go/`** | Go 1.23 `go test` + vet |
| **`runners/node/`** | Node 22 `node:test` + ESLint + c8 |
| **`runners/dotnet/`** | .NET 8 xUnit + coverlet + `dotnet format` |
| **`runners/typescript/`** | TypeScript 5.7 + tsx + `node:test` + c8 |
| **`runners/rust/`** | Rust 1.84 `cargo test` + clippy + llvm-cov |
| **`runners/cpp/`** | C++20 CMake + Catch2 + cppcheck + lcov |
| **`runners/react/`** | React 19 + Vitest + Testing Library |
| **`runners/vue/`** | Vue 3.5 + Vitest + Vue Test Utils |
| **`runners/angular/`** | Angular 19 pipes/services + Vitest |
| **`runners/lsp-java/`** | JDT LS (Java IntelliSense) |
| **`runners/lsp-python/`** | Pyright |
| **`runners/lsp-go/`** | gopls |
| **`runners/lsp-typescript/`** | typescript-language-server (+ Vue language server for Vue challenges) |
| **`runners/lsp-dotnet/`** | csharp-ls |
| **`runners/lsp-rust/`** | rust-analyzer |
| **`runners/lsp-cpp/`** | clangd |

```text
code-challenge-ide/
├── docker-compose.yml          # default: build & run everything locally
├── docker-compose.runners.yml  # runner + LSP image builds (make runners)
├── docker-compose.prod.yml     # override: pull be/fe from GHCR
├── docker-compose.coolify.yml  # Coolify entry (includes stack + GHCR + internal DB ports)
├── .env.example
├── be/
├── fe/
├── runners/                    # execution sandboxes + lsp-* language servers
└── challenges/
```

### Challenges

Built-in exercises live under `challenges/` and load on API startup (`ChallengeGitLoader`).

| How to add | Doc |
| --- | --- |
| Bulk seed from Python catalog | [docs/adding-challenges.md#method-1--seed-catalog-bulk](docs/adding-challenges.md) |
| Admin UI (`/challenges/new`) | [docs/adding-challenges.md#method-2--admin-ui](docs/adding-challenges.md) |
| Manual `challenges/{slug}/` tree | [docs/adding-challenges.md#method-3--manual-git-tree](docs/adding-challenges.md) |
| Future (multi-file, type-only, imports) | [docs/adding-challenges.md#future-authoring](docs/adding-challenges.md) |

Quick seed (all 11 languages):

```bash
python3 scripts/seed-challenges/generate.py          # skip existing slugs
python3 scripts/seed-challenges/generate.py --force  # overwrite on-disk trees
```

Catalog counts and backlog: [docs/challenge-catalog-backlog.md](docs/challenge-catalog-backlog.md).

## Git hooks (Husky)

After `npm install` at the **repo root**, every `git commit` runs [lint-staged](https://github.com/lint-staged/lint-staged) via Husky:

| Staged files | Check |
| --- | --- |
| `fe/**/*.{ts,tsx}` | `eslint --fix` (in `fe/`) |
| `be/**/*.java` | `./gradlew :be:compileJava` |

Install frontend deps (`cd fe && npm ci`) before the first FE commit so ESLint is available. Skip hooks once with `git commit --no-verify` if needed.

## CI / quality gates (optional on host)

Contributors run tests on the host with **JDK 26** + Node (`java -version` should report 26). If you use jenv, optionally run `jenv shell 26` first.

```bash
./gradlew check
cd fe && npm run lint && npm run build
```

See [`.github/workflows/ci.yml`](.github/workflows/ci.yml) — `backend-quality`, `frontend-quality`, and `backend-docker-e2e` on pull requests.

Optional local Docker E2E (requires Docker, runner image from `make runners`):

```bash
docker build -f runners/java/Dockerfile --build-arg JAVA_MAJOR=26 \
  -t code-challenge-ide-runner-java-26:local runners/java
CTL_INTEGRATION_DOCKER=true ./gradlew :be:test \
  --tests com.codetraininglab.submission.application.SubmissionFlowDockerIntegrationTest
```

Coverage thresholds: [`config/quality-gates.properties`](config/quality-gates.properties).
