# Deploy on Coolify

Code Training Lab is a self-hosted stack: API, UI, Postgres, RabbitMQ, and **on-demand** Java runner containers spawned by the API via Docker.

## Prerequisites

- Coolify server with Docker socket available to the API container (same pattern as local `docker-compose.yml`)
- GHCR images published from [`.github/workflows/build.yml`](../.github/workflows/build.yml), or build images on the server

## Services

| Service | Image | Notes |
| --- | --- | --- |
| API | `ghcr.io/<owner>/code-challenge-ide-be:latest` | Mount `docker.sock`, `challenges/` volume |
| Frontend | `ghcr.io/<owner>/code-challenge-ide-fe:latest` | Proxies `/api` to API |
| Postgres | `postgres:17` | Persistent volume |
| RabbitMQ | `rabbitmq:3-management` | Optional management UI |

Runner images (Java 25/26, Python 3.12, Go, Node, C#, TypeScript, Rust, C++, React, Vue, Angular, LSP) are **not** long-running services. Pull or build them on the host:

```bash
./scripts/pull-runner-images.sh
# or: make runners
# verify: ./scripts/smoke-runners.sh
```

## Environment (API)

Copy from [`.env.example`](../.env.example):

- `JWT_SECRET` (≥ 32 chars)
- `PG_*`, `RMQ_*`
- `CTL_CHALLENGES_PATH=/challenges`
- `CTL_DOCKER_ENABLED=true`
- `DOCKER_GID` (Linux: group id of `/var/run/docker.sock`)
- Runner image tags: `RUNNER_JAVA_*_IMAGE`, `RUNNER_PYTHON_312_IMAGE`, `RUNNER_GO_123_IMAGE`, `RUNNER_NODE_22_IMAGE`, `RUNNER_DOTNET_8_IMAGE`, `RUNNER_TYPESCRIPT_57_IMAGE`, `RUNNER_RUST_184_IMAGE`, `RUNNER_CPP_20_IMAGE`, `RUNNER_REACT_19_IMAGE`, `RUNNER_VUE_35_IMAGE`, `RUNNER_ANGULAR_19_IMAGE`, `LSP_*_IMAGE` (see `.env.example`)
- `CTL_LSP_ENABLED=true` when LSP sidecar image is available

## Compose reference

Production overlay:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

Map Coolify domains to the frontend service; ensure WebSocket upgrade works for `/api/v1/lsp/{language}` and submission SSE.

## Operations

- Health: `GET /api/v1/health` and `/actuator/health`
- Dead-letter queue (authenticated):
  - `GET /api/v1/ops/dead-letter-submissions?limit=20` — peek (non-destructive)
  - `POST /api/v1/ops/dead-letter-submissions/replay?limit=10` — requeue to `ctl.submissions`
