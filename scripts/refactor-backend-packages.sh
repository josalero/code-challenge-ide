#!/usr/bin/env bash
# One-time package restructure for be/. Re-run only on a clean tree.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE="${ROOT}/be/src/main/java/com/codetraininglab"
TEST_BASE="${ROOT}/be/src/test/java/com/codetraininglab"

move_dir() {
  local from="$1"
  local to="$2"
  if [[ -d "${BASE}/${from}" ]]; then
    mkdir -p "$(dirname "${BASE}/${to}")"
    git mv "${BASE}/${from}" "${BASE}/${to}" 2>/dev/null || mv "${BASE}/${from}" "${BASE}/${to}"
  fi
}

move_file() {
  local from="$1"
  local to="$2"
  if [[ -f "${BASE}/${from}" ]]; then
    mkdir -p "$(dirname "${BASE}/${to}")"
    git mv "${BASE}/${from}" "${BASE}/${to}" 2>/dev/null || mv "${BASE}/${from}" "${BASE}/${to}"
  fi
}

mkdir -p "${BASE}/platform/config" "${BASE}/platform/security" "${BASE}/platform/web" "${BASE}/platform/persistence"
mkdir -p "${BASE}/integration/runner" "${BASE}/integration/lsp"
mkdir -p "${BASE}/catalog/api" "${BASE}/catalog/application"
mkdir -p "${BASE}/submission/api" "${BASE}/submission/application" "${BASE}/submission/messaging"
mkdir -p "${BASE}/feedback/application"
mkdir -p "${BASE}/coach/api" "${BASE}/coach/application"
mkdir -p "${BASE}/identity/api" "${BASE}/identity/application"
mkdir -p "${BASE}/operations/api" "${BASE}/operations/application"

# Platform
move_dir "config" "platform/config"
move_dir "security" "platform/security"
move_dir "api" "platform/web"
move_dir "persistence" "platform/persistence"

# Integrations
move_dir "runner" "integration/runner"
move_dir "lsp" "integration/lsp"

# Catalog
for f in ChallengeController.java CustomTestsController.java; do
  move_file "challenge/${f}" "catalog/api/${f}"
done
for f in ChallengeService.java CustomTestsService.java ChallengeGitLoader.java; do
  move_file "challenge/${f}" "catalog/application/${f}"
done
rmdir "${BASE}/challenge" 2>/dev/null || true

# Identity
for f in AuthController.java LoginRequest.java RegisterRequest.java AuthResponse.java; do
  move_file "auth/${f}" "identity/api/${f}"
done
move_file "auth/AuthService.java" "identity/application/AuthService.java"
rmdir "${BASE}/auth" 2>/dev/null || true
move_file "user/MeController.java" "identity/api/MeController.java"
rmdir "${BASE}/user" 2>/dev/null || true

# Coach
move_file "ai/AiCoachController.java" "coach/api/AiCoachController.java"
for f in AiCoachService.java AiResponseParser.java AiProviderUrls.java; do
  move_file "ai/${f}" "coach/application/${f}"
done
rmdir "${BASE}/ai" 2>/dev/null || true

# Feedback
move_file "feedback/FeedbackAggregator.java" "feedback/application/FeedbackAggregator.java"
rmdir "${BASE}/feedback" 2>/dev/null || true

# Operations
for f in OpsController.java DeadLetterReplayResponse.java DeadLetterSubmissionResponse.java; do
  move_file "ops/${f}" "operations/api/${f}"
done
move_file "ops/DeadLetterQueueService.java" "operations/application/DeadLetterQueueService.java"
rmdir "${BASE}/ops" 2>/dev/null || true

# Submission
for f in SubmissionController.java CreateSubmissionRequest.java SubmissionResponse.java \
  ReportResponse.java FeedbackItemResponse.java RunnerLogsResponse.java; do
  move_file "submission/${f}" "submission/api/${f}"
done
for f in SubmissionService.java SubmissionProcessor.java SubmissionEventHub.java \
  SubmissionEventCatchUp.java ReportSummarySupport.java; do
  move_file "submission/${f}" "submission/application/${f}"
done
for f in SubmissionJobListener.java SubmissionJobMessage.java SubmissionEventType.java SsePayloadKeys.java; do
  move_file "submission/${f}" "submission/messaging/${f}"
done
rmdir "${BASE}/submission" 2>/dev/null || true

