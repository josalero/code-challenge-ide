# Frontend (`fe/`)

Browser UI for Code Training Lab.

**Stack:** React **19**, Vite, TypeScript, **Ant Design 5** (app chrome + legacy panels), **shadcn/ui** (workspace), Tailwind CSS, TanStack Query, Monaco Editor.

**Themes:** Light and dark modes share one component tree. Toggle via the sun/moon control in the app header (and on auth pages). Preference is stored in `localStorage` (`ctl-theme`); `index.html` applies the class before first paint to avoid flash. Tokens live in `fe/src/styles/themes.css` (`html.light` / `html.dark`). Catalog UI uses shadcn semantic tokens (`foreground`, `card`, `muted`, `border`) with `dark:` overrides where slate surfaces are still needed (e.g. workspace).

When this doc disagrees with `fe/package.json` or code, trust the build files.

---

## Run

```bash
cd fe && npm ci
npm run dev      # http://localhost:5173 — proxies /api → :8080
npm run build
npm run lint
```

React 19 requires the Ant Design compatibility patch at the top of `src/main.tsx`:

```ts
import "@ant-design/v5-patch-for-react-19";
```

---

## Key routes

| Path | Page | Notes |
| --- | --- | --- |
| `/challenges` | Challenge list | Filters by language + progress |
| `/metrics` | Learning metrics | Progress, runs, breakdowns by language/difficulty |
| `/challenges/:slug` | **Coding workspace** | Desktop-first IDE layout |
| `/challenges/new` | Create challenge | Admin only |
| `/admin/ops` | Runner/LSP ops | Admin only — Built/Warm status, Maven/LSP warm |
| `/login`, `/register` | Auth | |

---

## Coding workspace (`/challenges/:slug`)

Implemented under `fe/src/components/workspace/` and documented in `.cursor/skills/code-challenge-ux/SKILL.md`.

### Layout (desktop ≥ 1024px)

- **Header:** back link, title, language/runtime, autosave, Run + Submit
- **Left:** problem instructions, stats, public tests
- **Right:** Monaco editor (Solution + custom tests tabs)
- **Bottom:** resizable output tabs — Tests, Compiler, Analysis, Feedback, History
- **Resizable** horizontal (problem ↔ editor) and vertical (editor ↔ output) splits

### Mobile

- Instructions | Editor top tabs
- **View problem** sheet (left drawer)
- Output in bottom tab strip (no cramped triple split)

### Run lifecycle UI

States: loading, running, compilation error, failed test, timeout, service unavailable, successful submission — see `domain/workspaceRunState.ts`.

### Autosave

Solution draft debounced to `localStorage` (`ctl:draft:{slug}`). Custom tests saved via API (`PUT …/custom-tests`).

### IntelliSense (LSP)

All 11 challenge languages use Monaco + LSP when signed in and `CTL_LSP_ENABLED=true` on the API:

| Language | Server (Docker image) |
| --- | --- |
| Java | JDT LS (`lsp-java`) |
| Python | Pyright (`lsp-python`) |
| Go | gopls (`lsp-go`) |
| Node, TypeScript, React, Angular | typescript-language-server (`lsp-typescript`) |
| Vue | Vue language server (`lsp-typescript`, `CTL_LSP_LANGUAGE=vue`) |
| C# | csharp-ls (`lsp-dotnet`) |
| Rust | rust-analyzer (`lsp-rust`) |
| C++ | clangd (`lsp-cpp`) |

Build images once on the host (API spawns them via `docker run`):

```bash
make runners                      # build execution + LSP images
make lsp-warm                     # optional LSP initialize warm
./scripts/build-lsp-images.sh     # LSP images only (build)
```

Set `CTL_SKIP_LSP_WARM=1` to skip the LSP warm step (build only). Warm runs each **unique** LSP image once (7 images; Vue shares `lsp-typescript`), in parallel by default (`CTL_LSP_WARM_PARALLEL=4`). Go uses a fast `gopls version` smoke check instead of a full LSP handshake (the handshake can hang when warm containers pile up). Repeat runs skip warm when image IDs match `.ctl-lsp-warm-stamp`.

Frontend wiring: `fe/src/lsp/lspLanguageConfig.ts`, `fe/src/components/LspMonacoEditor.tsx`. WebSocket path: `/api/v1/lsp/{language}`.

---

## UI libraries

| Library | Used for |
| --- | --- |
| **shadcn/ui** | Workspace: Button, Select, Tabs, Resizable, Sheet, Badge, Alert, … (`fe/src/components/ui/`) |
| **Ant Design** | App layout, auth, challenge list, create form, AI coach internals |
| **Tailwind** | Layout, tokens, workspace styling (`fe/src/index.css`, `tailwind.config.js`) |
| **lucide-react** | Workspace icons |

shadcn is initialized via `fe/components.json`. Theme tokens live on `:root` in `index.css` (dark app). Do **not** import `shadcn/tailwind.css` on Tailwind v3 — use CSS variables instead.

Path alias: `@/*` → `fe/src/*` (`tsconfig.app.json`, `vite.config.ts`).

---

## Cursor skills (optional)

Project skills for UI work:

| Location | Purpose |
| --- | --- |
| `.cursor/skills/code-challenge-ux/` | Workspace layout and run-state UX |
| `.cursor/skills/ui-design-brain/` | General UI patterns |
| `.cursor/skills/html-tailwind-ui/` | Tailwind + a11y rules index |
| `.agents/skills/` | Installed via `npx skills add …` (shadcn, ui-audit, playwright, …) |

Install/update vendor packs: [cursor/README.md](./cursor/README.md).

---

## Adding UI for new challenge types

When a new challenge shape appears (e.g. multi-file, type-only):

1. Extend API types in `fe/src/api/types.ts` if response shape changes.
2. Update workspace components — prefer extending tabs/panels over new pages.
3. Document behavior in `code-challenge-ux` skill and [adding-challenges.md](./adding-challenges.md).
4. Keep Ant Design for list/admin; use shadcn for workspace chrome unless migrating deliberately.
