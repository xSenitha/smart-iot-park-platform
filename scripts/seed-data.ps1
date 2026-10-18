# PowerShell Data Seeding Helper
param(
    [string]$GatewayUrl = "http://localhost:8080"
)

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Smart IoT Park Platform: Seeding Initial Demonstration Data" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

Write-Host "Platform baseline data initialized via Flyway migrations and Mongo init." -ForegroundColor Green
Write-Host "Demo Credentials:" -ForegroundColor Yellow
Write-Host " - Admin:       admin / Admin123!"
Write-Host " - CityPlanner: planner / Planner123!"
Write-Host " - Maintenance: maintenance / Tech123!"
