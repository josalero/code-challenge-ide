# Runner operations

How challenge **Run tests** execution works on the API host, and how it differs from **LSP IntelliSense** warm-up.

When this doc disagrees with code, trust **`DockerRunnerClient`**, **`RunnerContainerPool`**, and **`RunnerOpsService`**.

---

## Two different “warm” concepts

| | **Submission runners** | **LSP (editor)** |
| --- | --- | --- |
| Purpose | Compile & run tests | IntelliSense in Monaco |
| Trigger | User clicks **Run tests** | Opening a challenge workspace |
| Containers | `ctl-runner-pool-{image}` (pooled) or one-shot `docker run --rm` | `code-challenge-ide-lsp-*` (per WebSocket session) |
| Warm in Admin Ops | **Maven cache** volume + per-row **Warm** on runner/LSP tables | Same Ops page — LSP **Warm** column |

**Built** = Docker image exists locally. **Warm** = preloaded for faster cold start (Maven volume or LSP initialize stamp). Warm does **not** skip compile/test on each run.

---

## Submission execution (default: pooled)

When `RUNNER_POOL_ENABLED=true` (default):

1. API uses **one long-lived container per runner image** (`ctl-runner-pool-*`), started on first use.
2. A **daemon** inside the container (`runners/java/daemon.py` for Java) reads JSON jobs on stdin and returns one JSON result line.
3. Challenge files are synced into `/challenge` only when the slug/path changes (skip on repeat runs).
4. **Java (pooled):** incremental workspace — same challenge keeps `target/` for faster Maven rebuilds.
5. Idle pool containers are removed after `RUNNER_POOL_IDLE_MINUTES` (default 60).

Set `RUNNER_POOL_ENABLED=false` to fall back to one-shot `docker run --rm` per submission.

### Expected timings (Java, after `make runners`)

| Scenario | Typical wall time |
| --- | --- |
| First run (pool container start + full compile) | ~15–25s |
| Repeat run (same challenge, edit solution) | ~5–10s |
| LSP only | Does not affect run time |

### Cleanup

```bash
# Pooled runner containers (API-managed)
docker rm -f $(docker ps -aq --filter label=ctl.runner-pool=true) 2>/dev/null

# Orphan compose build containers after make runners
docker compose -f docker-compose.local.yml up -d --remove-orphans   # make infra
```

Rebuild images after changing `runners/*/run.py` or Dockerfiles:

```bash
make runners
# restart API (bootRun or api container)
```

---

## Maven cache volume

Shared volume `ctl-runner-m2-cache` (see `RUNNER_MAVEN_CACHE_VOLUME`) is mounted into Java pool containers and one-shot runs. Populated by:

- Admin Ops → **Warm Maven cache**, or
- Full stack `make up` (compose `runner-m2-warm` runs before API), or
- `docker compose -f docker-compose.local.yml -f docker-compose.runners.yml run --rm runner-m2-warm`

Without it, each Java container copies from `/opt/m2` baked in the image (slower first run).

---

## Admin Ops (`/admin/ops`)

Admins only. Requires Docker CLI on the API host.

| Section | Meaning |
| --- | --- |
| Runner / LSP tables — **Built** | Image present locally |
| Runner / LSP tables — **Warm** | Maven cache warm (Java runners) or LSP stamp (LSP rows) |
| **Warm Maven cache** | Copy `/opt/m2` → volume |
| **Warm LSP** | Run `scripts/lsp_warm.py` handshake per image |

Backend: `RunnerOpsService`, `OpsController`.

---

## Environment variables (`.env`)

| Variable | Default | Purpose |
| --- | --- | --- |
| `RUNNER_MAVEN_CACHE_VOLUME` | `ctl-runner-m2-cache` | Java `.m2` mount |
| `RUNNER_POOL_ENABLED` | `true` | Pooled vs one-shot runners |
| `RUNNER_POOL_IDLE_MINUTES` | `60` | Evict idle pool containers |
| `CTL_DOCKER_ENABLED` | `true` | Disable for stub runner in tests |
| `RUNNER_JAVA_26_IMAGE`, … | see `.env.example` | Image tags per language |

`CTL_RUNNER_POOLED=1` is set **inside** pool containers by the API — do not add it to `.env`.

---

## Runner job contract (stdin)

See [contracts.md](./contracts.md). Job JSON includes `challenge_slug` for incremental workspace in pooled mode.

Implementation: `RunnerJobPayload`, language `run.py` / `daemon.py` under `runners/`.
