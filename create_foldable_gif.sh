#!/bin/bash
set -euo pipefail

# Same timings as create_gif.sh:
# hold 1.0s (delay 100), morph 10 frames at 0.1s (delay 10), loop last -> first.

ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/fastlane/metadata/android/en-US/images/sevenInchScreenshots"
OUT="$ROOT/docs/assets/tkweek_foldable.gif"

files=()
while IFS= read -r f; do
  files+=("$f")
done < <(find "$SRC" -maxdepth 1 -type f -name '*.png' -exec basename {} \; | sort)

if (( ${#files[@]} < 2 )); then
  echo "Need at least 2 PNGs in $SRC" >&2
  exit 1
fi

# Downscale for web (full-res morph is impractically slow)
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

echo "--- Script Started ---"
echo "  -> Found ${#files[@]} PNGs in $SRC"
for f in "${files[@]}"; do
  echo "  -> Resize: $f"
  magick "$SRC/$f" -resize 675x "$TMP/$f"
done

cd "$TMP"
count=${#files[@]}
last_index=$((count - 1))
first_file="${files[0]}"
last_file="${files[$last_index]}"

CMD="magick -loop 0"
for ((i=0; i<last_index; i++)); do
  current="${files[i]}"
  next="${files[i+1]}"
  echo "  -> Adding transition: \"$current\" to \"$next\""
  CMD+=" \\( -delay 100 \"$current\" \\) \\( -delay 10 \"$current\" \"$next\" -morph 10 \\)"
done

echo "  -> Adding final hold: \"$last_file\""
CMD+=" \\( -delay 100 \"$last_file\" \\)"

echo "  -> Closing the loop: \"$last_file\" to \"$first_file\""
CMD+=" \\( -delay 10 \"$last_file\" \"$first_file\" -morph 10 \\) -layers Optimize \"$OUT\""

echo "--- Command Building Complete ---"
echo "Starting ImageMagick..."
echo "Output: $OUT"
eval "$CMD"

echo "--- DONE ---"
