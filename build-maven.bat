@echo off
echo Building project with Maven...
echo Using Maven from: C:\Program Files\maven-mvnd-1.0.3-windows-amd64\bin
set PATH=C:\Program Files\maven-mvnd-1.0.3-windows-amd64\bin;%PATH%
call mvnd clean compile
if %errorlevel% == 0 (
    echo Build successful!
) else (
    echo Build failed!
    pause
    exit /b 1
)

