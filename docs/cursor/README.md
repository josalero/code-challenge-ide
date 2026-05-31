# Cursor vendor packs (optional)

Install or refresh third-party rules/skills:

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
