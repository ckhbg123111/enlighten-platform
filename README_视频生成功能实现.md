# 视频生成功能实现说明

## 功能概述

本系统实现了完整的视频生成功能，包括数字人视频生成和字幕烧录两个阶段。系统采用异步任务处理架构，支持分布式部署。

## 架构设计

### 核心组件

1. **VideoGenerationTask** - 视频生成任务实体
2. **VideoGenerationService** - 视频生成核心服务
3. **VideoTaskWorkerService** - 异步任务处理器
4. **VideoGenerationController** - 对外API接口

### 技术栈

- **持久化**: MySQL + MyBatis-Plus + Flyway
- **异步处理**: Spring @Async + @Scheduled
- **HTTP客户端**: Java 11 HttpClient
- **线程池**: ThreadPoolTaskExecutor

## 数据库设计

### 视频生成任务表 (video_generation_task)

```sql
CREATE TABLE video_generation_task (
    id VARCHAR(36) NOT NULL COMMENT '主键ID (UUID)',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    input_text TEXT NOT NULL COMMENT '输入文本',
    model_name VARCHAR(100) COMMENT '数字人模型名称',
    voice VARCHAR(50) DEFAULT 'Female_Voice_1' COMMENT '语音类型',
    
    dh_task_id VARCHAR(100) COMMENT '数字人任务ID',
    dh_status VARCHAR(50) COMMENT '数字人任务状态',
    dh_result_url TEXT COMMENT '数字人生成的视频URL',
    audio_data LONGTEXT COMMENT '数字人返回的音频数据(JSON格式)',
    
    burn_task_id VARCHAR(100) COMMENT '字幕烧录任务ID',
    burn_status VARCHAR(50) COMMENT '字幕烧录状态',
    output_url TEXT COMMENT '最终输出的带字幕视频URL',
    
    progress INT DEFAULT 0 COMMENT '任务进度 (0-100)',
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED' COMMENT '任务状态',
    error_message TEXT COMMENT '错误信息',
    
    version INT DEFAULT 0 COMMENT '版本号(乐观锁)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除字段',
    
    PRIMARY KEY (id),
    INDEX idx_user_tenant (user_id, tenant_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    UNIQUE INDEX uk_dh_task_id (dh_task_id),
    UNIQUE INDEX uk_burn_task_id (burn_task_id)
);
```

## 状态流转

```
CREATED → DH_PROCESSING → DH_DONE → BURN_PROCESSING → COMPLETED
                ↓               ↓              ↓
              FAILED          FAILED        FAILED
```

### 状态说明

- **CREATED**: 任务已创建，等待处理
- **DH_PROCESSING**: 数字人视频生成中
- **DH_DONE**: 数字人视频生成完成，等待字幕烧录
- **BURN_PROCESSING**: 字幕烧录中
- **COMPLETED**: 任务完成
- **FAILED**: 任务失败

### 进度映射

- CREATED: 0%
- DH_PROCESSING: 5-60% (根据数字人接口返回的进度映射)
- DH_DONE: 60%
- BURN_PROCESSING: 65-99% (根据字幕平台进度映射)
- COMPLETED: 100%

## API接口

### 1. 创建视频生成任务

```http
POST /api/video/generate
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "text": "这里是要生成视频的文本内容",
  "modelName": "数字人模型名称（可选）",
  "voice": "Female_Voice_1"
}
```

**响应**:
```json
{
  "code": 200,
  "success": true,
  "msg": "操作成功",
  "data": {
    "taskId": "550e8400-e29b-41d4-a716-446655440000",
    "message": "视频生成任务创建成功"
  }
}
```

### 2. 查询任务状态

```http
GET /api/video/status/{taskId}
Authorization: Bearer {jwt_token}
```

**响应**:
```json
{
  "code": 200,
  "success": true,
  "msg": "操作成功",
  "data": {
    "status": "DH_PROCESSING",
    "progress": 25,
    "resultUrl": null,
    "message": "数字人视频生成中...",
    "createdAt": "2024-01-15 10:30:00",
    "updatedAt": "2024-01-15 10:32:00"
  }
}
```

### 3. 下载视频结果

```http
GET /api/video/download/{taskId}
Authorization: Bearer {jwt_token}
```

