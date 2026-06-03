# Deploy on Coolify

Code Training Lab is a self-hosted stack: **frontend**, **API**, **Postgres**, **RabbitMQ**, plus **on-demand runner and LSP containers** started by the API through the host Docker socket.

Use **`docker-compose.coolify.yml`** as the single Compose file in Coolify. It defines **code-lab-postgres**, **code-lab-rabbitmq**, **code-lab-api**, and **code-lab-fe** explicitly (no `include` or `!reset` — compatible with Coolify’s Compose parser). **API/FE** pull from GHCR; Postgres and RabbitMQ use upstream images on the internal network only (no host ports).

## Prerequisites

| Requirement | Notes |
| --- | --- |
| **Coolify** on a Linux VPS with Docker | Same host runs Coolify and submission runners |
| **Docker socket** | API container must mount `/var/run/docker.sock` (already in compose) |
| **Disk** | ~15–25 GB for images + DB volume |
| **GHCR images** | Published from [`.github/workflows/build.yml`](../.github/workflows/build.yml) under your GitHub user/org |
| **Git repo** | Coolify deploys from Bitbucket/GitHub; `challenges/` must be present in the checkout |

## Coolify UI — step by step

### 1. Create a Docker Compose resource

| Field | Value |
| --- | --- |
| **Type** | Docker Compose |
| **Repository** | This project (branch `main` or your release branch) |
| **Base directory** | `/` (repository root) |
| **Docker Compose file** | `docker-compose.coolify.yml` |
| **Build** | Disabled for app images (Compose pulls GHCR `be` / `fe`) |

### 2. Domain and routing

| Field | Value |
| --- | --- |
| **Service to expose** | `code-lab-fe` |
| **Container port** | `80` (nginx inside the FE container) |
| **HTTPS** | Enable in Coolify (Let’s Encrypt) |

The frontend proxies `/api/` to the internal `api` service. You do **not** need a separate public domain for the API.

**Proxy requirements:** allow long-lived connections and WebSocket upgrade on `/api/` (LSP and submission event streams).

### 3. Environment variables

Set these in Coolify **Environment Variables**, or on the server:

```bash
cp .env.coolify.example .env
# edit .env — secrets, CTL_IMAGE_OWNER, CORS_ALLOWED_ORIGINS
```

Template: [`.env.coolify.example`](../.env.coolify.example). General reference: [`.env.example`](../.env.example).

**Required**

| Variable | Example | Purpose |
| --- | --- | --- |
| `CTL_IMAGE_OWNER` | `your-github-user` | GHCR namespace (lowercase) |
| `PG_PASSWORD` | strong secret | Postgres |
| `RMQ_PASSWORD` | strong secret | RabbitMQ |
| `JWT_SECRET` | ≥ 32 random chars | API auth |
| `CORS_ALLOWED_ORIGINS` | `https://lab.example.com` | Must match your Coolify URL |

**Strongly recommended**

| Variable | Example | Purpose |
| --- | --- | --- |
| `CTL_IMAGE_TAG` | `latest` or `v1.0.0` | GHCR tag for `be` / `fe` / runners |
| `DOCKER_GID` | `999` | Linux: `stat -c '%g' /var/run/docker.sock` |
| `REGISTRATION_ENABLED` | `false` | Lock sign-up on public instances |
| `RUNNER_POOL_WARM_ON_STARTUP` | `true` | Pre-warm runners after boot |
| `OPENROUTER_API_KEY` | (secret) | AI coach (optional) |

**Runner image tags (GHCR)**

After `./scripts/coolify-post-deploy.sh`, point runtime images at GHCR (or keep `:local` for host-built SQL/LSP):

```bash
CTL_IMAGE_REGISTRY=ghcr.io
CTL_IMAGE_OWNER=your-github-user
CTL_IMAGE_TAG=latest

RUNNER_JAVA_26_IMAGE=ghcr.io/your-github-user/code-challenge-ide-runner-java-26:latest
RUNNER_PYTHON_312_IMAGE=ghcr.io/your-github-user/code-challenge-ide-runner-python-312:latest
# … see .env.example for all RUNNER_* and LSP_* variables
RUNNER_POSTGRES_17_IMAGE=code-challenge-ide-runner-postgres-17:local
```

SQL and six non-Java LSP images are **built on the server** by the post-deploy script until CI publishes them to GHCR.

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
| `code-lab-api` | Yes | `docker.sock`, `./challenges`, `ctl-ops-data`; DNS alias `api` for FE nginx |
| `code-lab-fe` | Yes (public URL) | nginx config inlined in compose (`configs`) → `code-lab-api:8080` |

**Base directory must be the git root** so `./challenges` resolves.

### 6. Post-deployment command

| Field | Value |
| --- | --- |
| **Post-deployment / Execute command** | `./scripts/coolify-post-deploy.sh` |

This script:

1. Pulls GHCR runner images (+ `lsp-java`) via [`pull-runner-images.sh`](../scripts/pull-runner-images.sh)
2. Builds **SQL** (`runner-postgres-17`) and **non-Java LSP** images on the host
3. Optionally runs [`smoke-runners.sh`](../scripts/smoke-runners.sh)

Run it again after every release that changes runner Dockerfiles.

### 7. GHCR authentication (private packages)

If images are private, add a **Docker Registry** in Coolify with a GitHub PAT (`read:packages`) and attach it to the resource before deploy.

## Manual deploy (SSH on the VPS)

```bash
git clone <your-repo-url> code-challenge-ide && cd code-challenge-ide
cp .env.example .env
# edit .env — set CTL_IMAGE_OWNER, secrets, CORS_ALLOWED_ORIGINS

docker compose -f docker-compose.coolify.yml pull
./scripts/coolify-post-deploy.sh
docker compose -f docker-compose.coolify.yml up -d
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
| `code-lab-fe` | `ghcr.io/<owner>/code-challenge-ide-fe:<tag>` | Public URL → this service, port **80** |
| `code-lab-api` | `ghcr.io/<owner>/code-challenge-ide-be:<tag>` | Internal; needs Docker socket + `challenges/` |
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
| 401 / CORS in browser | `CORS_ALLOWED_ORIGINS` matches exact site URL (scheme + host) |
| Challenges missing | `challenges/` mounted at `/challenges`; API logs for `ChallengeGitLoader` |
| IntelliSense dead | LSP images built; `CTL_LSP_ENABLED=true`; WebSocket proxy |
| GHCR pull 401 | Registry credentials in Coolify |
| SQL challenges fail | `runner-postgres-17` built (`coolify-post-deploy.sh`) |
| `host not found in upstream "api"` (nginx) | Use current `docker-compose.coolify.yml` (inlined nginx `configs`, upstream `code-lab-api`) |
| `nginx.coolify.conf` mount: not a directory | Remove stray dir on VPS: `rm -rf fe/nginx.coolify.conf` if created by a failed mount; redeploy (compose no longer bind-mounts this file) |

## Related files

| File | Role |
| --- | --- |
| [`docker-compose.coolify.yml`](../docker-compose.coolify.yml) | Coolify stack: postgres, rabbitmq, api, fe (self-contained) |
| [`docker-compose.prod.yml`](../docker-compose.prod.yml) | GHCR override only (use with `docker-compose.yml`) |
| [`scripts/coolify-post-deploy.sh`](../scripts/coolify-post-deploy.sh) | Post-deploy pulls + host builds |
| [`.env.example`](../.env.example) | Full variable reference |
