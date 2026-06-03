# Deploy on Coolify

Code Training Lab is a self-hosted stack: **frontend**, **API**, **Postgres**, **RabbitMQ**, plus **on-demand runner and LSP containers** started by the API through the host Docker socket.

Use **`docker-compose.coolify.yml`** as the single Compose file in Coolify. It defines **code-lab-postgres**, **code-lab-rabbitmq**, **code-lab-api**, and **code-lab-fe** (no `include` — compatible with Coolify’s Compose parser).

**Default: local Docker build** — `docker-compose.coolify.yml` defines `code-lab-api`, `code-lab-fe`, and all `runner-*` / `runner-lsp-*` inline. With Coolify **Build** enabled, every deploy builds runner/LSP images on the VPS (`*:local` tags). No separate post-deploy step required. No GHCR pull required.

Postgres and RabbitMQ use upstream images on the internal network only (no host ports).

## Prerequisites

| Requirement | Notes |
| --- | --- |
| **Coolify** on a Linux VPS with Docker | Same host runs Coolify and submission runners |
| **Docker socket** | API container must mount `/var/run/docker.sock` (already in compose) |
| **Disk** | ~25–40 GB for local builds + image layers + DB volume |
| **Git repo** | Coolify deploys from Bitbucket/GitHub; full repo root (`challenges/`, `runners/`, `be/`, `fe/`) |
| **Build time** | First deploy: API + FE + all runners/LSP in one Compose build — allow 30–90+ minutes |

## Coolify UI — step by step

### 1. Create a Docker Compose resource

| Field | Value |
| --- | --- |
| **Type** | Docker Compose |
| **Repository** | This project (branch `main` or your release branch) |
| **Base directory** | `/` (repository root) |
| **Docker Compose file** | `docker-compose.coolify.yml` |
| **Build** | **Enabled** — Compose builds `code-lab-api`, `code-lab-fe`, and runner/LSP images (same file) |

### 2. Domain and routing

| Field | Value |
| --- | --- |
| **Service to expose** | `code-lab-fe` |
| **Container port** | **`80`** (nginx inside the container — not `FE_PORT`) |
| **Host port (optional)** | `FE_PORT=3010` in env maps **`3010:80`** for `http://<vps-ip>:3010` — Coolify HTTPS domain still uses container **80** |
| **Port mappings** | Coolify attaches to the container port; `FE_PORT` host mapping in compose is optional |
| **HTTPS** | Enable in Coolify (Let’s Encrypt) |

The frontend proxies `/api/` to the internal `api` service. You do **not** need a separate public domain for the API.

**Proxy requirements:** allow long-lived connections and WebSocket upgrade on `/api/` (LSP and submission event streams).

### 3. Environment variables

Set these in Coolify **Environment Variables**, or on the server:

```bash
cp .env.coolify.example .env
# edit .env — secrets, CORS_ALLOWED_ORIGINS, DOCKER_GID
```

Template: [`.env.coolify.example`](../.env.coolify.example). General reference: [`.env.example`](../.env.example).

**Required**

| Variable | Example | Purpose |
| --- | --- | --- |
| `PG_PASSWORD` | strong secret | Postgres |
| `RMQ_PASSWORD` | strong secret | RabbitMQ |
| `JWT_SECRET` | ≥ 32 random chars | API auth |
| `CORS_ALLOWED_ORIGINS` | `https://lab.example.com` | Must match your Coolify URL |
| `DOCKER_GID` | `988` | Linux: `stat -c '%g' /var/run/docker.sock` |

**Strongly recommended**

| Variable | Example | Purpose |
| --- | --- | --- |
| `REGISTRATION_ENABLED` | `false` | Lock sign-up on public instances |
| `RUNNER_POOL_WARM_ON_STARTUP` | `false` | Set `true` after runners are built; or use Ops → Warm |
| `OPENROUTER_API_KEY` | (secret) | AI coach (optional) |

**Runner / LSP images (`:local`)**

Defaults in [`.env.coolify.example`](../.env.coolify.example) match Flyway (`code-challenge-ide-runner-*:local`). Defined inline in `docker-compose.coolify.yml` — no GHCR required.

Optional GHCR override: set `RUNNER_*_IMAGE` / `LSP_*_IMAGE` to `ghcr.io/...` only if you publish images via [`.github/workflows/build.yml`](../.github/workflows/build.yml) and pull them yourself.

### 4. Docker socket (required for `code-lab-api`)

The API runs `docker info` and spawns runner/LSP containers on the **host**. Without socket access you will see:

```text
Runner pool warm on startup could not start: … Docker is not reachable from the API container …
```

**Fix on the VPS**

1. SSH to the server and read the socket group id:

   ```bash
   stat -c '%g' /var/run/docker.sock
   ```

2. In Coolify **Environment Variables**, set `DOCKER_GID` to that number (not `0` unless it matches).

