#!/usr/bin/env sh
# Builds the game, then runs the headless playthrough and the save/shop checks.
set -e
cd "$(dirname "$0")/.."
./build.sh >/dev/null
rm -rf tests/out && mkdir -p tests/out/classes tests/out/shots
javac -d tests/out/classes -cp out tests/harness/*.java
java -Djava.awt.headless=true -cp tests/out/classes:out harness.Bot tests/out/shots 2>/dev/null | grep -E '^\[|RESULT'
(cd tests/out && java -Djava.awt.headless=true -cp classes:../../out harness.SaveShopTest shots 2>/dev/null)
