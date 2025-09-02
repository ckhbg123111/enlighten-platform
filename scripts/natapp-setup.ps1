# NATAPP Windows 配置脚本
# 使用方法：在PowerShell中运行此脚本

param(
    [Parameter(Mandatory=$true)]
    [string]$AuthToken,
    [int]$LocalPort = 8080
)

Write-Host "开始配置NATAPP内网穿透..." -ForegroundColor Green

# 检查是否已下载natapp
$natappPath = ".\natapp.exe"
if (!(Test-Path $natappPath)) {
    Write-Host "请先下载NATAPP客户端："
    Write-Host "1. 访问 https://natapp.cn/"
    Write-Host "2. 注册账号并获取免费隧道"
    Write-Host "3. 下载Windows客户端到当前目录"
    Write-Host "4. 重新运行此脚本"
    exit 1
}

# 创建配置文件
$configContent = @"
#将本文件放置于natapp同级目录 程序将读取 [default] 段
#在命令行参数模式如 natapp -authtoken=xxx 等相同参数将会覆盖掉此配置
#命令行参数 -config= 可以指定任意config.ini文件
[default]
authtoken=$AuthToken
clienttoken=
log=none
loglevel=ERROR
http_proxy=
"@

$configContent | Out-File -FilePath "config.ini" -Encoding utf8

Write-Host "配置文件已创建：config.ini" -ForegroundColor Yellow
Write-Host "启动NATAPP穿透..." -ForegroundColor Yellow

# 启动natapp
& $natappPath

Write-Host "NATAPP穿透服务已启动！" -ForegroundColor Green