# Rewrite packages and imports in main + test
while IFS= read -r -d '' file; do
  sed -i.bak \
    -e 's/com\.codetraininglab\.submission\.messaging/com.codetraininglab.submission.messaging/g' \
    -e 's/com\.codetraininglab\.submission\.application/com.codetraininglab.submission.application/g' \
    -e 's/com\.codetraininglab\.submission\.api/com.codetraininglab.submission.api/g' \
    -e 's/com\.codetraininglab\.operations\.application/com.codetraininglab.operations.application/g' \
    -e 's/com\.codetraininglab\.operations\.api/com.codetraininglab.operations.api/g' \
    -e 's/com\.codetraininglab\.coach\.application/com.codetraininglab.coach.application/g' \
    -e 's/com\.codetraininglab\.coach\.api/com.codetraininglab.coach.api/g' \
    -e 's/com\.codetraininglab\.feedback\.application/com.codetraininglab.feedback.application/g' \
    -e 's/com\.codetraininglab\.catalog\.application/com.codetraininglab.catalog.application/g' \
    -e 's/com\.codetraininglab\.catalog\.api/com.codetraininglab.catalog.api/g' \
    -e 's/com\.codetraininglab\.identity\.application/com.codetraininglab.identity.application/g' \
    -e 's/com\.codetraininglab\.identity\.api/com.codetraininglab.identity.api/g' \
    -e 's/com\.codetraininglab\.integration\.runner/com.codetraininglab.integration.runner/g' \
    -e 's/com\.codetraininglab\.integration\.lsp/com.codetraininglab.integration.lsp/g' \
    -e 's/com\.codetraininglab\.platform\.persistence/com.codetraininglab.platform.persistence/g' \
    -e 's/com\.codetraininglab\.platform\.web/com.codetraininglab.platform.web/g' \
    -e 's/com\.codetraininglab\.platform\.security/com.codetraininglab.platform.security/g' \
    -e 's/com\.codetraininglab\.platform\.config/com.codetraininglab.platform.config/g' \
    -e 's/package com\.codetraininglab\.config;/package com.codetraininglab.platform.config;/g' \
    -e 's/package com\.codetraininglab\.security;/package com.codetraininglab.platform.security;/g' \
    -e 's/package com\.codetraininglab\.api;/package com.codetraininglab.platform.web;/g' \
    -e 's/package com\.codetraininglab\.persistence;/package com.codetraininglab.platform.persistence;/g' \
    -e 's/package com\.codetraininglab\.runner;/package com.codetraininglab.integration.runner;/g' \
    -e 's/package com\.codetraininglab\.lsp;/package com.codetraininglab.integration.lsp;/g' \
    -e 's/package com\.codetraininglab\.challenge;/package com.codetraininglab.catalog.application;/g' \
    -e 's/package com\.codetraininglab\.auth;/package com.codetraininglab.identity.api;/g' \
    -e 's/package com\.codetraininglab\.user;/package com.codetraininglab.identity.api;/g' \
    -e 's/package com\.codetraininglab\.ai;/package com.codetraininglab.coach.application;/g' \
    -e 's/package com\.codetraininglab\.feedback;/package com.codetraininglab.feedback.application;/g' \
    -e 's/package com\.codetraininglab\.ops;/package com.codetraininglab.operations.application;/g' \
    -e 's/package com\.codetraininglab\.submission;/package com.codetraininglab.submission.application;/g' \
    -e 's/com\.codetraininglab\.config\./com.codetraininglab.platform.config./g' \
    -e 's/com\.codetraininglab\.security\./com.codetraininglab.platform.security./g' \
    -e 's/com\.codetraininglab\.api\./com.codetraininglab.platform.web./g' \
    -e 's/com\.codetraininglab\.persistence\./com.codetraininglab.platform.persistence./g' \
    -e 's/com\.codetraininglab\.runner\./com.codetraininglab.integration.runner./g' \
    -e 's/com\.codetraininglab\.lsp\./com.codetraininglab.integration.lsp./g' \
    -e 's/com\.codetraininglab\.challenge\.ChallengeService/com.codetraininglab.catalog.application.ChallengeService/g' \
    -e 's/com\.codetraininglab\.challenge\./com.codetraininglab.catalog.application./g' \
    -e 's/com\.codetraininglab\.auth\./com.codetraininglab.identity.api./g' \
    -e 's/com\.codetraininglab\.user\./com.codetraininglab.identity.api./g' \
    -e 's/com\.codetraininglab\.ai\./com.codetraininglab.coach.application./g' \
    -e 's/com\.codetraininglab\.feedback\./com.codetraininglab.feedback.application./g' \
    -e 's/com\.codetraininglab\.ops\./com.codetraininglab.operations.application./g' \
    -e 's/com\.codetraininglab\.submission\./com.codetraininglab.submission.application./g' \
    "$file"
  rm -f "${file}.bak"
done < <(find "${ROOT}/be/src" -name '*.java' -print0)

# Fix per-file packages (controllers/services split)
fix_pkg() {
  local file="$1"
  local pkg="$2"
  sed -i.bak "s/^package .*;/package ${pkg};/" "$file"
  rm -f "${file}.bak"
}

fix_pkg "${BASE}/catalog/api/ChallengeController.java" "com.codetraininglab.catalog.api"
fix_pkg "${BASE}/catalog/api/CustomTestsController.java" "com.codetraininglab.catalog.api"
fix_pkg "${BASE}/coach/api/AiCoachController.java" "com.codetraininglab.coach.api"
fix_pkg "${BASE}/identity/application/AuthService.java" "com.codetraininglab.identity.application"
fix_pkg "${BASE}/submission/api/SubmissionController.java" "com.codetraininglab.submission.api"
for f in "${BASE}"/submission/api/*.java; do [[ -f "$f" ]] && fix_pkg "$f" "com.codetraininglab.submission.api"; done
for f in "${BASE}"/submission/messaging/*.java; do [[ -f "$f" ]] && fix_pkg "$f" "com.codetraininglab.submission.messaging"; done
for f in "${BASE}"/operations/api/*.java; do [[ -f "$f" ]] && fix_pkg "$f" "com.codetraininglab.operations.api"; done

echo "Package refactor complete. Run: ./gradlew :be:check"
