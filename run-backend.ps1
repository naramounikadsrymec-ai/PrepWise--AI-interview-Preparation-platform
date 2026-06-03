$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $scriptDir 'backend\backend'
Set-Location $backendDir
Write-Host "Starting PrepWise backend..."
if (-not (Test-Path ".\mvnw.cmd")) {
    throw "Maven wrapper not found in $backendDir"
}
& .\mvnw.cmd spring-boot:run
