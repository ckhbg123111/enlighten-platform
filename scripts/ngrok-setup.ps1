# ngrok Windows 安装和配置脚本
# 使用方法：在PowerShell中运行此脚本

param(
    [Parameter(Mandatory=$true)]
    [string]$AuthToken,
    [int]$Port = 8080
)

Write-Host "开始配置ngrok内网穿透..." -ForegroundColor Green

# 检查ngrok是否已安装
if (!(Get-Command "ngrok" -ErrorAction SilentlyContinue)) {
    Write-Host "请先下载并安装ngrok："
    Write-Host "1. 访问 https://ngrok.com/download"
    Write-Host "2. 下载Windows版本"
    Write-Host "3. 解压并将ngrok.exe添加到PATH环境变量"
    Write-Host "4. 重新运行此脚本"
    exit 1
}

# 配置认证token
Write-Host "配置认证token..." -ForegroundColor Yellow
ngrok authtoken $AuthToken

# 启动穿透
Write-Host "启动内网穿透，映射端口 $Port ..." -ForegroundColor Yellow
Write-Host "按 Ctrl+C 停止穿透"
ngrok http $Port

Write-Host "穿透服务已启动！请查看上方显示的公网URL" -ForegroundColor Green
