#!/bin/sh
# Enable shared git hooks for this repository (run once after clone).
set -eu

repo_root=$(git rev-parse --show-toplevel)
cd "$repo_root"

chmod +x .githooks/pre-commit .githooks/check-pathnames.sh
git config core.hooksPath .githooks

printf 'Git hooks enabled: core.hooksPath=.githooks\n'
printf 'Pre-commit will block paths with trailing spaces/dots (Windows-safe names).\n'
