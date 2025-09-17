# 生成视频功能

本项目为后端服务


## 需求介绍
1. 前端发起请求，传入一段文本
2. 后端调用第三方接口生成视频
3. 后端拿到返回的视频地址和字幕信息将字幕烧录到视频中
4. 前端通过接口轮询获取视频生成进度
5. 视频生成完成后，前端可以下载带字幕的视频

## 要求
考虑到分布式集群部署，请设计合适的任务异步处理和轮询方案的技术选型

## 设计思路

### 总体架构

- 分阶段异步编排：
  - 阶段A（数字人视频生成）：调用“AI 科普平台 数字人接口” `/dh/generate`（或 `/dh/generate_by_audio`），返回 `task_id` 与 `audio_data`（字幕切片，含起止时间）。通过 `/dh/status/{task_id}` 轮询直至获取 `result_url`（原始视频URL）。
  - 阶段B（字幕烧录）：将阶段A产出的 `result_url` 与由 `audio_data` 转换得到的 `.srt` 字幕文件，提交给“字幕合成平台” `/api/subtitles/burn-url-srt/async`，获得 `taskId`，轮询 `/api/subtitles/task/{taskId}` 直至返回 `outputUrl`（带字幕视频URL）。
- 对外只暴露本系统的创建任务与查询任务接口，前端仅与本系统 `task_id` 交互；第三方任务ID在后端统一维护与编排。

### 任务与状态编排

- 任务表（建议）：`video_generation_task`
  - 字段（建议）：
    - `id`（系统任务ID，UUID）、`user_id`、`input_text`、`model_name`、`voice`
    - `dh_task_id`、`dh_status`、`dh_result_url`
    - `burn_task_id`、`burn_status`、`output_url`
    - `progress`（0-100）、`status`（CREATED/DH_PROCESSING/DH_DONE/BURN_PROCESSING/COMPLETED/FAILED）
    - `error_message`、`created_at`、`updated_at`
  - 索引：`user_id`、`status`、`created_at`，以及 `dh_task_id`、`burn_task_id` 唯一索引（幂等）。

- 状态机：
  - CREATED → DH_PROCESSING → DH_DONE → BURN_PROCESSING → COMPLETED
  - 任一阶段失败 → FAILED，记录 `error_message`，支持重试。

- 进度映射（示例）：
  - DH_PROCESSING：0-60（按 `/dh/status` 的 `progress` 映射）
  - BURN_PROCESSING：60-99（按字幕平台 `progress` 映射）
  - COMPLETED：100

### 技术选型（分布式/异步）

- 持久化：MySQL + Flyway（项目已集成），任务表持久化流程状态。
- 任务驱动：
  - 首选 MQ（RocketMQ）+ 独立 Worker 服务（`enlighten-platform-biz` 承载），保证可扩展与解耦；
  
- 幂等：
  - 第三方 `task_id` 建唯一约束，重复提交直接复用结果；
  - 外部调用前做“同输入正在进行中任务”的去重（可选）；
  - 更新状态使用乐观锁（`version`/`updated_at`）。
- 重试与退避：
  - 网络/5xx：指数退避重试（3-5 次）；
  - 业务 4xx：不重试，直接失败入库。

### 对接细节（参考两份文档）

- 数字人阶段（参考《AI 科普平台 API 文档 v1.8.md》第二部分）：
  - 发起：`POST /dh/generate`，入参含 `model_name`、`text`、`voice`、可选切分参数；
  - 响应：`data.task_id`、`data.audio_data[]`（`text` 与 `time`，例如 `"00:00:00,170"` → `"00:00:01,670"`）；
  - 轮询：`GET /dh/status/{task_id}`，完成后拿到 `result_url`。
  - SRT 生成：将 `audio_data` 转为 SRT：
    - 序号自 1 递增；
    - 时间行：`start --> end`（时间格式已为 SRT 标准的逗号毫秒）；
    - 文本行：`text`。

- 字幕烧录阶段（参考《视频字幕合成平台 API 文档.md》）：
  - 提交：`POST /api/subtitles/burn-url-srt/async`（multipart/form-data），字段：`taskId`、`videoUrl`（上一步 `result_url`）、`subtitleFile`（SRT 文件）。
  - 轮询：`GET /api/subtitles/task/{taskId}`，直至 `state=COMPLETED`，获取 `outputUrl`。

### 对外接口设计（本系统）

- 创建任务：`POST /api/video/generate`
  - 请求：`text`（必填）、`model_name`（可选）、`voice`（可选，默认 `Female_Voice_1`）
  - 响应：`task_id`（系统任务ID）

- 查询状态：`GET /api/video/status/{task_id}`
  - 响应：`status`、`progress`、`result_url`（完成后为带字幕视频 URL）、`message/error_message`

- 下载结果（可选）：`GET /video/download/{task_id}`（后端可 302 跳转至 `result_url` 或做鉴权代理）。

### 执行流程（顺序概览）

1) `POST /video/generate`：写入任务（CREATED, progress=0），投递 MQ/标记待处理。
2) Worker 处理 CREATED：调用 `/dh/generate`，落库 `dh_task_id` → DH_PROCESSING。
3) 轮询 `/dh/status/{dh_task_id}`：完成后落库 `dh_result_url`、`audio_data` → DH_DONE，progress≈60。
4) 生成 SRT 文件：由 `audio_data` 转换，存临时目录或对象存储。
5) 提交 `/api/subtitles/burn-url-srt/async`：落库 `burn_task_id` → BURN_PROCESSING。
6) 轮询 `/api/subtitles/task/{burn_task_id}`：完成后落库 `output_url` → COMPLETED（progress=100）。
7) 异常：任一阶段失败 → FAILED，记录 `error_message`，可人工/接口触发重试（注意幂等与第三方任务有效性）。

### 轮询与频控

- 前端：每 3-5 秒轮询我方 `/video/status/{task_id}`；不直连第三方。
- 后端：
  - 数字人阶段：初始 2s，最大 10s，指数退避；
  - 字幕阶段：建议 5s；
  - 设置全局超时（如 30 分钟）与最大重试次数。

### 存储与文件处理

- SRT：在内存拼装并落地为临时文件；
- 结果：使用字幕平台 `outputUrl`

### 安全与多租户

- 多租户：按 `tenant_id` 隔离；
- 鉴权：对外接口鉴权与限流；
- 输入校验：限制 `text` 长度、敏感词校验（暂时不做）、防重提交；


### 可观测性

- 日志：记录外部请求摘要/耗时、状态流转；
- 指标：任务吞吐、成功率、阶段时延、失败分布；
- 告警：阶段超时/连续失败触发。

## 参考文档

第三方服务接口见 
### 文档1：
AI 科普平台 API 文档 v1.8
第二部分：数字人接口 的第四和第六节
### 文档2：
视频字幕合成平台 API 文档.md


