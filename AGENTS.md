# Agent Instructions — Code Training Lab

If this file disagrees with **build files or code**, trust the build files and code.

| Area | Stack |
| --- | --- |
| `be/` | Java **26**, Spring Boot **4** — [be/ARCHITECTURE.md](be/ARCHITECTURE.md) |
| `fe/` | React **19**, Vite, shadcn/ui, Ant Design (+ React 19 patch), Monaco, Tailwind — [docs/frontend.md](docs/frontend.md) |

## How to run

See [README.md](README.md) (local infra + host dev, full Docker stack, CI).

```bash
npm install          # Husky (repo root)
./gradlew :be:check
cd fe && npm run lint && npm run build
```

## Common tasks

| Task | Where to start |
| --- | --- |
| Add a code challenge | [docs/adding-challenges.md](docs/adding-challenges.md) |
| Change workspace UI | [docs/frontend.md](docs/frontend.md), `.cursor/skills/code-challenge-ux/` |
| Runner / submission contract | [docs/contracts.md](docs/contracts.md) |
| Seed catalog modules | `scripts/seed-challenges/` |

## Docs index

[docs/README.md](docs/README.md) — MVP spec, contracts, adding challenges, frontend, deploy, Cursor vendors.
