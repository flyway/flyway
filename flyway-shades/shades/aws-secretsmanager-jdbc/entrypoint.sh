#!/bin/sh
set -e
cd /artifact
if ! ls *.jar >/dev/null 2>&1; then
  echo "No shaded jar found to export" >&2
  exit 1
fi
mkdir -p "$OUT_DIR"
for jar in *.jar; do
  cp "$jar" "$OUT_DIR/$jar"
  echo "Exported $jar to $OUT_DIR/$jar"
done
