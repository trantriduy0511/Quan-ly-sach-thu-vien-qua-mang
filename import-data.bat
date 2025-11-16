@echo off
echo Importing data to MongoDB...
java -cp "lib\*;bin" server.DataImporter
pause


