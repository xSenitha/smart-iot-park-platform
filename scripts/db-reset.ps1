# PowerShell Database Reset Helper
Write-Host "Resetting Smart IoT Park Platform databases and volumes..." -ForegroundColor Yellow
docker compose down -v --remove-orphans
Write-Host "Volumes removed. Restart with 'make up' or 'docker compose up -d'." -ForegroundColor Green
