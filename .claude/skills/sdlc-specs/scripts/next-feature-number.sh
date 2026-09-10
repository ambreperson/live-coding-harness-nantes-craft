#!/usr/bin/env bash
# Prints the next zero-padded 3-digit feature number for sdlc/NNN-slug.md,
# based on the highest NNN already present in the sdlc/ directory.
set -euo pipefail

SDLC_DIR="${1:-sdlc}"

if [ ! -d "$SDLC_DIR" ]; then
  echo "001"
  exit 0
fi

HIGHEST=$(find "$SDLC_DIR" -maxdepth 1 -name '[0-9][0-9][0-9]-*.md' -print 2>/dev/null \
  | sed -E 's#.*/([0-9]{3})-.*#\1#' \
  | sort -n \
  | tail -1)

if [ -z "$HIGHEST" ]; then
  echo "001"
else
  printf '%03d\n' $((10#$HIGHEST + 1))
fi
