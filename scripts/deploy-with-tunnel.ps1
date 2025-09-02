# 带内网穿透的一键部署脚本
# 适用于Windows环境的Java后端服务部署

param(
    [string]$TunnelType = "ngrok",  # 可选: ngrok, natapp
    [string]$AuthToken = "",
    [int]$AppPort = 8080,
    [string]$EnvFile = ".\.env"
)

Write-Host "=== Enlighten Platform 部署 with 内网穿透 ===" -ForegroundColor Cyan

# 1. 检查Docker环境
Write-Host "检查Docker环境..." -ForegroundColor Yellow
if (!(Get-Command "docker" -ErrorAction SilentlyContinue)) {
    Write-Host "错误：未找到Docker，请先安装Docker Desktop" -ForegroundColor Red
    exit 1
}

# 2. 启动后端服务
Write-Host "启动后端服务..." -ForegroundColor Yellow
if (Test-Path "scripts\deploy.sh") {
    # 使用WSL运行bash脚本
    wsl bash -c "cd /mnt/c/Users/Administrator/Desktop/enlighten-platform && ./scripts/deploy.sh -e $EnvFile"
} else {
    Write-Host "错误：未找到deploy.sh脚本" -ForegroundColor Red
    exit 1
}

# 3. 等待服务启动
Write-Host "等待服务启动（30秒）..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# 4. 检查服务状态
$testUrl = "http://localhost:$AppPort"
try {
    $response = Invoke-WebRequest -Uri $testUrl -TimeoutSec 10 -ErrorAction Stop
    Write-Host "✅ 后端服务启动成功！" -ForegroundColor Green
} catch {
    Write-Host "⚠️  服务可能还在启动中，请稍后手动检查" -ForegroundColor Yellow
}

# 5. 启动内网穿透
if ($AuthToken -eq "") {
    Write-Host "请提供AuthToken以启动内网穿透" -ForegroundColor Yellow
    Write-Host "使用方法："
    Write-Host "  ngrok: .\scripts\deploy-with-tunnel.ps1 -TunnelType ngrok -AuthToken YOUR_NGROK_TOKEN"
    Write-Host "  natapp: .\scripts\deploy-with-tunnel.ps1 -TunnelType natapp -AuthToken YOUR_NATAPP_TOKEN"
} else {
    Write-Host "启动 $TunnelType 内网穿透..." -ForegroundColor Yellow
    
    if ($TunnelType -eq "ngrok") {
        & ".\scripts\ngrok-setup.ps1" -AuthToken $AuthToken -Port $AppPort
    } elseif ($TunnelType -eq "natapp") {
        & ".\scripts\natapp-setup.ps1" -AuthToken $AuthToken -LocalPort $AppPort
    } else {
        Write-Host "不支持的穿透类型: $TunnelType" -ForegroundColor Red
    }
}

Write-Host "部署完成！" -ForegroundColor Green