成功时会重定向(302)到实际的视频URL。

## 异步任务处理

### 定时任务调度

1. **处理新任务** - 每10秒执行，处理CREATED状态的任务
2. **轮询数字人状态** - 每5秒执行，轮询DH_PROCESSING状态的任务
3. **处理数字人完成任务** - 每10秒执行，处理DH_DONE状态的任务
4. **轮询字幕烧录状态** - 每5秒执行，轮询BURN_PROCESSING状态的任务
5. **清理超时任务** - 每30分钟执行，清理超时任务

### 线程池配置

- **视频任务线程池**: 核心5线程，最大20线程，队列容量100
- **通用异步线程池**: 核心3线程，最大10线程，队列容量50

## 配置说明

### application.properties 新增配置

```properties
# 视频生成相关接口配置
app.upstream.dh-generate-url=http://frp5.mmszxc.xin:57599/dh/generate
app.upstream.dh-status-url=http://frp5.mmszxc.xin:57599/dh/status
app.upstream.subtitle-burn-url=http://localhost:8080/api/subtitles/burn-url-srt/async
app.upstream.subtitle-status-url=http://localhost:8080/api/subtitles/task
```

## 部署说明

### 1. 数据库迁移

系统启动时会自动执行Flyway迁移脚本 `V17__create_video_generation_task.sql`。

### 2. 依赖服务

确保以下外部服务可访问：
- AI科普平台数字人接口
- 视频字幕合成平台接口

### 3. 监控要点

- 任务堆积情况
- 异常任务比例
- 第三方接口响应时间
- 线程池使用情况

## 使用示例

### 前端调用示例

```javascript
// 1. 创建视频生成任务
const createResponse = await fetch('/api/video/generate', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify({
    text: '人工智能是计算机科学的一个分支，它致力于创建能够执行通常需要人类智能的任务的系统。',
    voice: 'Female_Voice_1'
  })
});

const { data: { taskId } } = await createResponse.json();

// 2. 轮询任务状态
const pollStatus = async () => {
  const response = await fetch(`/api/video/status/${taskId}`, {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  
  const { data } = await response.json();
  
  if (data.status === 'COMPLETED') {
    // 任务完成，可以下载视频
    window.open(`/api/video/download/${taskId}`);
  } else if (data.status === 'FAILED') {
    console.error('任务失败:', data.message);
  } else {
    // 继续轮询
    console.log(`进度: ${data.progress}% - ${data.message}`);
    setTimeout(pollStatus, 3000); // 3秒后再次轮询
  }
};

pollStatus();
```

## 故障排查

### 常见问题

1. **任务一直在CREATED状态**
   - 检查异步任务处理器是否正常运行
   - 检查数字人接口是否可访问

2. **数字人任务失败**
   - 检查输入文本是否符合要求
   - 检查数字人接口配置和网络连通性

3. **字幕烧录失败**
   - 检查字幕合成平台接口配置
   - 检查SRT文件生成是否正确

### 日志查看

关键日志前缀：
- `VideoGenerationServiceImpl`: 服务层操作日志
- `VideoTaskWorkerService`: 异步任务处理日志
- `VideoGenerationController`: API调用日志

## 管理员功能

### 手动触发任务处理

```http
POST /api/video/admin/process/{taskId}
Authorization: Bearer {admin_jwt_token}
```

可用于重试失败的任务或手动推进卡住的任务。

## 后续扩展

1. **任务优先级**: 支持VIP用户优先处理
2. **批量处理**: 支持批量创建和处理任务
3. **任务统计**: 提供详细的任务统计和监控面板
4. **自动重试**: 对于网络异常等临时性错误自动重试
5. **任务取消**: 支持用户取消正在进行的任务

## 注意事项

1. **幂等性**: 系统通过数字人任务ID和字幕烧录任务ID的唯一约束保证幂等性
2. **乐观锁**: 使用版本号字段防止并发更新冲突
3. **资源清理**: SRT临时文件会在使用后自动删除
4. **安全性**: 所有接口都需要JWT认证，支持多租户隔离
5. **性能**: 建议监控任务处理延迟，必要时调整轮询频率和线程池大小