3. Confirm `docker-compose.coolify.yml` still mounts the socket on **`code-lab-api`**:

   ```yaml
   volumes:
     - /var/run/docker.sock:/var/run/docker.sock
   group_add:
     - "${DOCKER_GID}"
   ```

4. Redeploy the stack.

5. Verify inside the API container:

   ```bash
   docker ps --filter name=code-lab-api --format '{{.Names}}'
   docker exec -it <api-container-name> docker info
   ```

   `docker info` must exit 0. If you get “permission denied”, `DOCKER_GID` is wrong.

**Coolify UI:** Some versions require explicitly allowing the compose project to use the Docker socket (resource **Advanced** / **Docker** settings). If Coolify strips bind mounts, re-add the socket volume or deploy on the same host where Coolify manages Docker.

**Workaround until socket works:** set `RUNNER_POOL_WARM_ON_STARTUP=false`, redeploy, then use Admin → **Ops** → **Warm everything** after fixing `DOCKER_GID`.

### 5. Volumes and mounts (verify in Coolify)

All four services are declared in `docker-compose.coolify.yml`. Confirm Coolify does not strip mounts:

| Service | In Coolify stack | Mount / data |
| --- | --- | --- |
| `code-lab-postgres` | Yes | volume `ctl-postgres-data` |
| `code-lab-rabbitmq` | Yes | ephemeral (queue state in container) |
| `code-lab-api` | Yes | Built from `be/Dockerfile` (includes `challenges/`); `docker.sock`, `ctl-ops-data` |
| `code-lab-fe` | Yes (public URL) | nginx config inlined in compose (`configs`) → `code-lab-api:8080` |

**Base directory must be the git root** (API build context is `.`).

### 6. Post-deployment command (optional)

| Field | Value |
| --- | --- |
| **Post-deployment / Execute command** | *(leave empty)* or `./scripts/coolify-post-deploy.sh` |

Coolify **Build** already builds all `runner-*` / `runner-lsp-*` services in `docker-compose.coolify.yml` (no Compose profile). Use the script only for a **manual** rebuild on SSH, or to run [`smoke-runners.sh`](../scripts/smoke-runners.sh).

Runner compose services use `entrypoint: /bin/true` — they show as **Exited** after `up`; that is normal (images are for `docker run` from the API).

### 7. GHCR (optional)

The default Coolify path does **not** use GHCR. To use pre-built images instead, change `docker-compose.coolify.yml` back to `image: ghcr.io/...` and run [`pull-runner-images.sh`](../scripts/pull-runner-images.sh) + [`tag-runner-images-local.sh`](../scripts/tag-runner-images-local.sh).

## Manual deploy (SSH on the VPS)

```bash
git clone <your-repo-url> code-challenge-ide && cd code-challenge-ide
cp .env.example .env
# edit .env — set CTL_IMAGE_OWNER, secrets, CORS_ALLOWED_ORIGINS

docker compose -f docker-compose.coolify.yml build
docker compose -f docker-compose.coolify.yml up -d
# optional: ./scripts/coolify-post-deploy.sh  # rebuild runners only + smoke
```

Alternative (same stack, two-file override):

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
./scripts/pull-runner-images.sh
./scripts/build-lsp-images.sh   # if not using coolify-post-deploy.sh
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## Services

| Service | Image | Notes |
| --- | --- | --- |
| `code-lab-fe` | `code-challenge-ide-fe:local` (built `fe/Dockerfile`) | Public URL → port **80** |
| `code-lab-api` | `code-challenge-ide-be:local` (built `be/Dockerfile`) | Internal; Docker socket |
| `code-lab-postgres` | `postgres:17` | Not exposed on host |
| `code-lab-rabbitmq` | `rabbitmq:3-management-alpine` | Not exposed on host |

Runner/LSP images are **not** long-running Compose services; the API runs them with `docker run`.

## First login checklist

1. Open `https://<your-domain>/`
2. Sign in (or register if `REGISTRATION_ENABLED=true`)
3. Admin → **Ops** → **Warm everything** (if not auto-warmed)
4. Open a Java challenge → **Run tests** to confirm runners work
5. `GET https://<your-domain>/api/v1/health` or `/actuator/health/readiness`

## Operations

| Task | How |
| --- | --- |
| Health | `GET /api/v1/health`, `/actuator/health/readiness` |
| Warm state | Admin → **Infrastructure warm-up** (`/admin/ops`) |
| Dead-letter submissions | `GET /api/v1/ops/dead-letter-submissions?limit=20` |
| Replay dead-letter | `POST /api/v1/ops/dead-letter-submissions/replay?limit=10` |
| Logs | Coolify → service logs, or `docker compose -f docker-compose.coolify.yml logs -f api` |

## Troubleshooting

