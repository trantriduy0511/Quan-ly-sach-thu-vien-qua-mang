@echo off
echo Starting Server...
if not exist lib (
    echo Error: lib folder not found!
    echo Please download sqlite-jdbc-x.x.x.jar and place it in lib folder
    pause
    exit /b 1
)
java -cp "lib/*;bin" server.Server
pause



