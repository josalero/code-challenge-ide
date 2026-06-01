# Cursor vendor packs (optional)

Install or refresh third-party rules/skills for UI and agent workflows.

## Vendor script (rules)

```bash
./scripts/install-cursor-vendors.sh
# WITH_ONLOOK=true ...  # optional large Onlook clone — see upstream https://docs.onlook.com
```

| Source | Active path |
| --- | --- |
| cursor-designer | `.cursor/rules/vendor/cursor-designer/` |
| Continue awesome-rules | `.cursor/rules/vendor/continue/` |
| ui-design-brain | `.cursor/skills/ui-design-brain/` |
| awesome-cursorrules, cursor-skills | `vendor/cursor/` (reference only) |

**Project rules take precedence:** `.cursor/rules/00-ui-*` … `70-*` (frontend), `12-java-26-style.mdc`, `20-spring-boot-4.mdc` (backend).

Commit SHAs: `vendor/cursor/manifest.json` after install.

## Skills CLI (`.agents/skills/`)

Additional skills can be installed with [skills CLI](https://github.com/vercel-labs/skills):

```bash
cd fe   # or repo root where components.json lives for shadcn-related packs
npx skills add shadcn/ui -a cursor
npx skills add <owner/repo> -a cursor
```

Installed packs land in **`.agents/skills/`** (Cursor agent scope). Project-specific skills live under **`.cursor/skills/`** (e.g. `code-challenge-ux` for the coding workspace).

| Path | Purpose |
| --- | --- |
| `.cursor/skills/code-challenge-ux/` | Workspace layout, run states, a11y |
| `.cursor/skills/ui-design-brain/` | General UI patterns |
| `.agents/skills/` | Vendor packs (shadcn, playwright, ui-audit, …) |

See [frontend.md](../frontend.md) for how skills relate to the React workspace.