| Symptom | Check |
| --- | --- |
| `Docker CLI is not available` / `Docker is not reachable` on startup | `DOCKER_GID` = `stat -c '%g' /var/run/docker.sock`; socket volume on `code-lab-api`; `docker exec … docker info` |
| Run tests fails immediately | Same as above + runner images (`docker images \| grep code-challenge-ide`) |
| Ops shows runner images **missing** | DB expects `:local` tags; enable Coolify **Build**, redeploy, or run `./scripts/coolify-post-deploy.sh`. See **Runner images on Coolify** below |
| 401 / CORS in browser | `CORS_ALLOWED_ORIGINS` matches exact site URL (scheme + host) |
| Wrong redirects / links behind HTTPS | API uses `server.forward-headers-strategy: framework` in `application-production.yml` (same as TrailPulse behind Coolify) |
| Challenges missing / empty catalog | Do **not** bind-mount `./challenges` on Coolify (empty host dir overrides image). Use GHCR `be` image with baked `/challenges`. API logs: `ChallengeGitLoader` / `Seeded challenge`. Verify: `docker exec <api> ls /challenges \| head` |
| IntelliSense dead | LSP images built; `CTL_LSP_ENABLED=true`; WebSocket proxy |
| GHCR pull 401 | Registry credentials in Coolify |
| SQL challenges fail | `runner-postgres-17` built (`coolify-post-deploy.sh`) |
| `host not found in upstream "api"` (nginx) | Use current `docker-compose.coolify.yml` (inlined nginx `configs`, upstream `code-lab-api`) |
| `nginx.coolify.conf` mount: not a directory | Remove stray dir on VPS: `rm -rf fe/nginx.coolify.conf` if created by a failed mount; redeploy (compose no longer bind-mounts this file) |
| **504 / 503** on public URL | Coolify **container port = 80** on `code-lab-fe`; see checks below |
| Browser `content.js` / `first bit not a valid function name` | Chrome extension noise — not from this app; ignore or disable the extension |

### Runner images on Coolify

The API uses **`language_runtimes.docker_image`** from Postgres (`code-challenge-ide-*:local`). Coolify builds them when **Build** is enabled on the Compose resource (all `runner-*` / `runner-lsp-*` in `docker-compose.coolify.yml`).

Verify on the VPS:

```bash
docker images | grep code-challenge-ide
```

If images are missing after deploy, rebuild manually:

```bash
chmod +x scripts/*.sh
./scripts/coolify-post-deploy.sh
```

Then Admin → **Ops** → **Warm everything** (or `RUNNER_POOL_WARM_ON_STARTUP=true` once images exist).

### 504 / 503 gateway errors

Coolify returns **504** when nothing answers on **`code-lab-fe:80`** (container crash, never started, wrong port, or deploy still building).

**Common cause:** `code-lab-fe` waited for `code-lab-api` health while the API was still building (Gradle). Compose now starts FE when the API **container is up** (`service_started`), not when readiness passes. The login page should load; `/api/` may 502 until the API is healthy.

**Runners:** All `runner-*` / `runner-lsp-*` are in the same Compose file and built on deploy (Coolify **Build**). First deploy logs will show many image builds — allow extra time before the API is ready.

**Coolify domain settings**

| Field | Value |
| --- | --- |
| Service | `code-lab-fe` |
| Port | **80** |

**On the VPS (SSH)**

```bash
cd /data/coolify/applications/<your-app-id>
docker compose -f docker-compose.coolify.yml ps
docker compose -f docker-compose.coolify.yml logs --tail=80 code-lab-fe
docker compose -f docker-compose.coolify.yml logs --tail=80 code-lab-api
```

| `ps` shows | Meaning |
| --- | --- |
| `code-lab-fe` missing or Restarting | API not healthy yet, nginx config error, or image pull failed — read `code-lab-fe` logs |
| `code-lab-api` unhealthy | Fix Postgres/RabbitMQ env, `JWT_SECRET`, or wait through `start_period` (up to ~2 min) |
| Both **Up (healthy)** | Fix Coolify port (must be **80**) and redeploy proxy |
| Deploy logs show 20+ runner builds | Normal on first deploy (Coolify **Build**); long-running stack is postgres, rabbitmq, api, fe — runners exit after `/bin/true` |
| `network coolify not found` | Create external network or let Coolify provision it: `docker network create coolify` |

**Env**

```bash
CORS_ALLOWED_ORIGINS=https://code-challenge-lab.5.78.76.98.sslip.io
```

(match your real URL, no trailing slash)

## Related files

| File | Role |
| --- | --- |
| [`docker-compose.coolify.yml`](../docker-compose.coolify.yml) | Coolify stack: postgres, rabbitmq, api, fe (self-contained) |
| [`docker-compose.prod.yml`](../docker-compose.prod.yml) | GHCR override only (use with `docker-compose.yml`) |
| [`scripts/coolify-post-deploy.sh`](../scripts/coolify-post-deploy.sh) | Optional manual runner/LSP rebuild + smoke |
| [`.env.example`](../.env.example) | Full variable reference |
