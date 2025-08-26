# 异步字幕渲染API文档

本文档描述了字幕融合服务中的异步API接口，主要包括任务提交接口和任务状态查询接口。

## 接口概览

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 提交异步任务 | POST | `/api/subtitles/burn-url-srt/async` | 提交异步字幕渲染任务 |
| 查询任务状态 | GET | `/api/subtitles/task/{taskId}` | 查询指定任务的处理状态 |

## 1. 提交异步任务接口

### 接口信息
- **URL**: `/api/subtitles/burn-url-srt/async`
- **方法**: `POST`
- **Content-Type**: `multipart/form-data`
- **描述**: 提交异步字幕渲染任务，支持视频URL和字幕文件上传

### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| taskId | String | 是 | 任务唯一标识符，用于后续查询任务状态 |
| videoUrl | String | 是 | 视频文件的URL地址（必须以http://或https://开头） |
| subtitleFile | MultipartFile | 是 | 字幕文件（仅支持.srt格式） |

### 请求示例

```bash
curl -X POST "http://localhost:8081/api/subtitles/burn-url-srt/async" \
  -F "taskId=task-123456" \
  -F "videoUrl=https://example.com/video.mp4" \
  -F "subtitleFile=@subtitle.srt"
```

### 响应格式

```json
{
  "taskId": "task-123456",
  "state": "PENDING",
  "message": "任务已创建，等待处理",
  "outputUrl": null,
  "errorMessage": null,
  "createTime": "2024-01-15 10:30:00",
  "updateTime": "2024-01-15 10:30:00",
  "progress": 0
}
```

### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| taskId | String | 任务ID |
| state | String | 任务状态（见任务状态说明） |
| message | String | 任务描述信息 |
| outputUrl | String | 输出视频的URL（任务完成后才有值） |
| errorMessage | String | 错误信息（任务失败时才有值） |
| createTime | String | 任务创建时间 |
| updateTime | String | 任务最后更新时间 |
| progress | Integer | 任务进度（0-100） |

### 错误响应示例

```json
{
  "taskId": "task-123456",
  "state": "PENDING",
  "message": "任务ID已存在，请使用不同的taskId",
  "outputUrl": null,
  "errorMessage": null,
  "createTime": "2024-01-15 10:30:00",
  "updateTime": "2024-01-15 10:30:00",
  "progress": 0
}
```

## 2. 查询任务状态接口

### 接口信息
- **URL**: `/api/subtitles/task/{taskId}`
- **方法**: `GET`
- **描述**: 查询指定任务的处理状态和进度

### 路径参数

| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| taskId | String | 是 | 任务ID |

### 请求示例

```bash
curl -X GET "http://localhost:8081/api/subtitles/task/task-123456"
```

### 响应格式

#### 任务处理中
```json
{
  "taskId": "task-123456",
  "state": "PROCESSING",
  "message": "字幕渲染中",
  "outputUrl": null,
  "errorMessage": null,
  "createTime": "2024-01-15 10:30:00",
  "updateTime": "2024-01-15 10:32:15",
  "progress": 45
}
```

#### 任务完成
```json
{
  "taskId": "task-123456",
  "state": "COMPLETED",
  "message": "处理完成",
  "outputUrl": "http://minio-server:9000/videos/output-task-123456.mp4",
  "errorMessage": null,
  "createTime": "2024-01-15 10:30:00",
  "updateTime": "2024-01-15 10:35:20",
  "progress": 100
}
```

#### 任务失败
```json
{
  "taskId": "task-123456",
  "state": "FAILED",
  "message": "处理失败: 视频下载失败",
  "outputUrl": null,
  "errorMessage": "视频下载失败",
  "createTime": "2024-01-15 10:30:00",
  "updateTime": "2024-01-15 10:31:30",
  "progress": 0
}
```

#### 任务不存在
```json
{
  "taskId": "task-unknown",
  "state": "PENDING",
  "message": "任务不存在",
  "outputUrl": null,
  "errorMessage": null,
  "createTime": "2024-01-15 10:30:00",
  "updateTime": "2024-01-15 10:30:00",
  "progress": 0
}
```

