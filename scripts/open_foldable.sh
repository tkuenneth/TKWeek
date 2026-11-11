#!/bin/bash
set -euo pipefail

usage() {
  cat >&2 <<'USAGE'
Usage: open_foldable.sh
Opens the foldable emulator that ANDROID_SERIAL points to (or the only connected one) and waits
until it reports the OPENED state. Devices that are not foldable are left alone. OPEN_TIMEOUT
(seconds, default 10) limits the wait.
USAGE
  exit 2
}

[ $# -eq 0 ] || usage
: "${ANDROID_HOME:?Set ANDROID_HOME to your Android SDK location}"
ADB="$ANDROID_HOME/platform-tools/adb"
OPEN_TIMEOUT="${OPEN_TIMEOUT:-10}"

opened=$("$ADB" shell cmd device_state print-states | tr -d '\r' |
  sed -n "s/.*identifier=\([0-9]*\), name='OPENED'.*/\1/p" | head -n 1)
[ -n "$opened" ] || exit 0

"$ADB" shell cmd device_state state "$opened" >/dev/null
deadline=$((SECONDS + OPEN_TIMEOUT))
until [ "$("$ADB" shell cmd device_state print-state | tr -d '\r')" = "$opened" ]; do
  if [ "$SECONDS" -ge "$deadline" ]; then
    echo "The foldable did not report the OPENED state within ${OPEN_TIMEOUT}s" >&2
    exit 1
  fi
  sleep 1
done
