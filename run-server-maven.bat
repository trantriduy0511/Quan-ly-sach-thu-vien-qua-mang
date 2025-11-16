@echo off
echo Starting Server with Maven...
set PATH=C:\Program Files\maven-mvnd-1.0.3-windows-amd64\bin;%PATH%
call mvnd exec:java -Dexec.mainClass="server.Server" -Dexec.args=""
pause

