# Documentation

| Doc | Purpose |
| --- | --- |
| [technical-article.md](./technical-article.md) | **How it was built** — architecture, design decisions, LSP pooling, runner daemon, submission pipeline |
| [user-guide.md](./user-guide.md) | **How to use the app** — setup flow, Run/Submit, admin Ops, troubleshooting |
| [runner-ops.md](./runner-ops.md) | **Run tests** pooling, Maven/LSP warm, Admin Ops, env vars |
| [code_training_lab_mvp_specification.md](./code_training_lab_mvp_specification.md) | Product scope, roadmap, architecture |
| [contracts.md](./contracts.md) | Runner, submission, SSE, challenge on-disk contract |
| [adding-challenges.md](./adding-challenges.md) | **How to add challenges** (seed, admin, manual, future) |
| [challenge-catalog-backlog.md](./challenge-catalog-backlog.md) | Catalog counts, recent additions, backlog |
| [challenge-catalog-sources.md](./challenge-catalog-sources.md) | Inspiration repos per language |
| [frontend.md](./frontend.md) | React workspace UI, shadcn, routes, skills |
| [coolify.md](./coolify.md) | Self-hosted deploy (Coolify / VPS) |
| [cursor/README.md](./cursor/README.md) | Optional Cursor rules/skills vendors |
| [../be/ARCHITECTURE.md](../be/ARCHITECTURE.md) | Backend package layout |
| [../README.md](../README.md) | Run locally, Docker, CI |
| [../AGENTS.md](../AGENTS.md) | Agent entry point |

## Quick links

| I want to… | Start here |
| --- | --- |
| Use the app (sign-up, challenges, Run/Submit, Ops) | [user-guide.md](./user-guide.md) |
| Run the app locally (prerequisites) | [../README.md#prerequisites](../README.md#prerequisites) |
| Run the app locally (steps) | [../README.md](../README.md) |
| Add a new code challenge | [adding-challenges.md](./adding-challenges.md) |
| Bulk-seed from catalog | [adding-challenges.md#method-1--seed-catalog-bulk](./adding-challenges.md) + `scripts/seed-challenges/` |
| Create one challenge (admin) | [adding-challenges.md#method-2--admin-ui](./adding-challenges.md) |
| Change the workspace UI | [frontend.md](./frontend.md) |
| Warm runners / fix slow tests | [runner-ops.md](./runner-ops.md) |
| Understand runner JSON | [contracts.md](./contracts.md) |
