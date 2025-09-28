param(
    [string]$EnvFile = "./.env",
    [switch]$Build = $true
)

Write-Host "[Enlighten] Starting deployment with Docker Compose..." -ForegroundColor Cyan

if (!(Test-Path $EnvFile)) {
    if (Test-Path "./env") { $EnvFile = "./env" } 
    elseif (Test-Path "./env.sample") { $EnvFile = "./env.sample" } 
    else { Write-Error "Env file not found. Provide via -EnvFile or create ./.env"; exit 1 }
}

Write-Host "Using env file: $EnvFile"

$compose = "docker-compose.yml"
if (!(Test-Path $compose)) { Write-Error "docker-compose.yml not found"; exit 1 }

if ($Build) {
    docker compose --env-file $EnvFile build | cat
}

docker compose --env-file $EnvFile up -d | cat

Write-Host "[Enlighten] Done. Logs are written to .\\logs\\app\\app.log (hourly rotation)." -ForegroundColor Green
Write-Host "[Enlighten] Follow logs on host: Get-Content .\\logs\\app\\app.log -Tail 200 -Wait" -ForegroundColor Green
Write-Host "[Enlighten] Or inside container: docker exec -it enlighten-app sh -c 'tail -F /var/log/enlighten/app.log'" -ForegroundColor Green


