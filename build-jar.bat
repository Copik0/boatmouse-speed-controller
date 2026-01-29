@echo off
echo.
echo === Building BoatMouse.jar ===
echo.

REM Checking for files
if not exist "jna-5.13.0.jar" (
    echo ERROR: File jna-5.13.0.jar not found
    pause
    exit /b 1
)
if not exist "jna-platform-5.13.0.jar" (
    echo ERROR: File jna-platform-5.13.0.jar not found
    pause
    exit /b 1
)
if not exist "BoatMouse.java" (
    echo ERROR: File BoatMouse.java not found
    pause
    exit /b 1
)

REM Removing old build files
if exist "build" rmdir /s /q build
mkdir build
if exist "BoatMouse.jar" del "BoatMouse.jar"

echo 1. Compiling Java file...
javac -cp ".;jna-5.13.0.jar;jna-platform-5.13.0.jar" -d build BoatMouse.java
if %errorlevel% neq 0 (
    echo COMPILATION ERROR! Check the code.
    pause
    exit /b 1
)

echo.
echo 2. Unpacking libraries into build folder...
cd build
jar xf ..\jna-5.13.0.jar
jar xf ..\jna-platform-5.13.0.jar
cd ..

echo.
echo 3. Creating JAR file...
echo Main-Class: BoatMouse > MANIFEST.MF
jar cfm BoatMouse.jar MANIFEST.MF -C build .

echo.
echo 4. Cleaning up temporary files...
del MANIFEST.MF
rmdir /s /q build

echo.
echo === DONE! ===
echo File created: BoatMouse.jar
echo You can run it using: java -jar BoatMouse.jar
echo Or by double-clicking (if .jar is associated with Java).
echo.
pause
