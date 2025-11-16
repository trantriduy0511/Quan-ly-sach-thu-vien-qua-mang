@echo off
echo Building project...
if not exist bin mkdir bin
if not exist lib (
    echo Error: lib folder not found!
    echo Please download sqlite-jdbc-x.x.x.jar and place it in lib folder
    pause
    exit /b 1
)
javac -cp "lib/*;." -d bin src\model\*.java src\util\*.java src\server\*.java src\client\*.java
if %errorlevel% == 0 (
    echo Build successful!
) else (
    echo Build failed!
    pause
    exit /b 1
)



