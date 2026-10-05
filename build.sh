#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes
find src/main/java -name '*.java' -print > build/main-sources.txt
javac --release 17 -encoding UTF-8 -d build/classes @build/main-sources.txt
if [ "${1:-}" = "--package" ]; then
  mkdir -p output/app
  jar --create --file output/app/MemeCoinClash.jar --main-class memecoinclash.MemeCoinClash -C build/classes . -C . assets
fi
