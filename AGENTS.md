# Agent Instructions — Code Training Lab

If this file disagrees with **build files or code**, trust the build files and code.

| Area | Stack |
| --- | --- |
| `be/` | Java **26**, Spring Boot **4** — [be/ARCHITECTURE.md](be/ARCHITECTURE.md) |
| `fe/` | React **19**, Vite, Ant Design, Monaco |

## How to run

See [README.md](README.md) (local infra + host dev, full Docker stack, CI).

```bash
npm install          # Husky (repo root)
./gradlew :be:check
cd fe && npm run lint && npm run build
```

## Docs index

[docs/README.md](docs/README.md) — MVP spec, contracts, deploy, Cursor vendors.
