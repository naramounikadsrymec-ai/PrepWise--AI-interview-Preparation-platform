$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontendDir = Join-Path $scriptDir 'frontend'
Set-Location $frontendDir
Write-Host "Starting PrepWise frontend..."
if (-not (Test-Path "node_modules")) {
    Write-Host "Installing frontend dependencies..."
    npm install
}
npm start
