#!/usr/bin/env bash
# Install third-party Cursor rules/skills catalogs into this repo.
# Re-run after upstream updates: ./scripts/install-cursor-vendors.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

VENDOR_ROOT="${ROOT}/vendor/cursor"
RULES_VENDOR="${ROOT}/.cursor/rules/vendor"
SKILLS_DIR="${ROOT}/.cursor/skills"
DOCS_CURSOR="${ROOT}/docs/cursor"

CLONE_DEPTH="${CLONE_DEPTH:-1}"
WITH_ONLOOK="${WITH_ONLOOK:-false}"

clone_or_pull() {
  local url="$1"
  local dest="$2"
  if [[ -d "${dest}/.git" ]]; then
    echo "Updating ${dest}…"
    git -C "${dest}" pull --ff-only
  else
    echo "Cloning ${url} → ${dest}…"
    git clone --depth "${CLONE_DEPTH}" "${url}" "${dest}"
  fi
}

mkdir -p "${VENDOR_ROOT}" "${RULES_VENDOR}" "${SKILLS_DIR}" "${DOCS_CURSOR}"

# --- Reference catalogs (browse / cherry-pick; not all auto-loaded) ---
clone_or_pull "https://github.com/PatrickJS/awesome-cursorrules.git" \
  "${VENDOR_ROOT}/awesome-cursorrules"
clone_or_pull "https://github.com/continuedev/awesome-rules.git" \
  "${VENDOR_ROOT}/awesome-rules"
clone_or_pull "https://github.com/araguaci/cursor-skills.git" \
  "${VENDOR_ROOT}/cursor-skills"

# --- Active: cursor-designer (lean product profile → .cursor/rules/vendor) ---
DESIGNER_SRC="${VENDOR_ROOT}/cursor-designer"
clone_or_pull "https://github.com/spencergoldade/cursor-designer.git" "${DESIGNER_SRC}"
mkdir -p "${RULES_VENDOR}/cursor-designer/core" "${RULES_VENDOR}/cursor-designer/frontend"
LEAN_FILES=(
  "core/design-core.mdc"
  "core/cursor-behavior-constraints.mdc"
  "frontend/ui-layout-and-density.mdc"
  "frontend/ux-forms-and-validation.mdc"
  "frontend/accessibility-frontend.mdc"
  "frontend/ux-flows-and-feedback.mdc"
)
for rel in "${LEAN_FILES[@]}"; do
  src="${DESIGNER_SRC}/.cursor/rules/${rel}"
  dest="${RULES_VENDOR}/cursor-designer/${rel}"
  cp "${src}" "${dest}"
  # Scope to this repo's React frontend
  if grep -q 'globs: \["\*\*/\*\.tsx"\]' "${dest}" 2>/dev/null; then
    sed -i.bak 's|globs: \["\*\*/\*\.tsx"\]|globs: ["fe/src/**/*.tsx", "fe/src/**/*.ts"]|' "${dest}"
    rm -f "${dest}.bak"
  fi
done

# --- Active: ui-design-brain skill ---
clone_or_pull "https://github.com/carmahhawwari/ui-design-brain.git" \
  "${SKILLS_DIR}/ui-design-brain"

# --- Active: Continue awesome-rules (React + Tailwind for fe/) ---
CONTINUE_SRC="${VENDOR_ROOT}/awesome-rules"
mkdir -p "${RULES_VENDOR}/continue"
for rule_dir in react tailwind typescript; do
  if [[ -d "${CONTINUE_SRC}/rules/${rule_dir}" ]]; then
    rm -rf "${RULES_VENDOR}/continue/${rule_dir}"
    cp -R "${CONTINUE_SRC}/rules/${rule_dir}" "${RULES_VENDOR}/continue/${rule_dir}"
    find "${RULES_VENDOR}/continue/${rule_dir}" -name '*.md' -print0 | while IFS= read -r -d '' f; do
      sed -i.bak 's|globs: "\*\*/\*\.{jsx,tsx}"|globs: "fe/src/**/*.{tsx,ts}"|' "${f}" 2>/dev/null || true
      sed -i.bak 's|globs: "\*\*/\*\.tsx"|globs: "fe/src/**/*.tsx"|' "${f}" 2>/dev/null || true
      rm -f "${f}.bak"
    done
  fi
done

# --- Optional: Onlook (large monorepo — visual editor, not Cursor rules) ---
if [[ "${WITH_ONLOOK}" == "true" ]]; then
  clone_or_pull "https://github.com/onlook-dev/onlook.git" "${VENDOR_ROOT}/onlook"
fi

# --- Manifest for reproducibility ---
COMMIT() {
  local dir="$1"
  if [[ -d "${dir}/.git" ]]; then
    git -C "${dir}" rev-parse HEAD 2>/dev/null || echo "unknown"
  else
    echo "missing"
  fi
}

cat > "${VENDOR_ROOT}/manifest.json" <<EOF
{
  "installedAt": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "repositories": {
    "awesome-cursorrules": {
      "url": "https://github.com/PatrickJS/awesome-cursorrules",
      "path": "vendor/cursor/awesome-cursorrules",
      "commit": "$(COMMIT "${VENDOR_ROOT}/awesome-cursorrules")",
      "role": "reference-catalog"
    },
    "cursor-designer": {
      "url": "https://github.com/spencergoldade/cursor-designer",
      "path": "vendor/cursor/cursor-designer",
      "commit": "$(COMMIT "${DESIGNER_SRC}")",
      "role": "active-rules-lean-copied-to-.cursor/rules/vendor/cursor-designer"
    },
    "ui-design-brain": {
      "url": "https://github.com/carmahhawwari/ui-design-brain",
      "path": ".cursor/skills/ui-design-brain",
      "commit": "$(COMMIT "${SKILLS_DIR}/ui-design-brain")",
      "role": "active-skill"
    },
    "awesome-rules": {
      "url": "https://github.com/continuedev/awesome-rules",
      "path": "vendor/cursor/awesome-rules",
      "commit": "$(COMMIT "${CONTINUE_SRC}")",
      "role": "active-rules-react-tailwind-copied-to-.cursor/rules/vendor/continue"
    },
    "cursor-skills": {
      "url": "https://github.com/araguaci/cursor-skills",
      "path": "vendor/cursor/cursor-skills",
      "commit": "$(COMMIT "${VENDOR_ROOT}/cursor-skills")",
      "role": "reference-catalog"
    },
    "onlook": {
      "url": "https://github.com/onlook-dev/onlook",
      "path": "vendor/cursor/onlook",
      "commit": "$(COMMIT "${VENDOR_ROOT}/onlook")",
      "role": "optional-visual-editor",
      "installed": ${WITH_ONLOOK}
    }
  }
}
EOF

cp "${VENDOR_ROOT}/manifest.json" "${DOCS_CURSOR}/manifest.json"

echo ""
echo "Done. See docs/cursor/README.md"
echo "  Active rules:  .cursor/rules/vendor/cursor-designer/, .cursor/rules/vendor/continue/"
echo "  Active skill:  .cursor/skills/ui-design-brain/"
echo "  Reference:     vendor/cursor/awesome-cursorrules, vendor/cursor/cursor-skills"
if [[ "${WITH_ONLOOK}" == "true" ]]; then
  echo "  Onlook clone:  vendor/cursor/onlook (run separately — not part of CTL app)"
fi
