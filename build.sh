#!/usr/bin/env sh
# Builds dist/TrialsOfTheAbyss.jar (needs JDK 17 or newer).
set -e
cd "$(dirname "$0")"
rm -rf out dist
mkdir -p out dist
javac --release 17 -encoding UTF-8 -d out $(find src -name "*.java")
cp -r res/* out/
printf "Main-Class: main.Main\n" > out/manifest.txt
jar cfm dist/TrialsOfTheAbyss.jar out/manifest.txt -C out .
echo "Built dist/TrialsOfTheAbyss.jar  ->  run: java -jar dist/TrialsOfTheAbyss.jar"
