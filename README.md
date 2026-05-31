# Code Training Lab

Multi-language code challenge platform.

**Docs:** [docs/README.md](docs/README.md) · **Backend:** [be/ARCHITECTURE.md](be/ARCHITECTURE.md) · **Agents:** [AGENTS.md](AGENTS.md)

## Local development (infra in Docker, API + UI on host)

JDK **26**, **Node**, and **Gradle** on the host; Postgres and RabbitMQ in Docker:

```bash
cp .env.example .env
docker compose -f docker-compose.local.yml up -d
make runners   # build runner images + warm Maven cache volume (once, or after Dockerfile changes)

npm install                    # repo root — installs Husky pre-commit hooks (once per clone)
cd fe && npm ci                # frontend dependencies

# Java 26 required on PATH (or JAVA_HOME). If you use jenv, optionally: jenv shell 26
./gradlew :be:bootRun          # http://localhost:8080

cd fe && npm run dev           # http://localhost:5173 (proxies /api → 8080)
```

`docker-compose.local.yml` runs **postgres** and **rabbitmq** only — not `api`, `fe`, or runners.

When you click **Run tests**, the API runs `docker run --rm` with the image for the challenge language and selected runtime (Java 17/21/25/26, Python 3.12). Runner images must exist on your Docker host (`make runners`). A shared Maven volume (`ctl-runner-m2-cache`) speeds up Java repeat runs.

Stop infra: `docker compose -f docker-compose.local.yml down`  
Reset DB: `docker compose -f docker-compose.local.yml down -v`

---

## Run the full stack (Docker only)

**You only need Docker** on the host — no JDK, Node, or Gradle required to run the app.

```bash
cp .env.example .env
# Edit .env: PG_PASSWORD, RMQ_PASSWORD, JWT_SECRET (min 32 chars)
# Linux: set DOCKER_GID=$(stat -c '%g' /var/run/docker.sock) so the API can spawn runners

make up
# or: ./scripts/compose-up.sh
# or: docker compose up --build -d
```

| URL | Service |
| --- | --- |
| http://localhost:3000 | Frontend (nginx → API proxy at `/api/`) |
| http://localhost:8080 | API (direct) |
| http://localhost:15672 | RabbitMQ management |

Stop: `make down` · Reset DB: `make reset`

Compose runs Postgres, RabbitMQ, API (spawns runners via `docker.sock`), and the frontend. Runner images are built with `make runners`, not as long-lived services.

### Production images (GHCR)

After [`.github/workflows/build.yml`](.github/workflows/build.yml) publishes images:

```bash
cp .env.example .env
# Set CTL_IMAGE_OWNER, PG_PASSWORD, RMQ_PASSWORD, JWT_SECRET
docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
./scripts/pull-runner-images.sh
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## Deploy (Coolify / VPS)

See [docs/coolify.md](docs/coolify.md) for self-hosted deployment, runner image pulls, and the dead-letter ops endpoint.

## Repository layout

| Folder | Stack |
| --- | --- |
| **`be/`** | Java 26, Spring Boot 4, PostgreSQL, RabbitMQ |
| **`fe/`** | React, TypeScript, Monaco, Ant Design, Tailwind |
| **`runners/java/`** | Java Maven/JUnit/JaCoCo sandbox (17/21/25/26) |
| **`runners/python/`** | Python 3.12 pytest/coverage/ruff sandbox |

```text
code-challenge-ide/
├── docker-compose.yml      # default: build & run everything locally
├── docker-compose.prod.yml # override: pull from GHCR
├── .env.example
├── be/
├── fe/
├── runners/java/
└── challenges/
```

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
