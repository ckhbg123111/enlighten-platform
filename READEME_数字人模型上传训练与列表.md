# 数字人模型上传训练与列表

## 需求
两个接口实现两个功能
1. 训练模型
2. 模型列表

## 参考文档
AI 科普平台 API 文档 v1.10（0828）的 第二章的第2节 2. 获取数字模特列表 /dh/models/ 和第3节 3. 上传视频训练数字人 /dh/train/

## 要求
1. 支持训练模型，前端发起请求，后端转发请求到AI科普平台，请求成功后，保存用户与数字人model_name 的映射到数据库
2. 模型列表，配置中维持一套默认model_name,为每个用户初始的模型，改接口返回的模型包含初始模型与用户自己训练的模型，模型列表的数据来自于AI科普平台的接口 /dh/models/

## 说明
用户鉴权以及用户名获取和其他接口一样
请求AI科普平台的字段名要和API严格保持一致
暴露给前端的字段名可以自行命名，表达清楚意思即可

## 接口文档

### 1）获取数字人模型列表（返回模型详情，参照上游 v1.8 文档）
- 路由：`GET /api/dh/models`
- 鉴权：Bearer JWT（同其他受保护接口）
- 请求参数：无
- 响应（字段与上游一致，原样透传；若模型不在上游，仅返回最小对象，含 `name`）：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "models": [
      {
        "name": "模特A",
        "video": "/media/bhnw/模特A.mp4",
        "thumbnail": "/media/bhnw/模特A_thumbnail_s.jpg",
        "preview": "/media/bhnw/模特A_thumbnail.jpg"
      },
      {
        "name": "YourTrainedModel"
      }
    ]
  }
}
```

- 说明：
  - 合并逻辑保持不变（顺序与去重）：
    1. 上游接口 `/dh/models` 返回的模型详细列表；
    2. 配置项 `app.dh.default-models` 中维护的默认模型；
    3. 当前用户训练成功后在本地保存的模型；
  - 若某模型存在于默认/用户集合但不在上游列表中，则以最小对象形式返回，仅包含 `name` 字段；

### 2）上传视频训练数字人
- 路由：`POST /api/dh/train`
- 鉴权：Bearer JWT（同其他受保护接口）
- Content-Type：`application/json`
- 请求体：原样按照上游 API（AI 科普平台 `/dh/train`）的字段发送，字段名需与上游严格一致（snake_case）。
  - 注意：请求体中需包含 `model_name`，用于本地记录“用户-模型名”映射。
  - 示例仅供参考，请以上游文档为准：

```json
{
  "model_name": "YourTrainedModel",
  "video_url": "https://example.com/train.mp4"
}
```

- 响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "code": 200,
    "success": true,
    "message": "ok",
    "data": "{\"task_id\":\"abc123\"}"
  }
}
```

- 说明：
  - 本接口将请求体原样转发到上游 `/dh/train`；
  - 若上游返回成功，系统会将 `model_name` 写入本地表，建立“用户-模型名”的幂等映射；
  - 返回体 `data.data` 中为“上游 data 字段的原始 JSON 字符串”。前端如需进一步解析，可再做一次 JSON 解析；

### 鉴权
- 与现有接口一致，需携带 JWT；

### 错误码说明
- 外层 `code`/`message` 为本系统统一返回；
- `data.code`/`data.success`/`data.message` 反映上游的返回结果；

## 配置说明
- `app.upstream.dh-models-url`：上游获取模型列表接口地址（默认：`http://127.0.0.1:57599/dh/models`）
- `app.upstream.dh-train-url`：上游训练接口地址（默认：`http://127.0.0.1:57599/dh/train`）
- `app.dh.default-models`：默认模型，逗号分隔（例如：`Alice,Bob`）