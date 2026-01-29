@echo off
echo.
echo === Сборка BoatMouse.jar ===
echo.

REM Проверяем наличие файлов
if not exist "jna-5.13.0.jar" (
    echo ОШИБКА: Не найден файл jna-5.13.0.jar
    pause
    exit /b 1
)
if not exist "jna-platform-5.13.0.jar" (
    echo ОШИБКА: Не найден файл jna-platform-5.13.0.jar
    pause
    exit /b 1
)
if not exist "BoatMouse.java" (
    echo ОШИБКА: Не найден файл BoatMouse.java
    pause
    exit /b 1
)

REM Удаляем старые файлы сборки
if exist "build" rmdir /s /q build
mkdir build
if exist "BoatMouse.jar" del "BoatMouse.jar"

echo 1. Компиляция Java-файла...
javac -cp ".;jna-5.13.0.jar;jna-platform-5.13.0.jar" -d build BoatMouse.java
if %errorlevel% neq 0 (
    echo ОШИБКА КОМПИЛЯЦИИ! Проверьте код.
    pause
    exit /b 1
)

echo.
echo 2. Распаковка библиотек в папку build...
cd build
jar xf ..\jna-5.13.0.jar
jar xf ..\jna-platform-5.13.0.jar
cd ..

echo.
echo 3. Создание JAR-файла...
echo Main-Class: BoatMouse > MANIFEST.MF
jar cfm BoatMouse.jar MANIFEST.MF -C build .

echo.
echo 4. Очистка временных файлов...
del MANIFEST.MF
rmdir /s /q build

echo.
echo === ГОТОВО! ===
echo Создан файл: BoatMouse.jar
echo Его можно запускать командой: java -jar BoatMouse.jar
echo Или двойным кликом (если .jar связан с Java).
echo.
pause