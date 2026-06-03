# Deploy on Coolify

Code Training Lab is a self-hosted stack with a React frontend, Spring Boot API,
Postgres, RabbitMQ, and Docker runner/LSP images used by the API for submissions
and editor assistance.

Use [`docker-compose.coolify.yml`](../docker-compose.coolify.yml) as the single
Compose file in Coolify. It is intentionally self-contained and mirrors the local
stack made from `docker-compose.yml` plus `docker-compose.runners.yml`, without
Compose `include`, runner profiles, or extra deploy commands.

## Deployment Model

Default Coolify path:

- Coolify builds `code-lab-api`, `code-lab-fe`, every `runner-*` image, every
  `runner-lsp-*` image, and the Maven cache warm service from the same Compose
  file.
- Runner and LSP services are one-shot build services. They run `/bin/true` and
  exit with status 0 after the image exists. That is expected.
- `code-lab-api` waits for Postgres, RabbitMQ, runner images, LSP images, and
  `runner-m2-warm` before starting.
- No extra Coolify deploy command is required.
- First deploys can take a long time on small VPS hosts because many base images
  and language dependencies are downloaded.

Long-running services after deploy:

| Service | Purpose | Expected state |
| --- | --- | --- |
| `code-lab-postgres` | Database | Up / healthy |
| `code-lab-rabbitmq` | Submission queue | Up / healthy |
| `code-lab-api` | Backend and runner orchestrator | Up / healthy |
| `code-lab-fe` | Public nginx frontend | Up / healthy |
| `runner-*`, `runner-lsp-*`, `runner-m2-warm` | Build-only images/cache | Exited 0 |

## Prerequisites

| Requirement | Notes |
| --- | --- |
| Coolify on a Linux VPS with Docker | Same host runs Coolify and submission runners |
| Docker socket | API container must mount `/var/run/docker.sock` |
| Disk | Plan for 25-40 GB minimum; more is better on first build |
| Git repo root | Coolify base directory must be `/` so `challenges/`, `runners/`, `be/`, and `fe/` are available |
| Network access | First build pulls Docker Hub, Microsoft, Maven, npm, Cargo, and OS package assets |

## Coolify Setup

### 1. Create Docker Compose resource

| Field | Value |
| --- | --- |
| Type | Docker Compose |
| Repository | This project |
| Base directory | `/` |
| Docker Compose file | `docker-compose.coolify.yml` |
| Build | Enabled |

### 2. Domain and routing

Expose only the frontend service:

| Field | Value |
| --- | --- |
| Service | `code-lab-fe` |
| Container port | `80` |
| HTTPS | Enable in Coolify |

Do not expose the API separately. The frontend nginx config proxies `/api/` to
`code-lab-api:8080` on the internal Compose network.

Make sure Coolify's proxy supports long-lived HTTP connections and WebSocket
upgrade for `/api/`, because submissions and LSP features use streaming and
WebSockets.

### 3. Environment variables

Start from [`.env.coolify.example`](../.env.coolify.example):

```bash
cp .env.coolify.example .env
```

Set these in Coolify Environment Variables:

| Variable | Example | Purpose |
| --- | --- | --- |
| `PG_PASSWORD` | `change-me` | Postgres password |
| `RMQ_PASSWORD` | `change-me` | RabbitMQ password |
| `JWT_SECRET` | `at-least-32-random-characters` | JWT signing key |
| `CORS_ALLOWED_ORIGINS` | `https://lab.example.com` | Exact Coolify URL, no trailing slash |
| `DOCKER_GID` | `998` | Group id of `/var/run/docker.sock` on the VPS |

Recommended production values:

| Variable | Recommended | Notes |
| --- | --- | --- |
| `REGISTRATION_ENABLED` | `false` | Keep public sign-up closed unless needed |
| `RUNNER_POOL_ENABLED` | `true` | Required for stable Docker-out-of-Docker execution |
| `RUNNER_POOL_WARM_ON_STARTUP` | `false` | Use Ops -> Warm everything after first deploy |
| `CTL_LSP_ENABLED` | `true` | Enables editor language servers |

