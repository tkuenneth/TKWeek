#!/bin/bash
# Creates an animated GIF from the PNG screenshots in a folder.
# Usage: create_gif.sh <screenshot folder> <output gif>
#
# Timings as in the projects' former create_gif.sh: hold 1.0s (delay 100),
# morph 10 frames at 0.1s (delay 10), loop last -> first.
set -euo pipefail

SRC="$1"
OUT="$2"

# ImageMagick 7 is called magick; Ubuntu ships ImageMagick 6 (convert).
if command -v magick >/dev/null; then IM=magick; else IM=convert; fi

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

echo "Found ${#files[@]} PNGs in $SRC"
for f in "${files[@]}"; do
  "$IM" "$SRC/$f" -resize 675x "$TMP/$f"
done

last_index=$(( ${#files[@]} - 1 ))
args=(-loop 0)
for ((i = 0; i < last_index; i++)); do
  args+=( '(' -delay 100 "$TMP/${files[i]}" ')' '(' -delay 10 "$TMP/${files[i]}" "$TMP/${files[i+1]}" -morph 10 ')' )
done
args+=( '(' -delay 100 "$TMP/${files[last_index]}" ')' )
args+=( '(' -delay 10 "$TMP/${files[last_index]}" "$TMP/${files[0]}" -morph 10 ')' )
args+=( -layers Optimize "$OUT" )

mkdir -p "$(dirname "$OUT")"
"$IM" "${args[@]}"
echo "Created $OUT"
