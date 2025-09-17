# MyBatis-Plus 使用指南

## 项目配置

### 1. 数据库配置
项目已配置连接到 `192.168.3.10:3306` 数据库，用户名：`ccc`，密码：`143333`

### 2. 依赖配置
- MyBatis-Plus: 3.5.12
- MySQL驱动: 8.0.33
- Lombok: 1.18.30

## 数据库初始化

### 1. 执行SQL脚本
运行 `database_init.sql` 脚本来创建数据库和表结构：

```sql
-- 连接到MySQL服务器
mysql -h 192.168.3.10 -u ccc -p143333

-- 执行脚本
source database_init.sql
```

### 2. 验证数据库连接
启动应用后，访问：`GET /api/user/test-connection`

## API接口说明

### 用户管理接口

#### 1. 测试数据库连接
```
GET /api/user/test-connection
```

#### 2. 创建用户
```
POST /api/user/create
Content-Type: application/json

{
    "username": "newuser",
    "password": "123456",
    "email": "newuser@example.com",
    "phone": "13800138004",
    "status": 1
}
```

#### 3. 更新用户
```
PUT /api/user/update
Content-Type: application/json

{
    "id": 1,
    "email": "updated@example.com",
    "phone": "13800138005",
    "status": 0
}
```

#### 4. 删除用户
```
DELETE /api/user/{id}
```

#### 5. 根据ID查询用户
```
GET /api/user/{id}
```

#### 6. 分页查询用户
```
GET /api/user/page?current=1&size=10&username=test&status=1
```

#### 7. 查询所有用户
```
GET /api/user/list
```

#### 8. 根据状态查询用户
```
GET /api/user/status/{status}
```

#### 9. 批量更新用户状态
```
PUT /api/user/batch-status?ids=1,2,3&status=0
```

## 测试示例

### 1. 启动应用
```bash
mvn spring-boot:run -pl enlighten-platform-web
```

### 2. 测试数据库连接
```bash
curl http://localhost:8080/api/user/test-connection
```

### 3. 创建用户
```bash
curl -X POST http://localhost:8080/api/user/create \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "phone": "13800138006"
  }'
```

### 4. 查询用户列表
```bash
curl http://localhost:8080/api/user/list
```

### 5. 分页查询
```bash
curl "http://localhost:8080/api/user/page?current=1&size=5"
```

## 特性说明

### 1. MyBatis-Plus特性
- **自动CRUD**: 继承 `BaseMapper<T>` 获得基础的增删改查功能
- **条件构造器**: 使用 `LambdaQueryWrapper` 和 `LambdaUpdateWrapper` 构建查询条件
- **分页插件**: 内置分页功能，支持 `Page<T>` 对象
- **逻辑删除**: 支持软删除，不会真正删除数据
- **自动填充**: 自动填充创建时间和更新时间

### 2. 数据验证
- 使用 `@Validated` 注解进行参数验证
- 用户名唯一性检查
- 邮箱和手机号格式验证

### 3. 统一响应格式
所有API都返回统一的 `Result<T>` 格式：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {...}
}
```

## 注意事项

1. **数据库连接**: 确保MySQL服务正在运行，且网络连接正常
2. **端口配置**: 默认使用8080端口，如需修改请在 `application.properties` 中配置
3. **字符编码**: 数据库使用UTF8MB4编码，支持emoji等特殊字符
4. **时区设置**: 数据库连接使用Asia/Shanghai时区

## 扩展功能

如需添加更多功能，可以：
1. 在 `UserService` 中添加新的业务方法
2. 在 `UserController` 中添加对应的API接口
3. 创建新的实体类和对应的Mapper、Service、Controller
