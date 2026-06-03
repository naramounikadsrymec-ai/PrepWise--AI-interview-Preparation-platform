Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process -Force
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $scriptDir 'backend\backend'
$frontendDir = Join-Path $scriptDir 'frontend'

function Fail([string]$message) {
    Write-Error $message
    exit 1
}

Write-Host "Preparing PrepWise startup..."
Write-Host "Backend path: $backendDir"
Write-Host "Frontend path: $frontendDir"

if (-not (Test-Path "$backendDir\mvnw.cmd")) {
    Fail "Maven wrapper not found in $backendDir. Ensure the repository has backend\backend\mvnw.cmd."
}

if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    Fail "Node.js is not installed or not on PATH. Install Node.js before running this script."
}

# Ensure frontend dependencies are installed
if (-not (Test-Path "$frontendDir\node_modules")) {
    Write-Host "Installing frontend dependencies..."
    Push-Location $frontendDir
    npm install
    Pop-Location
}

Write-Host "Launching backend and frontend in separate windows..."

$backendCmd = "cd /d `"$backendDir`" && .\mvnw.cmd spring-boot:run"
$frontendCmd = "cd /d `"$frontendDir`" && npm start"

Start-Process -FilePath cmd.exe -ArgumentList '/k', $backendCmd -WorkingDirectory $backendDir -WindowStyle Normal
Start-Process -FilePath cmd.exe -ArgumentList '/k', $frontendCmd -WorkingDirectory $frontendDir -WindowStyle Normal

Write-Host "PrepWise startup initiated. Backend and frontend windows should now open."
