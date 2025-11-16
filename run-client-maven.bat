@echo off
echo Starting Client with Maven...
set PATH=C:\Program Files\maven-mvnd-1.0.3-windows-amd64\bin;%PATH%
call mvnd exec:java -Dexec.mainClass="client.LoginFrame" -Dexec.args=""
pause

