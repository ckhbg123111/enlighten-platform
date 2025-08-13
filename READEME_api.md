## 平台接口文档（本地环境）

基地址：`http://localhost:8080`

### 认证与鉴权
- 除以下接口外，其余 `/api/**` 默认需要携带 JWT：
  - `/api/auth/login`
  - `/api/user/create`
  - `/actuator/health`
- 认证方式：在请求头添加
  - `Authorization: Bearer <JWT_TOKEN>`
- 令牌签名算法：HS256；有效期默认 7 天（`app.jwt.expire-seconds=604800`）。

### 通用返回结构
- 大多数接口返回：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```
- 媒体转换相关接口（`/api/convert2media/**`）返回：
```json
{
  "code": 200,
  "success": true,
  "msg": "OK",
  "data": { /* 上游原样数据或null */ }
}
```
- SSE 流式接口（`/api/science-generator`、`/api/fill_in`）返回 `text/event-stream`，按行推送：
```
data: {json片段}
data: {json片段}
...
```
客户端需逐行解析并拼接模型输出（通常在 `choices[0].delta.content` 字段）。

---

## 接口清单

### 1. 创建用户
- 路径：`POST /api/user/create`
- 认证：无需
- 请求头：`Content-Type: application/json`
- 请求体：
```json
{
  "username": "test",
  "password": "123456",
  "email": "string",
  "phone": "string",
  "status": 1,
  "role": "ADMIN|USER",
  "tenantId": 1
}
```
- 返回体（`data` 为用户信息，密码不返回）：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 6,
    "username": "test",
    "email": null,
    "phone": null,
    "status": 1,
    "role": "USER",
    "lastLoginTime": null,
    "tenantId": null,
    "createTime": "2025-08-13T16:21:18",
    "updateTime": "2025-08-13T16:21:18"
  }
}
```

### 2. 登录
- 路径：`POST /api/auth/login`
- 认证：无需
- 请求头：`Content-Type: application/json`
- 请求体：
```json
{
  "username": "test",
  "password": "123456"
}
```
- 返回体（`data.token` 为 JWT）：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "<JWT_TOKEN>"
  }
}
```

### 3. 获取文章选项
- 路径：`GET /api/article/options`
- 认证：需要（Bearer Token）
- 返回体（`data` 为分类到选项数组的映射，分类固定为 `style|length|mode|scene`）：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "style": [
      { "id": 1, "category": "style", "optionName": "专业严谨", "optionCode": "...", "sort": 0, "createTime": "...", "updateTime": "..." }
    ],
    "length": [ /* 同上结构 */ ],
    "mode": [ /* 同上结构 */ ],
    "scene": [ /* 同上结构 */ ]
  }
}
```

对象 `ArticleOptionVO` 字段：`id, category, optionName, optionCode, sort, createTime, updateTime`。

### 4. 表单自动填充（SSE）
- 路径：`POST /api/fill_in`
- 认证：需要
- 请求头：
  - `Content-Type: application/json`
  - `Accept: text/event-stream`
- 请求体：
```json
{
  "content": "风寒感冒"
}
```
- 响应：`text/event-stream` 流。每行以 `data: ` 开头，内容为 JSON 片段；客户端需逐行解析并填充到对应表单字段（上游可能包含 `<FIELD:...>` 与 `<SEP>` 标记）。

### 5. 生成科普内容（SSE）
- 路径：`POST /api/science-generator`
- 认证：需要
- 请求头：
  - `Content-Type: application/json`
  - `Accept: text/event-stream`
- 请求体（必填项已标注）：
```json
{
  "code": "uuid_example_test_1",            // 必填，平台侧唯一编码（仅记录，不透传上游）
  "topic": "感冒的早期识别与科学应对策略",   // 必填
  "content": "...",                         // 必填
  "outline": "...",
  "case": "...",                            // 可通过字段名 case 传入
  "style": "专业严谨",                       // 必填
  "length": "中",                           // 必填
  "mode": "常规文章",                        // 必填
  "scene": "日常科普",                       // 必填
  "document_id": "kb_xxx",                 // 可选
  "contains_image": true                     // 可选
}
```
- 响应：`text/event-stream` 流。逐行 `data: {json}` 推送，内容与上游一致，客户端拼接 `choices[0].delta.content` 以获得完整文章。