The Coolify env file includes local image names for every runner and LSP image,
including Rust and SQL:

- `RUNNER_RUST_184_IMAGE=code-challenge-ide-runner-rust-184:local`
- `RUNNER_POSTGRES_17_IMAGE=code-challenge-ide-runner-postgres-17:local`
- `LSP_RUST_IMAGE=code-challenge-ide-lsp-rust:local`

These names match the image inventory used by the backend.

## Docker Socket

The API starts runner and LSP containers through the host Docker socket. Without
socket access, Run tests, warm-up, and LSP features will fail.

On the VPS:

```bash
stat -c '%g' /var/run/docker.sock
```

Set `DOCKER_GID` to that value in Coolify, then redeploy.

The API service must keep:

```yaml
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
group_add:
  - "${DOCKER_GID}"
```

Verify from the API container:

```bash
docker ps --filter name=code-lab-api --format '{{.Names}}'
docker exec -it <api-container-name> docker info
```

`docker info` must exit 0. If it says permission denied, `DOCKER_GID` is wrong or
Coolify stripped the socket mount.

## Manual VPS Deploy

For a direct SSH deploy using the same Coolify compose:

```bash
git clone <your-repo-url> code-challenge-ide
cd code-challenge-ide
cp .env.coolify.example .env
# edit .env: secrets, CORS_ALLOWED_ORIGINS, DOCKER_GID

docker compose -f docker-compose.coolify.yml config --quiet
docker compose -f docker-compose.coolify.yml build
docker compose -f docker-compose.coolify.yml up -d
```

No extra deploy command is needed.

If you run this outside Coolify and the external network does not exist:

```bash
docker network create coolify
```

## First Login Checklist

1. Open `https://<your-domain>/`.
2. Sign in or register if `REGISTRATION_ENABLED=true`.
3. Open Admin -> Ops -> Warm everything.
4. Open a Java, Rust, or SQL challenge and run tests.
5. Check `GET https://<your-domain>/api/v1/health`.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Build fails at `docker/dockerfile:1` | Use current runner Java Dockerfile without the BuildKit syntax directive |
| 504 during deploy | First deploy may still be building all runner/LSP images; check Coolify build logs |
| 504 after deploy | Coolify must expose `code-lab-fe` on container port `80` |
| 504 when clicking Warm everything | Redeploy the API; warm endpoints enqueue quickly and Docker checks run inside the background job. If it still happens, Docker on the VPS is overloaded or unreachable |
| API unhealthy | Check Postgres/RabbitMQ credentials, `JWT_SECRET`, and readiness logs |
| `Docker is not reachable` | Check socket mount and `DOCKER_GID` |
| Ops shows Rust or SQL image missing | Confirm `runner-rust-184` and `runner-postgres-17` completed with exit 0 and `docker images` shows the `:local` tags |
| Run tests fails immediately | Check Docker socket access and runner image inventory |
| SQL challenges fail | Confirm `RUNNER_POSTGRES_17_IMAGE` points to `code-challenge-ide-runner-postgres-17:local` |
| IntelliSense dead | Confirm LSP images were built and `CTL_LSP_ENABLED=true` |
| CORS errors | `CORS_ALLOWED_ORIGINS` must match the exact public URL |
| `network coolify not found` | Let Coolify create it, or create it manually on SSH |

Useful VPS checks:

```bash
docker compose -f docker-compose.coolify.yml ps
docker compose -f docker-compose.coolify.yml logs --tail=100 code-lab-api
docker compose -f docker-compose.coolify.yml logs --tail=100 code-lab-fe
docker images | grep code-challenge-ide
```

## Optional Prebuilt Images

The default path builds everything on the VPS because that is the most reliable
script-free Coolify setup. If the VPS is too small or Docker Hub pulls are
unreliable, publish API, FE, runner, and LSP images from CI and use a Coolify
compose override or edited deploy compose that removes `build:` from those
services and points `image:` to the published registry tags.
