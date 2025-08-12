# Enlighten Platform 多模块工程

这是一个基于Spring Boot的多模块Maven工程，采用分层架构设计。

## 项目结构

```
enlighten-platform/
├── enlighten-platform-api/          # API模块 - 定义接口和DTO
├── enlighten-platform-biz/          # 业务逻辑模块 - 实现业务逻辑
├── enlighten-platform-web/          # Web应用模块 - 提供Web服务
└── pom.xml                         # 父模块POM文件
```

## 模块说明

### enlighten-platform-api
- **作用**: 定义系统接口、数据传输对象(DTO)、常量等
- **依赖**: Spring Boot Starter
- **打包方式**: jar

### enlighten-platform-biz  
- **作用**: 实现业务逻辑，包含服务层和业务规则
- **依赖**: enlighten-platform-api
- **打包方式**: jar

### enlighten-platform-web
- **作用**: Web应用入口，提供REST API和Web界面
- **依赖**: enlighten-platform-biz
- **打包方式**: jar (可执行jar)

## 构建和运行

### 构建整个项目
```bash
mvn clean install
```

### 构建特定模块
```bash
# 构建API模块
mvn clean install -pl enlighten-platform-api

# 构建业务模块
mvn clean install -pl enlighten-platform-biz

# 构建Web模块
mvn clean install -pl enlighten-platform-web
```

### 运行Web应用
```bash
cd enlighten-platform-web
mvn spring-boot:run
```

## 依赖关系

```
web → biz → api
```

- web模块依赖biz模块
- biz模块依赖api模块
- api模块为基础模块，不依赖其他内部模块

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.5.4
- **Maven**: 3.x
- **构建工具**: Maven

## 开发规范

1. 新增功能时，优先考虑在合适的模块中实现
2. 模块间依赖要遵循分层原则，避免循环依赖
3. 公共依赖在父模块中统一管理
4. 各模块的版本号由父模块统一管理
