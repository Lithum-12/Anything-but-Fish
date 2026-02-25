@echo off
setlocal enabledelayedexpansion

set "JAVA_17=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
set "JAVA_21=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"

set "GRADLE_7_6_1=D:\gradle-7.6.1\bin\gradle.bat"
set "GRADLE_8_6=D:\Program Files I\gradle-8.6\bin\gradle.bat"
set "GRADLE_9_3=D:\Program Files I\gradle-9.3.0\bin\gradle.bat"

set "OUTPUT_BASE=%~dp0output"

echo Building mc-1.17.1 (Gradle 7.6.1, Java 17)...
git checkout mc-1.17.1
set "JAVA_HOME=%JAVA_17%"
call "%GRADLE_7_6_1%" build
if exist "build\libs\*.jar" (
    if not exist "%OUTPUT_BASE%\mc-1.17.1" mkdir "%OUTPUT_BASE%\mc-1.17.1"
    for /f "delims=" %%j in ('dir /b build\libs\*.jar') do copy "build\libs\%%j" "%OUTPUT_BASE%\mc-1.17.1\ABF mc-1.17.1.jar"
)

echo Building mc-1.18.2 (Gradle 7.6.1, Java 17)...
git checkout mc-1.18.2
set "JAVA_HOME=%JAVA_17%"
call "%GRADLE_7_6_1%" build
if exist "build\libs\*.jar" (
    if not exist "%OUTPUT_BASE%\mc-1.18.2" mkdir "%OUTPUT_BASE%\mc-1.18.2"
    for /f "delims=" %%j in ('dir /b build\libs\*.jar') do copy "build\libs\%%j" "%OUTPUT_BASE%\mc-1.18.2\ABF mc-1.18.2.jar"
)

echo Building mc-1.19.2 (Gradle 7.6.1, Java 17)...
git checkout mc-1.19.2
set "JAVA_HOME=%JAVA_17%"
call "%GRADLE_7_6_1%" build
if exist "build\libs\*.jar" (
    if not exist "%OUTPUT_BASE%\mc-1.19.2" mkdir "%OUTPUT_BASE%\mc-1.19.2"
    for /f "delims=" %%j in ('dir /b build\libs\*.jar') do copy "build\libs\%%j" "%OUTPUT_BASE%\mc-1.19.2\ABF mc-1.19.2.jar"
)

echo Building mc-1.20.1 (Gradle 8.6, Java 17)...
git checkout mc-1.20.1
set "JAVA_HOME=%JAVA_17%"
call "%GRADLE_8_6%" build
if exist "build\libs\*.jar" (
    if not exist "%OUTPUT_BASE%\mc-1.20.1" mkdir "%OUTPUT_BASE%\mc-1.20.1"
    for /f "delims=" %%j in ('dir /b build\libs\*.jar') do copy "build\libs\%%j" "%OUTPUT_BASE%\mc-1.20.1\ABF mc-1.20.1.jar"
)

echo Building mc-1.20.4 (Gradle 8.6, Java 17)...
git checkout mc-1.20.4
set "JAVA_HOME=%JAVA_17%"
call "%GRADLE_8_6%" build
if exist "build\libs\*.jar" (
    if not exist "%OUTPUT_BASE%\mc-1.20.4" mkdir "%OUTPUT_BASE%\mc-1.20.4"
    for /f "delims=" %%j in ('dir /b build\libs\*.jar') do copy "build\libs\%%j" "%OUTPUT_BASE%\mc-1.20.4\ABF mc-1.20.4.jar"
)

echo Building mc-1.21.1 (Gradle 9.3, Java 21)...
git checkout mc-1.21.1
set "JAVA_HOME=%JAVA_21%"
call "%GRADLE_9_3%" build
if exist "build\libs\*.jar" (
    if not exist "%OUTPUT_BASE%\mc-1.21.1" mkdir "%OUTPUT_BASE%\mc-1.21.1"
    for /f "delims=" %%j in ('dir /b build\libs\*.jar') do copy "build\libs\%%j" "%OUTPUT_BASE%\mc-1.21.1\ABF mc-1.21.1.jar"
)

echo Building mc-1.21.11 (Gradle 9.3, Java 21)...
git checkout mc-1.21.11
set "JAVA_HOME=%JAVA_21%"
call "%GRADLE_9_3%" build
if exist "build\libs\*.jar" (
    if not exist "%OUTPUT_BASE%\mc-1.21.11" mkdir "%OUTPUT_BASE%\mc-1.21.11"
    for /f "delims=" %%j in ('dir /b build\libs\*.jar') do copy "build\libs\%%j" "%OUTPUT_BASE%\mc-1.21.11\ABF mc-1.21.11.jar"
)

echo Done! All jars are in %OUTPUT_BASE%
