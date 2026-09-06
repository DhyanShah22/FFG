#!/bin/sh
# Reject path components with leading/trailing whitespace or trailing dots.
# Windows cannot checkout paths like "Facilitators " reliably.

set -eu

invalid=0

report_invalid_path() {
  path=$1
  reason=$2
  printf '  %s (%s)\n' "$path" "$reason" >&2
  invalid=1
}

check_path() {
  path=$1
  rest=$path

  while [ -n "$rest" ]; do
    case "$rest" in
      */*)
        part=${rest%%/*}
        rest=${rest#*/}
        ;;
      *)
        part=$rest
        rest=
        ;;
    esac

    trimmed=$(printf '%s' "$part" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
    if [ "$part" != "$trimmed" ]; then
      report_invalid_path "$path" "path component has leading or trailing whitespace: '$part'"
      return
    fi

    case "$part" in
      .|..) ;;
      *.)
        report_invalid_path "$path" "path component has trailing dot: '$part'"
        return
        ;;
    esac
  done
}

while IFS= read -r path; do
  [ -n "$path" ] || continue
  check_path "$path"
done <<EOF
$(git diff --cached --name-only --diff-filter=ACMR 2>/dev/null || true)
EOF

if [ "$invalid" -ne 0 ]; then
  printf '\nCommit blocked: fix path names above.\n' >&2
  printf 'Rules: no leading/trailing spaces in file or folder names; no trailing dots.\n' >&2
  printf 'Example: use "Facilitators/" not "Facilitators /".\n' >&2
  exit 1
fi

exit 0
