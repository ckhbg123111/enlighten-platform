# Windows 内网穿透部署指南

本指南帮助您在Windows环境下部署Enlighten Platform后端服务，并通过内网穿透让外网用户访问。

## 前置要求

- Windows 10/11
- Docker Desktop for Windows
- PowerShell 5.0+
- WSL2（推荐）

## 快速开始

### 1. 克隆并准备项目
```powershell
cd C:\Users\Administrator\Desktop\enlighten-platform
```

### 2. 配置环境变量
复制 `env.sample` 为 `.env` 并修改配置：
```powershell
copy env.sample .env
# 编辑 .env 文件，设置数据库密码等
```

### 3. 选择内网穿透方案

#### 方案A：使用ngrok（推荐）
1. 访问 [ngrok.com](https://ngrok.com) 注册账号
2. 获取AuthToken
3. 下载Windows版本到项目目录
4. 运行一键部署：
```powershell
.\scripts\deploy-with-tunnel.ps1 -TunnelType ngrok -AuthToken YOUR_NGROK_TOKEN -AppPort 8080
```

#### 方案B：使用NATAPP（国内用户）
1. 访问 [natapp.cn](https://natapp.cn) 注册账号
2. 创建免费隧道获取AuthToken
3. 下载客户端到项目目录
4. 运行部署：
```powershell
.\scripts\deploy-with-tunnel.ps1 -TunnelType natapp -AuthToken YOUR_NATAPP_TOKEN -AppPort 8080
```

#### 方案C：手动分步部署
```powershell
# 1. 启动后端服务
wsl bash -c "cd /mnt/c/Users/Administrator/Desktop/enlighten-platform && ./scripts/deploy.sh"

# 2. 等待服务启动（约30-60秒）

# 3. 启动ngrok穿透
.\scripts\ngrok-setup.ps1 -AuthToken YOUR_TOKEN -Port 8080
```

## 服务验证

### 本地验证
```powershell
# 检查Docker容器状态
docker ps

# 检查应用健康状态
curl http://localhost:8080/actuator/health

# 查看应用日志
docker logs enlighten-app -f
```

### 外网访问验证
使用穿透工具提供的公网URL进行测试：
```
https://your-random-id.ngrok.io/actuator/health
```

## 常见问题

### 1. Docker启动失败
- 检查Docker Desktop是否运行
- 确认WSL2已启用
- 检查端口是否被占用

### 2. 穿透连接失败
- 验证AuthToken是否正确
- 检查防火墙设置
- 确认服务已启动

### 3. 数据库连接问题
- 检查数据库密码配置
- 确认MySQL容器健康状态
- 查看应用日志

## 高级配置

### 自定义端口
修改 `.env` 文件中的 `APP_PORT` 变量：
```bash
APP_PORT=9090
```

### HTTPS配置
ngrok免费版自动提供HTTPS，无需额外配置。

### 域名绑定（付费版）
```bash
# ngrok付费版可绑定自定义域名
ngrok http 8080 --hostname=your-domain.com
```

## 安全建议

1. **定期更新JWT密钥**：修改`.env`中的`APP_JWT_SECRET`
2. **数据库安全**：使用强密码并定期备份
3. **访问控制**：考虑在应用层添加IP白名单
4. **监控日志**：定期检查访问日志

## 生产环境部署

对于生产环境，建议：
1. 使用云服务器而非内网穿透
2. 配置负载均衡和反向代理
3. 启用数据库读写分离
4. 实施完整的监控和告警系统

## 技术支持

如遇问题，请检查：
1. Docker容器日志
2. 应用程序日志
3. 穿透工具连接状态
4. 网络连接状态