## 任务状态说明

| 状态 | 描述 |
|------|------|
| PENDING | 等待处理 |
| DOWNLOADING | 下载视频中 |
| PROCESSING | 字幕渲染中 |
| UPLOADING | 上传到MinIO中 |
| COMPLETED | 处理完成 |
| FAILED | 处理失败 |

## 使用流程

1. **提交任务**: 调用 `/burn-url-srt/async` 接口提交异步任务
2. **轮询状态**: 定期调用 `/task/{taskId}` 接口查询任务状态
3. **获取结果**: 当任务状态为 `COMPLETED` 时，从 `outputUrl` 字段获取处理后的视频URL

## 注意事项

1. **taskId唯一性**: 每个任务的taskId必须唯一，重复的taskId会被拒绝
2. **字幕格式**: 目前仅支持.srt格式的字幕文件
3. **视频URL**: 视频URL必须是有效的HTTP/HTTPS地址
4. **轮询频率**: 建议每5-10秒轮询一次任务状态，避免过于频繁的请求
5. **任务清理**: 系统会自动清理临时文件，完成的任务信息会保留一段时间

## 错误码说明

- 当任务提交失败时，会在 `message` 字段返回具体的错误信息
- 当任务处理失败时，会在 `errorMessage` 字段返回详细的错误信息
- 常见错误包括：
    - "taskId 不能为空"
    - "videoUrl 不能为空"
    - "字幕文件不能为空"
    - "任务ID已存在，请使用不同的taskId"
    - "无效的URL格式"
    - "仅支持 .srt 字幕格式"
    - "任务不存在"

## 示例代码

### JavaScript/Node.js 示例

```javascript
// 提交任务
async function submitTask(taskId, videoUrl, subtitleFile) {
  const formData = new FormData();
  formData.append('taskId', taskId);
  formData.append('videoUrl', videoUrl);
  formData.append('subtitleFile', subtitleFile);
  
  const response = await fetch('/api/subtitles/burn-url-srt/async', {
    method: 'POST',
    body: formData
  });
  
  return await response.json();
}

// 查询任务状态
async function getTaskStatus(taskId) {
  const response = await fetch(`/api/subtitles/task/${taskId}`);
  return await response.json();
}

// 轮询任务状态直到完成
async function waitForTaskCompletion(taskId) {
  while (true) {
    const status = await getTaskStatus(taskId);
    
    if (status.state === 'COMPLETED') {
      console.log('任务完成，输出URL:', status.outputUrl);
      return status;
    } else if (status.state === 'FAILED') {
      console.error('任务失败:', status.errorMessage);
      return status;
    }
    
    console.log(`任务进度: ${status.progress}%, 状态: ${status.state}`);
    await new Promise(resolve => setTimeout(resolve, 5000)); // 等待5秒
  }
}
```

### Python 示例

```python
import requests
import time

def submit_task(task_id, video_url, subtitle_file_path):
    """提交异步任务"""
    with open(subtitle_file_path, 'rb') as f:
        files = {'subtitleFile': f}
        data = {
            'taskId': task_id,
            'videoUrl': video_url
        }
        response = requests.post('/api/subtitles/burn-url-srt/async', 
                               files=files, data=data)
    return response.json()

def get_task_status(task_id):
    """查询任务状态"""
    response = requests.get(f'/api/subtitles/task/{task_id}')
    return response.json()

def wait_for_completion(task_id):
    """等待任务完成"""
    while True:
        status = get_task_status(task_id)
        
        if status['state'] == 'COMPLETED':
            print(f"任务完成，输出URL: {status['outputUrl']}")
            return status
        elif status['state'] == 'FAILED':
            print(f"任务失败: {status['errorMessage']}")
            return status
            
        print(f"任务进度: {status['progress']}%, 状态: {status['state']}")
        time.sleep(5)  # 等待5秒
```

---

更多信息请参考完整的API文档或联系开发团队。
