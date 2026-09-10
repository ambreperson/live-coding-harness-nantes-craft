#!/usr/bin/env bash
# SessionStart hook: runs the test suite and reports a short pass/fail summary
# back into the conversation via a systemMessage.
set -euo pipefail

cd "${CLAUDE_PROJECT_DIR:-.}"

OUTPUT="$(./mvnw test 2>&1)" || true

SUMMARY="$(printf '%s\n' "$OUTPUT" | grep -E '^\[INFO\] Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$' | tail -1 || true)"

if [ -z "$SUMMARY" ]; then
  jq -n '{systemMessage: "🧪 Tests: build failed"}'
  exit 0
fi

RUN=$(printf '%s' "$SUMMARY" | sed -E 's/.*Tests run: ([0-9]+).*/\1/')
FAILURES=$(printf '%s' "$SUMMARY" | sed -E 's/.*Failures: ([0-9]+).*/\1/')
ERRORS=$(printf '%s' "$SUMMARY" | sed -E 's/.*Errors: ([0-9]+).*/\1/')
PASSED=$((RUN - FAILURES - ERRORS))

if [ "$FAILURES" -eq 0 ] && [ "$ERRORS" -eq 0 ]; then
  ICON="✅"
else
  ICON="❌"
fi

jq -n --arg msg "${ICON} Tests: ${PASSED}/${RUN} passed" '{systemMessage: $msg}'
