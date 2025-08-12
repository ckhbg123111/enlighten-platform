#requires -Version 5.1
<#
.SYNOPSIS
  初始化 MySQL 数据库：若不存在则根据指定 SQL 脚本创建并初始化。

.DESCRIPTION
  - 优先使用脚本参数，其次读取环境变量，最后使用默认值。
  - 依赖已安装并可用的 `mysql` 客户端程序。

.PARAMETER Host
  MySQL 主机名，默认 127.0.0.1。可用环境变量 DB_HOST 覆盖。

.PARAMETER Port
  MySQL 端口，默认 3306。可用环境变量 DB_PORT 覆盖。

.PARAMETER User
  MySQL 用户名，默认 root。可用环境变量 DB_USER 覆盖。

.PARAMETER Password
  MySQL 密码，默认空。可用环境变量 DB_PASSWORD 覆盖。

.PARAMETER Database
  目标数据库名，默认 enlighten_platform。可用环境变量 DB_NAME 覆盖。

.PARAMETER SqlFile
  初始化 SQL 文件路径，默认为 仓库根目录的 database_init.sql。可用环境变量 DB_INIT_SQL 覆盖。

.EXAMPLE
  # 使用环境变量
  # $env:DB_HOST="127.0.0.1"; $env:DB_PORT="3306"; $env:DB_USER="root"; $env:DB_PASSWORD="pass"; $env:DB_NAME="enlighten_platform"
  # powershell -ExecutionPolicy Bypass -File .\scripts\init_db.ps1

.EXAMPLE
  # 使用参数
  # powershell -ExecutionPolicy Bypass -File .\scripts\init_db.ps1 -Host 127.0.0.1 -Port 3306 -User root -Password pass -Database enlighten_platform

#>

[CmdletBinding()]
param(
  [string] $Host,
  [int]    $Port,
  [string] $User,
  [string] $Password,
  [string] $Database,
  [string] $SqlFile
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

function Get-Value {
  param(
    [string] $ParamValue,
    [string] $EnvName,
    [object] $Default
  )
  if ($PSBoundParameters.ContainsKey($MyInvocation.BoundParameters.Keys | Select-Object -First 1)) { return $ParamValue }
  if ($null -ne $ParamValue -and $ParamValue.ToString().Length -gt 0) { return $ParamValue }
  if ($env:$EnvName) { return $env:$EnvName }
  return $Default
}

function Assert-CommandExists {
  param([string] $CommandName)
  if (-not (Get-Command $CommandName -ErrorAction SilentlyContinue)) {
    throw "未找到 '$CommandName' 命令，请确保已安装 MySQL 客户端并将其加入 PATH。"
  }
}

function Test-DatabaseExists {
  param(
    [string] $Host,
    [int]    $Port,
    [string] $User,
    [string] $Password,
    [string] $Database
  )
  $passwordArg = if ([string]::IsNullOrEmpty($Password)) { "" } else { "--password=$Password" }
  $args = @(
    "--host=$Host",
    "--port=$Port",
    "--user=$User",
    $passwordArg,
    "--protocol=TCP",
    "--skip-column-names",
    "--batch",
    "-e",
    "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='$Database';"
  ) | Where-Object { $_ -ne "" }

  $result = & mysql @args 2>$null
  return ($result -replace '\r', '' -replace '\n', '').Trim().ToLower() -eq $Database.ToLower()
}

function Invoke-InitSql {
  param(
    [string] $Host,
    [int]    $Port,
    [string] $User,
    [string] $Password,
    [string] $SqlFile
  )
  if (-not (Test-Path -LiteralPath $SqlFile)) {
    throw "找不到初始化 SQL 文件：$SqlFile"
  }
  $passwordArg = if ([string]::IsNullOrEmpty($Password)) { "" } else { "--password=$Password" }
  $args = @(
    "--host=$Host",
    "--port=$Port",
    "--user=$User",
    $passwordArg,
    "--protocol=TCP"
  ) | Where-Object { $_ -ne "" }

  Write-Host "执行初始化脚本：$SqlFile ..." -ForegroundColor Cyan
  # 使用输入重定向将脚本喂给 mysql 客户端
  $mysqlExe = (Get-Command mysql).Source
  $psi = New-Object System.Diagnostics.ProcessStartInfo
  $psi.FileName = $mysqlExe
  $psi.ArgumentList.AddRange($args)
  $psi.RedirectStandardInput = $true
  $psi.RedirectStandardError = $true
  $psi.RedirectStandardOutput = $true
  $psi.UseShellExecute = $false

  $proc = New-Object System.Diagnostics.Process
  $proc.StartInfo = $psi
  $null = $proc.Start()
  Get-Content -LiteralPath $SqlFile -Encoding UTF8 | ForEach-Object { $proc.StandardInput.WriteLine($_) }
  $proc.StandardInput.Close()
  $proc.WaitForExit()

  if ($proc.ExitCode -ne 0) {
    $stderr = $proc.StandardError.ReadToEnd()
    throw "初始化失败，mysql 退出码：$($proc.ExitCode)。错误：`n$stderr"
  }
  Write-Host "初始化完成。" -ForegroundColor Green
}

# 解析参数与环境变量
$resolvedHost     = Get-Value -ParamValue $Host     -EnvName 'DB_HOST'      -Default '127.0.0.1'
$resolvedPort     = [int](Get-Value -ParamValue $Port -EnvName 'DB_PORT'      -Default 3306)
$resolvedUser     = Get-Value -ParamValue $User     -EnvName 'DB_USER'      -Default 'root'
$resolvedPassword = Get-Value -ParamValue $Password -EnvName 'DB_PASSWORD'  -Default ''
$resolvedDatabase = Get-Value -ParamValue $Database -EnvName 'DB_NAME'      -Default 'enlighten_platform'

# SQL 文件优先级：参数 > 环境变量 > 仓库根目录 database_init.sql
$repoRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$defaultSql = Join-Path -Path $repoRoot -ChildPath 'database_init.sql'
$resolvedSqlFile = Get-Value -ParamValue $SqlFile -EnvName 'DB_INIT_SQL' -Default $defaultSql

Write-Host "数据库主机: $resolvedHost" -ForegroundColor Yellow
Write-Host "数据库端口: $resolvedPort" -ForegroundColor Yellow
Write-Host "数据库用户: $resolvedUser" -ForegroundColor Yellow
Write-Host "目标数据库: $resolvedDatabase" -ForegroundColor Yellow
Write-Host "初始化脚本: $resolvedSqlFile" -ForegroundColor Yellow

Assert-CommandExists -CommandName 'mysql'

if (Test-DatabaseExists -Host $resolvedHost -Port $resolvedPort -User $resolvedUser -Password $resolvedPassword -Database $resolvedDatabase) {
  Write-Host "数据库已存在：$resolvedDatabase，无需初始化。" -ForegroundColor Green
  exit 0
}

Write-Host "数据库不存在，将执行初始化..." -ForegroundColor Cyan
Invoke-InitSql -Host $resolvedHost -Port $resolvedPort -User $resolvedUser -Password $resolvedPassword -SqlFile $resolvedSqlFile

exit 0


