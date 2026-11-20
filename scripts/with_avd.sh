#!/bin/bash
set -uo pipefail

usage() {
  cat >&2 <<'USAGE'
Usage: with_avd.sh <avd name> <command> [arguments...]
Starts the named emulator on a free port, runs the command with ANDROID_SERIAL set to it and stops
the emulator again when the command ends. BOOT_TIMEOUT (seconds, default 300) limits how long the
boot may take. EMULATOR_LOG names a file that receives the emulator's own output (default: discarded).
EMULATOR_ARGS adds flags to the emulator command line, split on spaces, for the ones that can only
be given at startup such as -camera-back. HIDE_ERROR_DIALOGS=1 keeps the system's crash and ANR
dialogs off the screen while the command runs, which matters when it captures screenshots.
Every emulator starts in its natural orientation: the virtual accelerometer is reset to upright and
any rotation lock a previous run left behind is cleared, because a sideways display has no matching
skin and fails a screenshot run.
Every emulator this script boots gets a quiet status bar: SystemUI demo mode with a fixed clock
(DEMO_CLOCK, default 1200), a full battery and a full wifi icon, no mobile or notification icons,
Safety Center's data cleared and everything already posted snoozed. DEMO_MODE=0 leaves the bars
alone.
USAGE
  exit 2
}

[ $# -ge 2 ] || usage
AVD_NAME=$1
shift
: "${ANDROID_HOME:?Set ANDROID_HOME to your Android SDK location}"
ADB="$ANDROID_HOME/platform-tools/adb"
EMULATOR="$ANDROID_HOME/emulator/emulator"
BOOT_TIMEOUT="${BOOT_TIMEOUT:-300}"
for tool in "$ADB" "$EMULATOR"; do
  [ -x "$tool" ] || { echo "Missing $tool" >&2; exit 1; }
done

free_port() {
  local port=5554
  local devices
  devices=$("$ADB" devices)
  while grep -q "emulator-$port" <<<"$devices"; do
    port=$((port + 2))
  done
  echo "$port"
}

wait_for() {
  local description=$1
  shift
  local deadline=$((SECONDS + BOOT_TIMEOUT))
  until "$@"; do
    if [ "$SECONDS" -ge "$deadline" ]; then
      echo "$AVD_NAME: $description did not happen within ${BOOT_TIMEOUT}s" >&2
      return 1
    fi
    sleep 2
  done
}

is_booted() {
  [ "$("$ADB" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]
}

is_unlocked() {
  [ "$("$ADB" -s "$SERIAL" shell am get-started-user-state 0 2>/dev/null | tr -d '\r')" = RUNNING_UNLOCKED ]
}

demo() {
  "$ADB" -s "$SERIAL" shell am broadcast -a com.android.systemui.demo -e command "$@" \
    >/dev/null 2>&1 || true
}

snooze_notifications() {
  "$ADB" -s "$SERIAL" shell cmd notification list 2>/dev/null | tr -d '\r' |
    while IFS= read -r key; do
      case "$key" in
        *\|*\|*\|*)
          "$ADB" -s "$SERIAL" shell "cmd notification snooze --for 86400000 '$key'" \
            >/dev/null 2>&1 || true
          ;;
      esac
    done
}

natural_orientation() {
  "$ADB" -s "$SERIAL" emu sensor set acceleration 0:9.81:0 >/dev/null 2>&1 || true
  "$ADB" -s "$SERIAL" shell settings put system user_rotation 0 >/dev/null 2>&1 || true
  "$ADB" -s "$SERIAL" shell wm user-rotation free >/dev/null 2>&1 || true
}

is_upright() {
  [ "$("$ADB" -s "$SERIAL" shell dumpsys window displays 2>/dev/null |
    grep -oE 'mRotation=[0-9]' | head -1 | tr -d '\r')" = mRotation=0 ]
}

quiet_status_bar() {
  "$ADB" -s "$SERIAL" shell settings put global sysui_demo_allowed 1 >/dev/null 2>&1 || true
  demo enter
  demo clock -e hhmm "${DEMO_CLOCK:-1200}"
  demo battery -e level 100 -e plugged false
  demo network -e wifi show -e level 4 -e fully true
  demo network -e mobile hide
  demo notifications -e visible false
  demo status -e volume hide -e bluetooth hide -e location hide -e alarm hide -e sync hide \
    -e tty hide -e eri hide -e mute hide -e speakerphone hide
  "$ADB" -s "$SERIAL" shell cmd safety_center clear-data >/dev/null 2>&1 || true
  snooze_notifications
}

STARTED_PID=""
WATCHDOG_PID=""
SERIAL=""
cleanup() {
  kill "$WATCHDOG_PID" 2>/dev/null || true
  if [ "${HIDE_ERROR_DIALOGS:-}" = 1 ]; then
    "$ADB" -s "$SERIAL" shell settings delete global hide_error_dialogs >/dev/null 2>&1 || true
  fi
  "$ADB" -s "$SERIAL" emu kill >/dev/null 2>&1 || true
  kill "$STARTED_PID" 2>/dev/null || true
  wait "$STARTED_PID" 2>/dev/null || true
}
trap cleanup EXIT
trap 'exit 130' INT TERM

port=$(free_port)
options=(-avd "$AVD_NAME" -port "$port" -no-snapshot-save -no-audio)
if [ -n "${EMULATOR_ARGS:-}" ]; then
  read -ra extra_options <<<"$EMULATOR_ARGS"
  options+=("${extra_options[@]}")
fi
"$EMULATOR" "${options[@]}" >"${EMULATOR_LOG:-/dev/null}" 2>&1 &
STARTED_PID=$!
SERIAL="emulator-$port"

parent=$$
(
  while kill -0 "$parent" 2>/dev/null; do
    sleep 5
  done
  "$ADB" -s "$SERIAL" emu kill >/dev/null 2>&1 || true
  kill "$STARTED_PID" 2>/dev/null || true
) >/dev/null 2>&1 &
WATCHDOG_PID=$!

wait_for "booting" is_booted || exit 1
wait_for "unlocking" is_unlocked || exit 1
if [ "${HIDE_ERROR_DIALOGS:-}" = 1 ]; then
  "$ADB" -s "$SERIAL" shell settings put global hide_error_dialogs 1 >/dev/null 2>&1 || true
fi
natural_orientation
wait_for "returning to its natural orientation" is_upright || exit 1
if [ "${DEMO_MODE:-1}" = 1 ]; then
  quiet_status_bar
fi
export ANDROID_SERIAL="$SERIAL"
"$@"