### 6. 转换为媒体图文（通用）
- 路径：`POST /api/convert2media/common`
- 认证：需要
- 请求头：`Content-Type: application/json`
- 请求体：
```json
{
  "content": "原文内容（可为纯文/Markdown/HTML）",
  "platform": "douyin",             // 枚举：xiaohongshu | douyin
  "essayCode": "uuid_example_test_1",
  "mediaCode": "media_uuid_example_test_1"
}
```
- 返回体（HTTP 200，业务码见 `code`）：
```json
{
  "code": 200,
  "success": true,
  "msg": "OK",
  "data": { /* 上游返回的结构，原样透传 */ }
}
```

### 7. 转公众号（重新生成）
- 路径：`POST /api/convert2media/convert2gzh_re`
- 认证：需要
- 请求头：`Content-Type: application/json`
- 请求体：
```json
{
  "content": "<h1>...</h1>",
  "essayCode": "uuid_example_test_1",
  "mediaCode": "media_uuid_example_test_2"
}
```
- 返回体：同“媒体图文（通用）”。

### 8. 查询媒体转换记录（按文章编码）
- 路径：`GET /api/convert2media/records?essayCode={code}`
- 认证：需要
- 返回体：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "code": "auto_gen_code",
      "userId": 6,
      "tenantId": 1,
      "essayCode": "uuid_example_test_1",
      "content": "...",
      "platform": "douyin",
      "respCode": 200,
      "respMsg": "OK",
      "respSuccess": true,
      "respData": "{...}",
      "success": true,
      "errorMessage": null,
      "deleted": 0,
      "createTime": "2025-08-13T16:25:56",
      "updateTime": "2025-08-13T16:25:56",
      "deleteTime": null
    }
  ]
}
```
记录对象字段见 `media_convert_record` 表映射：`id, code, userId, tenantId, essayCode, content, platform, respCode, respMsg, respSuccess, respData, success, errorMessage, deleted, createTime, updateTime, deleteTime`。

### 9. 删除媒体转换记录（软删）
- 路径：`DELETE /api/convert2media/{id}`
- 认证：需要
- 返回体：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

## 错误码约定（常见）
- 200：成功
- 400：参数不合法（如平台枚举非法等）
- 401：未认证（`Authorization` 缺失或无效）
- 403：无权限/用户被禁用
- 404：资源不存在（如记录不存在）
- 500：服务器内部错误

## 示例请求（快速校验）
```http
POST /api/user/create
Content-Type: application/json

{"username":"test","password":"123456"}
```
```http
POST /api/auth/login
Content-Type: application/json

{"username":"test","password":"123456"}
```
```http
GET /api/article/options
Authorization: Bearer <JWT>
```
```http
POST /api/fill_in
Authorization: Bearer <JWT>
Content-Type: application/json

{"content":"风寒感冒"}
```
```http
POST /api/science-generator
Authorization: Bearer <JWT>
Content-Type: application/json

{"code":"uuid_example_test_1","topic":"...","content":"...","style":"专业严谨","length":"中","mode":"常规文章","scene":"日常科普","contains_image":true}
```
```http
POST /api/convert2media/common
Authorization: Bearer <JWT>
Content-Type: application/json

{"content":"...","platform":"douyin","essayCode":"uuid_example_test_1","mediaCode":"media_uuid_example_test_1"}
```
```http
POST /api/convert2media/convert2gzh_re
Authorization: Bearer <JWT>
Content-Type: application/json

{"content":"<h1>...</h1>","essayCode":"uuid_example_test_1","mediaCode":"media_uuid_example_test_2"}
```
```http
GET /api/convert2media/records?essayCode=uuid_example_test_1
Authorization: Bearer <JWT>
```


