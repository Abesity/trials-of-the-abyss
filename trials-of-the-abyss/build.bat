@echo off
rem Builds dist\TrialsOfTheAbyss.jar (needs JDK 17 or newer).
cd /d "%~dp0"
if exist out rmdir /s /q out
if exist dist rmdir /s /q dist
mkdir out
mkdir dist
dir /s /b src\*.java > out\sources.txt
javac --release 17 -encoding UTF-8 -d out @out\sources.txt || exit /b 1
xcopy /e /i /q res out >nul
echo Main-Class: main.Main> out\manifest.txt
jar cfm dist\TrialsOfTheAbyss.jar out\manifest.txt -C out .
echo Built dist\TrialsOfTheAbyss.jar  -  run: java -jar dist\TrialsOfTheAbyss.jar
