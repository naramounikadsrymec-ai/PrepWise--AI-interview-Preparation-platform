@echo off
set ROOT_DIR=%~dp0

echo Starting PrepWise backend and frontend...
start "" powershell -ExecutionPolicy Bypass -NoExit -Command "Set-Location '%ROOT_DIR%backend\backend'; .\mvnw.cmd spring-boot:run"
start "" powershell -ExecutionPolicy Bypass -NoExit -Command "Set-Location '%ROOT_DIR%frontend'; if (-not (Test-Path 'node_modules')) { npm install }; npm start"
echo Backend and frontend starting in new windows.
