$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $scriptDir 'backend\backend'
$frontendDir = Join-Path $scriptDir 'frontend'

Write-Host "Launching backend and frontend in separate windows..."
Start-Process powershell.exe -ArgumentList "-NoExit","-Command","Set-Location '$backendDir'; .\mvnw.cmd spring-boot:run" -WorkingDirectory $backendDir
Start-Process powershell.exe -ArgumentList "-NoExit","-Command","Set-Location '$frontendDir'; if (-not (Test-Path 'node_modules')) { npm install }; npm start" -WorkingDirectory $frontendDir
Write-Host "Backend and frontend starting in new windows."
