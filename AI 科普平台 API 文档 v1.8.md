# AI 科普平台 API 文档 v1.8

### DEMO

*   [http://192.168.1.65:8000/demo](http://192.168.1.65:8000/demo)
    

### 一、基础API 接口

### 1. 生成科普内容/science-generator 根据用户提供的参数生成科普文章或其他形式的内容。支持流式输出。

*   路由：`/science-generator`
    
*   请求方式：POST
    
*   Content-Type: `application/json`
    
*   字段说明:
    

| 字段名 | 类型 | 必填 | 默认值 | 描述 |
| --- | --- | --- | --- | --- |
| `document_id` | `string` 或 `null` | 否 | `null` | **知识库ID**，可以通过`/send_document`接口获取。如果提供，AI会结合该知识库内容进行创作。如果未提供，则不使用知识库。 |
| `contains_image` | `boolean` | 否 | `false` | 是否需要在生成内容中插入图片。**（目前前端传递参数，后端功能待实现）** |
| `topic` | `string` | 是 | \- | 内容的主题。 |
| `content` | `string` | 是 | \- | 具体内容要求，详细描述需要生成的内容。 |
| `outline` | `string` 或 `null` | 否 | `null` | 指定内容的大纲，AI会严格按照此大纲进行写作。 |
| `case` | `string` 或 `null` | 否 | `null` | 参考案例，AI会学习其风格和内容。 |
| `style` | `string` | 是 | \- | 内容的风格。如：`"亲切口语"`, `"专业严谨"`, `"故事化"`, `"小红书"`, `"公众号"` |
| `length` | `string` | 是 | \- | 内容的长度要求。如：`"短"`, `"中"`, `"长"` |
| `mode` | `string` | 是 | \- | 内容的表现形式。如：`"常规文章"`, `"双人对话"`, `"故事案例"` |
| `scene` | `string` | 是 | \- | 内容的应用场景。如：`"日常科普"`, `"入院须知"`, `"术前指导"`, `"术后康复"`, `"慢病管理"` |

*   请求示例:
    

```json
{
  "document_id": "optional_knowledge_base_id",
  "contains_image": false,
  "topic": "糖尿病患者的饮食管理",
  "content": "文章需要包含糖尿病的常见误区和正确的饮食建议",
  "outline": "1. 糖尿病的定义\n2. 糖尿病饮食的原则\n3. 常见饮食误区及纠正",
  "case": "这是一篇关于高血压的文章，风格亲切，口语化，适合大众阅读。",
  "style": "亲切口语",
  "length": "中",
  "mode": "常规文章",
  "scene": "日常科普"
}

```

**响应格式**

*   Content-Type: `text/event-stream`
    

> 接口采用 **Server-Sent Events (SSE)** 流式输出。客户端将持续接收 `data:` 前缀的文本行，每行包含一个 JSON 片段。客户端需要解析每个 data: 行后面的 JSON 数据，并将其中的 `choices[0].delta.content` 字段内容进行拼接，以构建完整的生成结果。通过判断 `choices.finish_reason === "stop"` 判断响应是否结束

**响应字段**`**type**`**说明：**

*   目前有两种可选值：
    
*   `llm_token`：正常的内容输出
    
*   `retrieving`：正式内容输出前的过程提示，用于展示在弹窗内
    

*   响应示例:
    

```plaintext
data: {"model":"zj-preset","type":"retrieving","choices":[{"delta":{"role":"assistant","content":"\u6b63\u5728\u68c0\u7d22\u53c2\u8003\u6587\u732e..."},"finish_reason":"null"}]}

data: {"model":"zj-preset","type":"retrieving","choices":[{"delta":{"role":"assistant","content":"\u672c\u6b21\u5185\u5bb9\u751f\u6210\u5171\u68c0\u7d22\u5230 8 \u6761\u4e13\u4e1a\u6587\u732e"},"finish_reason":"null"}]}

data: {"model":"zj-llm","type":"llm_token","choices":[{"finish_reason":"null","delta":{"role":"assistant","content":"#"}}]}

data: {"model":"zj-llm","type":"llm_token","choices":[{"finish_reason":"null","delta":{"role":"assistant","content":""}}]}

data: {"model":"zj-llm","type":"llm_token","choices":[{"finish_reason":"null","delta":{"role":"assistant","content":""}}]}

data: {"model":"zj-llm","type":"llm_token","choices":[{"finish_reason":"null","delta":{"role":"assistant","content":" \u7cd6"}}]}

data: {"model":"zj-llm","type":"llm_token","choices":[{"finish_reason":"null","delta":{"role":"assistant","content":"\u5c3f\u75c5\u60a3\u8005\u8fd0\u52a8"}}]}

...

data: {"model":"zj-llm","type":"llm_token","choices":[{"finish_reason":"stop","delta":{"role":"assistant","content":"\uff01**"}}]}

```

### 2. 上传文档 (创建知识库)/send\_document 用于上传文档内容，将其处理并创建为一个知识库。成功上传后，会返回一个 \`document\_id\`，此 ID 可用于后续的 \`/science-generator\` 请求，以便 AI 在生成内容时参考该知识库。

*   路由：`/send_document`
    
*   请求方式：POST
    
*   Content-Type: `application/json`
    
*   字段说明:
    

| 字段名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `document` | `string` | 是 | 要上传的文档内容。 |

*   请求示例:
    

```json
{
  "document": "这是一份关于糖尿病最新治疗指南的文档内容。糖尿病是一种慢性疾病，需要长期管理..."
}

```

**响应格式**

*   Content-Type: `application/json`
    
*   字段说明:
    

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `message` | `string` | 描述信息。 |
| `data` | `object` | 包含返回数据的对象。 |
| `data.document_id` | `string` | 新创建的知识库的唯一标识符。可以在后续的 `/science-generator` 请求中使用此ID来引用该知识库。 |

*   成功响应:
    

```json
{
    "code": 200,
    "success": true,
    "msg": "ok",
    "data": {
        "document_id": "1752045883.341268_914"
    }
}

```

*   错误响应:
    

```json
{
    "code": 500,
    "success": false,
    "msg": "文档上传失败",
    "data": null
}

```

### 3. AI 帮填/fill\_in 根据用户描述的需求，智能分析并返回结构化数据，用于前端表单的自动化填充。该接口支持流式输出，并且输出内容包含特定标记，方便进行实时解析和填充。

*   路由：`/fill_in`
    
*   请求方式：`POST`
    
*   Content-Type: `application/json`
    
*   字段说明:
    

| 字段名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `content` | `string` | 是 | 用户对表单填充的具体描述或指令。例如：“帮我填一个关于高血压慢病管理的短篇科普，用亲切口语风格，不需要配图，具体内容写日常注意事项。” |

*   请求示例:
    

```json
{
  "content": "关于高血压慢病管理的短篇科普，用亲切口语风格，不需要配图，具体内容写日常注意事项。"
}

```

**响应格式**

*   Content-Type: `text/event-stream`
    

> 接口采用 **Server-Sent Events (SSE)** 流式输出。客户端将持续接收 `data:` 前缀的文本行，每行包含一个 JSON 片段。客户端需要解析每个 `data:` 行后面的 JSON 数据，并将其中的 `choices[0].delta.content` 字段内容进行拼接。与 `/science-generator` 接口不同的是，`/fill_in` 接口的 `content` 字段会包含特定的**字段标记** (`<FIELD:field_name>`) 和**内容分隔符** (`<SEP>`)，用于指示不同表单字段及其内容。客户端应根据这些标记实时解析内容，并将其填充到对应的表单元素中。

*   响应内容结构 (在 `choices[0].delta.content` 内):
    

```plaintext
<FIELD:topic>高血压患者的日常健康管理指南<SEP>
<FIELD:content>文章需涵盖高血压患者日常监测血压的方法、饮食调整建议（如低钠饮食）、规律运动的重要性及推荐运动类型、情绪管理技巧，以及药物依从性的重要性。<SEP>
<FIELD:style>亲切口语<SEP>
<FIELD:length>中<SEP>
<FIELD:scene>慢病管理<SEP>
<FIELD:mode>常规文章<SEP>
<FIELD:containsImage>true<SEP>

```

*   `**<FIELD:field_name>**`: 表示一个新字段的开始，`field_name` 是对应的表单字段 `ID` (例如 `topic`, `content`, `style` 等)。
    
*   `**<SEP>**`: 表示当前字段内容的结束。
    
*   **注意**: 即使是布尔值或下拉框选项，AI也会以字符串形式输出在 `<FIELD:...>` 和 `<SEP>` 之间。前端需自行转换。
    
*   响应示例:
    

```plaintext
data:{"status_code":200,"request_id":"fec4fd8b-8c0f-98cc-98fa-c7bd6c83940c","code":"","message":"","usage":{"input_tokens":1162,"output_tokens":1,"total_tokens":1163,"prompt_tokens_details":{"cached_tokens":0}},"choices":[{"finish_reason":"null","delta":{"role":"assistant","content":"`"}}]}

data:{"status_code":200,"request_id":"fec4fd8b-8c0f-98cc-98fa-c7bd6c83940c","code":"","message":"","usage":{"input_tokens":1162,"output_tokens":2,"total_tokens":1164,"prompt_tokens_details":{"cached_tokens":0}},"choices":[{"finish_reason":"null","delta":{"role":"assistant","content":"<"}}]}
  
data:{"status_code":200,"request_id":"fec4fd8b-8c0f-98cc-98fa-c7bd6c83940c","code":"","message":"","usage":{"input_tokens":1162,"output_tokens":3,"total_tokens":1165,"prompt_tokens_details":{"cached_tokens":0}},"choices":[{"finish_reason":"null","delta":{"role":"assistant","content":"FIELD"}}]}
  
data:{"status_code":200,"request_id":"fec4fd8b-8c0f-98cc-98fa-c7bd6c83940c","code":"","message":"","usage":{"input_tokens":1162,"output_tokens":4,"total_tokens":1166,"prompt_tokens_details":{"cached_tokens":0}},"choices":[{"finish_reason":"null","delta":{"role":"assistant","content":":"}}]}
  
data:{"status_code":200,"request_id":"fec4fd8b-8c0f-98cc-98fa-c7bd6c83940c","code":"","message":"","usage":{"input_tokens":1162,"output_tokens":8,"total_tokens":1170,"prompt_tokens_details":{"cached_tokens":0}},"choices":[{"finish_reason":"null","delta":{"role":"assistant","content":"topic>AI\u5728"}}]}
  
data:{"status_code":200,"request_id":"fec4fd8b-8c0f-98cc-98fa-c7bd6c83940c","code":"","message":"","usage":{"input_tokens":1162,"output_tokens":12,"total_tokens":1174,"prompt_tokens_details":{"cached_tokens":0}},"choices":[{"finish_reason":"null","delta":{"role":"assistant","content":"\u533b\u5b66\u79d1\u666e\u4e2d\u7684\u5e94\u7528"}}]}
  
data:{"status_code":200,"request_id":"fec4fd8b-8c0f-98cc-98fa-c7bd6c83940c","code":"","message":"","usage":{"input_tokens":1162,"output_tokens":16,"total_tokens":1178,"prompt_tokens_details":{"cached_tokens":0}},"choices":[{"finish_reason":"null","delta":{"role":"assistant","content":"\u4e0e\u524d\u666f<SEP"}}]}
  
data:{"status_code":200,"request_id":"fec4fd8b-8c0f-98cc-98fa-c7bd6c83940c","code":"","message":"","usage":{"input_tokens":1162,"output_tokens":20,"total_tokens":1182,"prompt_tokens_details":{"cached_tokens":0}},"choices":[{"finish_reason":"null","delta":{"role":"assistant","content":">`\n`<FIELD"}}]}

...

data:{"status_code":200,"request_id":"...","choices":[{"finish_reason":"stop","delta":{"role":"assistant","content":""}}]}

```

### 4. 语音合成 /synthesize\_audio 用于将文本合成为语音，并以实时流式方式传输音频数据。前端可以使用 \`MediaSource API\` 进行实时播放。

*   路由：`/synthesize_audio`
    
*   请求方式：POST
    
*   Content-Type: `application/json`
    
*   字段说明:
    

| 字段名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `text` | `string` | 是 | 目标合成文本。 |
| `voice` | `string` | 是 | 预置音色。目前可选：`Male_Voice_1`: 男性音色 `Female_Voice_1`：女性音色 |
| `seed` | `integer` | 否 | 推理的随机种子。0 表示随机。默认值：0。 |
| `stream` | `boolean` | 否 | 是否使用流式推理。默认值：`true`。 |
| `speed` | `number` | 否 | 合成速度调整（0.5 到 2.0）。默认值：1.0。 |

*   请求示例:
    

```json
{
  "text": "你好，这是一个实时语音合成的例子。",
  "voice": "Female_Voice_1"
}

```

**响应格式**

*   成功响应:
    
    *   Content-Type: `audio/mpeg` 
        
    *   成功时，直接返回 MP3 格式的音频二进制流。前端将实时接收并播放此流。
        
    *   HTTP 状态码为 `200 OK`。
        
*   错误响应:
    
    *   Content-Type: `application/json`
        
    *   HTTP 状态码非200， 通常为 `400 Bad Request`。
        
    *   字段说明:
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `msg` | `string` | 描述信息。 |

*   错误响应示例:
    

```json
{
  "code": 400,
  "success": false,
  "msg": "请提供要合成的文本！",
  "data": null
}

```

### 5. 转换为媒体图文 /convert2media 用于将输入的原文内容转换为目标媒体平台的文章格式，返回符合平台风格的文章结构。

*   路由：`/convert2media`
    
*   请求方式：POST
    
*   Content-Type: `application/json`
    
*   字段说明:
    

| 字段名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `content` | `string` | 是 | 原文内容。<br>可以传HTML / Markdown / 纯文本 |
| `platform` | `string` | 是 | 目标媒体平台。<br>目前支持：<br>`xiaohongshu`（小红书）<br>`douyin`（抖音） |

*   请求示例:
    

```json
{
  "content": "这是一篇关于健康饮食的文章，介绍了如何通过科学饮食改善身体状况。",
  "platform": "xiaohongshu"
}

```

**响应格式**

*   成功响应:
    
    *   Content-Type: `application/json`
        
    *   字段说明:
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `msg` | `string` | 描述信息。 |
| `data` | `object` | 返回的文章数据，包含以下字段： |

*   `title`: 大标题。   
    
*   `slides`: 数组：      
    
    *   `subtitle`: 小标题。      
        
    *   `points`: 数组，3-4条正文。
        
*   `content`: 推文文案，符合目标平台风格。
    

*   成功响应示例:
    

```json
{
  "code": 200,
  "success": true,
  "msg": "ok",
  "data": {
    "title": "健康饮食，轻松改善身体状况",
    "slides": [
      {
        "subtitle": "科学饮食第一步",
        "points": [
          "均衡营养搭配，每天摄入多样化食物。",
          "减少高糖高脂食品，保持健康体重。",
          "多吃蔬菜水果，补充维生素和纤维素。"
        ]
      },
      {
        "subtitle": "坚持运动更健康",
        "points": [
          "每天运动30分钟，增强体质。",
          "选择适合自己的运动方式，比如瑜伽或跑步。",
          "运动后及时补充水分，避免脱水。"
        ]
      }
    ],
    "content": "🌟健康饮食小贴士：均衡营养、少油少盐，搭配适量运动，轻松拥有好身体！#健康生活 #小红书推荐"
  }
}

```

*   错误响应:
    
    *   Content-Type: `application/json`
        
    *   字段说明:
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `msg` | `string` | 描述信息。 |
| `data` | `null` | 错误时无数据返回。 |

*   错误响应示例:
    

```json
{
  "code": 400,
  "success": false,
  "msg": "platform不合法！",
  "data": null
}

```

### 6. 转换为公众号图文(重新生成) /convert2gzh\_re 用于将输入的原文内容转换为公众号图文的模版格式。

*   路由：`/convert2gzh_re`
    
*   请求方式：POST
    
*   Content-Type: `application/json`
    
*   字段说明:
    

| 字段名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `content` | `string` | 是 | 原文内容。（不含大标题）<br>可以传HTML / Markdown / 纯文本 |

*   请求示例:
    

```json
{
  "content": "百日咳，听上去像是“咳一百天”，其实它还真不是夸张..."
}

```

**响应格式**

*   成功响应:
    
    *   Content-Type: `application/json`
        
    *   字段说明:
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `msg` | `string` | 描述信息。 |
| `data` | `object` | 返回文章的正文数据（不含大标题），HTML格式 |

*   成功响应示例:
    

```json
{
    "code": 200,
    "success": true,
    "msg": "ok",
    "data": "<section data-role=\"title\" data-tools=\"135编辑器\" data-id=\"158694\"><section style=\"margin: 10px auto;display: flex;justify-content: center;\"><section><section style=\"width: 26px;margin-bottom: -7px;margin-left: -14px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 135.21 135.21\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M67.6,0l2.25,27.81C72.09,47,88.22,63.12,107.4,65.36l27.81,2.24L107.4,69.85C88.22,72.09,72.09,88.22,69.85,107.4L67.6,135.21,65.36,107.4C63.12,88.22,47,72.09,27.81,69.85L0,67.6l27.81-2.24C47,63.12,63.12,47,65.36,27.81Z\" style=\"fill:#ffd220;\"></path></g></g></svg></section><section style=\"display: flex;flex-direction: column;\"><section style=\"background-color: rgb(30, 155, 232);padding: 9px 14px;z-index: 4;transform: skew(-15deg);\"><section style=\"transform: skew(15deg);-webkit-transform: skew(15deg);-moz-transform: skew(15deg);-o-transform: skew(15deg);\"><section style=\"font-size: 16px;color: #ffffff;text-align: center;\"><strong data-brushtype=\"text\">百日咳全攻略</strong></section></section></section><section style=\"width: 100%;height: 25px;background-image: linear-gradient(to right, transparent, rgb(182, 228, 253));background-position: initial;background-size: initial;background-repeat: initial;background-attachment: initial;background-origin: initial;background-clip: initial;margin-top: -18px;z-index: 1;transform: skew(20deg);\" data-width=\"100%\"><br></section></section></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">百日咳，听上去像是“咳一百天”，其实它还真不是夸张。这个病名来源于其漫长的病程，咳嗽可能持续数周甚至数月。百日咳是一种由百日咳杆菌引起的急性呼吸道传染病，尤其在儿童中高发，但成人也不能掉以轻心。今天我们就来聊聊百日咳的诊断与治疗，帮你科学应对这场“持久战”。</span></section><section data-role=\"title\" data-id=\"141293\"><section style=\"margin: 10px auto;display: flex;justify-content: center;padding-left: 30px;\"><section><section style=\"display: flex;justify-content: flex-start;\"><section style=\"margin-bottom: -5px;transform: translateX(-30px);-webkit-transform: translateX(-30px);-moz-transform: translateX(-30px);-o-transform: translateX(-30px);\"><section style=\"background-color: rgb(255, 206, 89);border-radius: 25px;padding: 2px 15px;\" class=\"js_darkmode__14\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">临床表现</strong></section></section><section style=\"width: 0px;height: 1px;border-left: 8px solid transparent;border-top: 8px solid rgb(255, 206, 89);margin-left: 20px;\" class=\"js_darkmode__15\"><br></section></section></section><section style=\"display: flex;justify-content: center;\"><section style=\"display: flex;justify-content: space-between;\"><section style=\"padding: 2px 10px;background-color: rgb(30, 155, 232);border-radius: 5px 0px 0px 5px;display: flex;justify-content: space-between;align-items: center;\" class=\"js_darkmode__16\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">三个阶段要知晓</strong></section></section><section style=\"flex-shrink: 0;\"><section style=\"display: flex;justify-content: space-between;align-items: flex-start;flex-direction: column;\"><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section></section></section></section></section></section></section></section><section data-id=\"152673\"><section style=\"margin: 10px auto;background-color: rgb(182, 228, 253);border-radius: 10px;padding: 10px;\" class=\"js_darkmode__5\"><section style=\"border-width: 1px;border-style: dashed;border-color: rgb(90, 154, 242);border-radius: 10px;background-color: rgb(255, 255, 255);padding: 13px 10px;\" class=\"js_darkmode__6\"><section style=\"margin: 10px auto;text-align: left;\" data-id=\"105887\" data-tools=\"135编辑器\"><section data-brushtype=\"text\" style=\"font-size: 16px;letter-spacing: 1.5px;color: rgb(62, 157, 215);margin-bottom: -22px;margin-left: 16px;\">01 卡他期（初期）</section><section style=\"display: flex;justify-content: center;align-items: flex-end;\"><section style=\"width: 5px;height: 22px;background-color: rgb(66, 163, 225);\" class=\"js_darkmode__8\"><br></section><section style=\"width: 100%;height: 3px;background-color0: rgb(255, 238, 194);flex: 1 1 0%;margin-left: 8px;\" data-width=\"100%\" class=\"js_darkmode__9\"><br></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">持续约1～2周，症状类似普通感冒，包括流涕、打喷嚏、轻咳、低热等，容易被忽视或误诊。</span></section><section style=\"margin: 10px auto;text-align: left;\" data-id=\"105887\" data-tools=\"135编辑器\"><section data-brushtype=\"text\" style=\"font-size: 16px;letter-spacing: 1.5px;color: rgb(62, 157, 215);margin-bottom: -22px;margin-left: 16px;\">02 痉咳期（高峰期）</section><section style=\"display: flex;justify-content: center;align-items: flex-end;\"><section style=\"width: 5px;height: 22px;background-color: rgb(66, 163, 225);\" class=\"js_darkmode__8\"><br></section><section style=\"width: 100%;height: 3px;background-color0: rgb(255, 238, 194);flex: 1 1 0%;margin-left: 8px;\" data-width=\"100%\" class=\"js_darkmode__9\"><br></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">通常在发病后1～2周进入此阶段，持续2～6周甚至更久。典型表现为阵发性、痉挛性咳嗽，咳嗽剧烈且频繁，常伴有“鸡鸣样”吸气声，咳后呕吐也较常见。儿童尤其容易出现这种特征性表现。</span></section><section style=\"margin: 10px auto;text-align: left;\" data-id=\"105887\" data-tools=\"135编辑器\"><section data-brushtype=\"text\" style=\"font-size: 16px;letter-spacing: 1.5px;color: rgb(62, 157, 215);margin-bottom: -22px;margin-left: 16px;\">03 恢复期（缓解期）</section><section style=\"display: flex;justify-content: center;align-items: flex-end;\"><section style=\"width: 5px;height: 22px;background-color: rgb(66, 163, 225);\" class=\"js_darkmode__8\"><br></section><section style=\"width: 100%;height: 3px;background-color0: rgb(255, 238, 194);flex: 1 1 0%;margin-left: 8px;\" data-width=\"100%\" class=\"js_darkmode__9\"><br></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">咳嗽频率和强度逐渐减轻，但仍可能持续数周，遇冷、烟雾等刺激时容易复发。</span></section></section></section></section><section data-role=\"title\" data-id=\"141293\"><section style=\"margin: 10px auto;display: flex;justify-content: center;padding-left: 30px;\"><section><section style=\"display: flex;justify-content: flex-start;\"><section style=\"margin-bottom: -5px;transform: translateX(-30px);-webkit-transform: translateX(-30px);-moz-transform: translateX(-30px);-o-transform: translateX(-30px);\"><section style=\"background-color: rgb(255, 206, 89);border-radius: 25px;padding: 2px 15px;\" class=\"js_darkmode__14\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">诊断方法</strong></section></section><section style=\"width: 0px;height: 1px;border-left: 8px solid transparent;border-top: 8px solid rgb(255, 206, 89);margin-left: 20px;\" class=\"js_darkmode__15\"><br></section></section></section><section style=\"display: flex;justify-content: center;\"><section style=\"display: flex;justify-content: space-between;\"><section style=\"padding: 2px 10px;background-color: rgb(30, 155, 232);border-radius: 5px 0px 0px 5px;display: flex;justify-content: space-between;align-items: center;\" class=\"js_darkmode__16\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">科学检测更准确</strong></section></section><section style=\"flex-shrink: 0;\"><section style=\"display: flex;justify-content: space-between;align-items: flex-start;flex-direction: column;\"><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section></section></section></section></section></section></section></section><section data-id=\"152673\"><section style=\"margin: 10px auto;background-color: rgb(182, 228, 253);border-radius: 10px;padding: 10px;\" class=\"js_darkmode__5\"><section style=\"border-width: 1px;border-style: dashed;border-color: rgb(90, 154, 242);border-radius: 10px;background-color: rgb(255, 255, 255);padding: 13px 10px;\" class=\"js_darkmode__6\"><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">确诊百日咳不能只靠“听咳辨病”，还需借助科学手段：</span></section><section style=\"margin: 10px auto;text-align: left;\" data-id=\"105887\" data-tools=\"135编辑器\"><section data-brushtype=\"text\" style=\"font-size: 16px;letter-spacing: 1.5px;color: rgb(62, 157, 215);margin-bottom: -22px;margin-left: 16px;\">01 鼻咽拭子检测</section><section style=\"display: flex;justify-content: center;align-items: flex-end;\"><section style=\"width: 5px;height: 22px;background-color: rgb(66, 163, 225);\" class=\"js_darkmode__8\"><br></section><section style=\"width: 100%;height: 3px;background-color0: rgb(255, 238, 194);flex: 1 1 0%;margin-left: 8px;\" data-width=\"100%\" class=\"js_darkmode__9\"><br></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">通过PCR技术检测百日咳杆菌的DNA，敏感性高、出结果快，是早期诊断的首选方法。</span></section><section style=\"margin: 10px auto;text-align: left;\" data-id=\"105887\" data-tools=\"135编辑器\"><section data-brushtype=\"text\" style=\"font-size: 16px;letter-spacing: 1.5px;color: rgb(62, 157, 215);margin-bottom: -22px;margin-left: 16px;\">02 血清学检查</section><section style=\"display: flex;justify-content: center;align-items: flex-end;\"><section style=\"width: 5px;height: 22px;background-color: rgb(66, 163, 225);\" class=\"js_darkmode__8\"><br></section><section style=\"width: 100%;height: 3px;background-color0: rgb(255, 238, 194);flex: 1 1 0%;margin-left: 8px;\" data-width=\"100%\" class=\"js_darkmode__9\"><br></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">检测特异性抗体水平，适用于病程中后期或无法采集呼吸道样本的患者。</span></section><section style=\"margin: 10px auto;text-align: left;\" data-id=\"105887\" data-tools=\"135编辑器\"><section data-brushtype=\"text\" style=\"font-size: 16px;letter-spacing: 1.5px;color: rgb(62, 157, 215);margin-bottom: -22px;margin-left: 16px;\">03 细菌培养</section><section style=\"display: flex;justify-content: center;align-items: flex-end;\"><section style=\"width: 5px;height: 22px;background-color: rgb(66, 163, 225);\" class=\"js_darkmode__8\"><br></section><section style=\"width: 100%;height: 3px;background-color0: rgb(255, 238, 194);flex: 1 1 0%;margin-left: 8px;\" data-width=\"100%\" class=\"js_darkmode__9\"><br></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">虽然特异性高，但对技术和时间要求较高，临床应用相对较少。</span></section></section></section></section><section data-role=\"title\" data-id=\"141293\"><section style=\"margin: 10px auto;display: flex;justify-content: center;padding-left: 30px;\"><section><section style=\"display: flex;justify-content: flex-start;\"><section style=\"margin-bottom: -5px;transform: translateX(-30px);-webkit-transform: translateX(-30px);-moz-transform: translateX(-30px);-o-transform: translateX(-30px);\"><section style=\"background-color: rgb(255, 206, 89);border-radius: 25px;padding: 2px 15px;\" class=\"js_darkmode__14\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">治疗方案</strong></section></section><section style=\"width: 0px;height: 1px;border-left: 8px solid transparent;border-top: 8px solid rgb(255, 206, 89);margin-left: 20px;\" class=\"js_darkmode__15\"><br></section></section></section><section style=\"display: flex;justify-content: center;\"><section style=\"display: flex;justify-content: space-between;\"><section style=\"padding: 2px 10px;background-color: rgb(30, 155, 232);border-radius: 5px 0px 0px 5px;display: flex;justify-content: space-between;align-items: center;\" class=\"js_darkmode__16\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">及时用药更有效</strong></section></section><section style=\"flex-shrink: 0;\"><section style=\"display: flex;justify-content: space-between;align-items: flex-start;flex-direction: column;\"><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section></section></section></section></section></section></section></section><section data-id=\"152673\"><section style=\"margin: 10px auto;background-color: rgb(182, 228, 253);border-radius: 10px;padding: 10px;\" class=\"js_darkmode__5\"><section style=\"border-width: 1px;border-style: dashed;border-color: rgb(90, 154, 242);border-radius: 10px;background-color: rgb(255, 255, 255);padding: 13px 10px;\" class=\"js_darkmode__6\"><section data-role=\"title\" data-tools=\"135编辑器\" data-id=\"158694\"><section style=\"margin: 10px auto;display: flex;justify-content: center;\"><section><section style=\"width: 26px;margin-bottom: -7px;margin-left: -14px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 135.21 135.21\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M67.6,0l2.25,27.81C72.09,47,88.22,63.12,107.4,65.36l27.81,2.24L107.4,69.85C88.22,72.09,72.09,88.22,69.85,107.4L67.6,135.21,65.36,107.4C63.12,88.22,47,72.09,27.81,69.85L0,67.6l27.81-2.24C47,63.12,63.12,47,65.36,27.81Z\" style=\"fill:#ffd220;\"></path></g></g></svg></section><section style=\"display: flex;flex-direction: column;\"><section style=\"background-color: rgb(30, 155, 232);padding: 9px 14px;z-index: 4;transform: skew(-15deg);\"><section style=\"transform: skew(15deg);-webkit-transform: skew(15deg);-moz-transform: skew(15deg);-o-transform: skew(15deg);\"><section style=\"font-size: 16px;color: #ffffff;text-align: center;\"><strong data-brushtype=\"text\">药物治疗</strong></section></section></section><section style=\"width: 100%;height: 25px;background-image: linear-gradient(to right, transparent, rgb(182, 228, 253));background-position: initial;background-size: initial;background-repeat: initial;background-attachment: initial;background-origin: initial;background-clip: initial;margin-top: -18px;z-index: 1;transform: skew(20deg);\" data-width=\"100%\"><br></section></section></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">百日咳一旦确诊，应尽早开始抗菌治疗，以缩短病程并减少传染性。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\"><span style=\"font-weight: bold;\">首选药物</span>：大环内酯类抗生素（如阿奇霉素、克拉霉素、红霉素），特别适用于儿童，尤其是婴幼儿。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\"><span style=\"font-weight: bold;\">替代方案</span>：四环素类（如多西环素）适用于8岁以上儿童及成人；喹诺酮类（如左氧氟沙星）适用于18岁以上成人。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\"><span style=\"font-weight: bold;\">家庭成员预防性用药</span>：密切接触者即使无症状，也建议预防性使用抗生素，防止传播。</span></section><section data-role=\"title\" data-tools=\"135编辑器\" data-id=\"158694\"><section style=\"margin: 10px auto;display: flex;justify-content: center;\"><section><section style=\"width: 26px;margin-bottom: -7px;margin-left: -14px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 135.21 135.21\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M67.6,0l2.25,27.81C72.09,47,88.22,63.12,107.4,65.36l27.81,2.24L107.4,69.85C88.22,72.09,72.09,88.22,69.85,107.4L67.6,135.21,65.36,107.4C63.12,88.22,47,72.09,27.81,69.85L0,67.6l27.81-2.24C47,63.12,63.12,47,65.36,27.81Z\" style=\"fill:#ffd220;\"></path></g></g></svg></section><section style=\"display: flex;flex-direction: column;\"><section style=\"background-color: rgb(30, 155, 232);padding: 9px 14px;z-index: 4;transform: skew(-15deg);\"><section style=\"transform: skew(15deg);-webkit-transform: skew(15deg);-moz-transform: skew(15deg);-o-transform: skew(15deg);\"><section style=\"font-size: 16px;color: #ffffff;text-align: center;\"><strong data-brushtype=\"text\">对症治疗与护理</strong></section></section></section><section style=\"width: 100%;height: 25px;background-image: linear-gradient(to right, transparent, rgb(182, 228, 253));background-position: initial;background-size: initial;background-repeat: initial;background-attachment: initial;background-origin: initial;background-clip: initial;margin-top: -18px;z-index: 1;transform: skew(20deg);\" data-width=\"100%\"><br></section></section></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- 止咳治疗：目前尚无特效止咳药，但可使用镇咳剂缓解症状，尤其在夜间影响睡眠时。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- 止吐与营养支持：咳嗽剧烈导致呕吐者，可适当使用止吐药，并注意补充水分和营养。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- 环境管理：保持空气流通，避免烟雾、灰尘等刺激物，饮食宜清淡易消化。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- 休息与观察：尤其婴幼儿需密切观察是否有呼吸暂停、缺氧等严重并发症。</span></section></section></section></section><section data-role=\"title\" data-id=\"141293\"><section style=\"margin: 10px auto;display: flex;justify-content: center;padding-left: 30px;\"><section><section style=\"display: flex;justify-content: flex-start;\"><section style=\"margin-bottom: -5px;transform: translateX(-30px);-webkit-transform: translateX(-30px);-moz-transform: translateX(-30px);-o-transform: translateX(-30px);\"><section style=\"background-color: rgb(255, 206, 89);border-radius: 25px;padding: 2px 15px;\" class=\"js_darkmode__14\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">人群建议</strong></section></section><section style=\"width: 0px;height: 1px;border-left: 8px solid transparent;border-top: 8px solid rgb(255, 206, 89);margin-left: 20px;\" class=\"js_darkmode__15\"><br></section></section></section><section style=\"display: flex;justify-content: center;\"><section style=\"display: flex;justify-content: space-between;\"><section style=\"padding: 2px 10px;background-color: rgb(30, 155, 232);border-radius: 5px 0px 0px 5px;display: flex;justify-content: space-between;align-items: center;\" class=\"js_darkmode__16\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">因人施治更科学</strong></section></section><section style=\"flex-shrink: 0;\"><section style=\"display: flex;justify-content: space-between;align-items: flex-start;flex-direction: column;\"><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section></section></section></section></section></section></section></section><section data-id=\"152673\"><section style=\"margin: 10px auto;background-color: rgb(182, 228, 253);border-radius: 10px;padding: 10px;\" class=\"js_darkmode__5\"><section style=\"border-width: 1px;border-style: dashed;border-color: rgb(90, 154, 242);border-radius: 10px;background-color: rgb(255, 255, 255);padding: 13px 10px;\" class=\"js_darkmode__6\"><section data-role=\"title\" data-tools=\"135编辑器\" data-id=\"158694\"><section style=\"margin: 10px auto;display: flex;justify-content: center;\"><section><section style=\"width: 26px;margin-bottom: -7px;margin-left: -14px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 135.21 135.21\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M67.6,0l2.25,27.81C72.09,47,88.22,63.12,107.4,65.36l27.81,2.24L107.4,69.85C88.22,72.09,72.09,88.22,69.85,107.4L67.6,135.21,65.36,107.4C63.12,88.22,47,72.09,27.81,69.85L0,67.6l27.81-2.24C47,63.12,63.12,47,65.36,27.81Z\" style=\"fill:#ffd220;\"></path></g></g></svg></section><section style=\"display: flex;flex-direction: column;\"><section style=\"background-color: rgb(30, 155, 232);padding: 9px 14px;z-index: 4;transform: skew(-15deg);\"><section style=\"transform: skew(15deg);-webkit-transform: skew(15deg);-moz-transform: skew(15deg);-o-transform: skew(15deg);\"><section style=\"font-size: 16px;color: #ffffff;text-align: center;\"><strong data-brushtype=\"text\">儿童（特别是婴幼儿）</strong></section></section></section><section style=\"width: 100%;height: 25px;background-image: linear-gradient(to right, transparent, rgb(182, 228, 253));background-position: initial;background-size: initial;background-repeat: initial;background-attachment: initial;background-origin: initial;background-clip: initial;margin-top: -18px;z-index: 1;transform: skew(20deg);\" data-width=\"100%\"><br></section></section></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- 抗生素首选红霉素或阿奇霉素，需严格遵医嘱调整剂量。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- 婴幼儿病情进展快，建议早期住院治疗。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- 家庭护理要特别注意防止呛咳引发窒息。</span></section><section data-role=\"title\" data-tools=\"135编辑器\" data-id=\"158694\"><section style=\"margin: 10px auto;display: flex;justify-content: center;\"><section><section style=\"width: 26px;margin-bottom: -7px;margin-left: -14px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 135.21 135.21\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M67.6,0l2.25,27.81C72.09,47,88.22,63.12,107.4,65.36l27.81,2.24L107.4,69.85C88.22,72.09,72.09,88.22,69.85,107.4L67.6,135.21,65.36,107.4C63.12,88.22,47,72.09,27.81,69.85L0,67.6l27.81-2.24C47,63.12,63.12,47,65.36,27.81Z\" style=\"fill:#ffd220;\"></path></g></g></svg></section><section style=\"display: flex;flex-direction: column;\"><section style=\"background-color: rgb(30, 155, 232);padding: 9px 14px;z-index: 4;transform: skew(-15deg);\"><section style=\"transform: skew(15deg);-webkit-transform: skew(15deg);-moz-transform: skew(15deg);-o-transform: skew(15deg);\"><section style=\"font-size: 16px;color: #ffffff;text-align: center;\"><strong data-brushtype=\"text\">成人</strong></section></section></section><section style=\"width: 100%;height: 25px;background-image: linear-gradient(to right, transparent, rgb(182, 228, 253));background-position: initial;background-size: initial;background-repeat: initial;background-attachment: initial;background-origin: initial;background-clip: initial;margin-top: -18px;z-index: 1;transform: skew(20deg);\" data-width=\"100%\"><br></section></section></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- 成人症状相对较轻，但仍可能成为传染源。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- 可使用多西环素或左氧氟沙星治疗。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- 注意避免传染给儿童，尤其是未接种疫苗的婴幼儿。</span></section></section></section></section><section data-role=\"title\" data-id=\"141293\"><section style=\"margin: 10px auto;display: flex;justify-content: center;padding-left: 30px;\"><section><section style=\"display: flex;justify-content: flex-start;\"><section style=\"margin-bottom: -5px;transform: translateX(-30px);-webkit-transform: translateX(-30px);-moz-transform: translateX(-30px);-o-transform: translateX(-30px);\"><section style=\"background-color: rgb(255, 206, 89);border-radius: 25px;padding: 2px 15px;\" class=\"js_darkmode__14\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">康复注意</strong></section></section><section style=\"width: 0px;height: 1px;border-left: 8px solid transparent;border-top: 8px solid rgb(255, 206, 89);margin-left: 20px;\" class=\"js_darkmode__15\"><br></section></section></section><section style=\"display: flex;justify-content: center;\"><section style=\"display: flex;justify-content: space-between;\"><section style=\"padding: 2px 10px;background-color: rgb(30, 155, 232);border-radius: 5px 0px 0px 5px;display: flex;justify-content: space-between;align-items: center;\" class=\"js_darkmode__16\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">科学调养助恢复</strong></section></section><section style=\"flex-shrink: 0;\"><section style=\"display: flex;justify-content: space-between;align-items: flex-start;flex-direction: column;\"><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section></section></section></section></section></section></section></section><section data-id=\"152673\"><section style=\"margin: 10px auto;background-color: rgb(182, 228, 253);border-radius: 10px;padding: 10px;\" class=\"js_darkmode__5\"><section style=\"border-width: 1px;border-style: dashed;border-color: rgb(90, 154, 242);border-radius: 10px;background-color: rgb(255, 255, 255);padding: 13px 10px;\" class=\"js_darkmode__6\"><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- <span style=\"font-weight: bold;\">坚持完成疗程</span>：即使症状减轻，也应按疗程服药，防止复发或耐药。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- <span style=\"font-weight: bold;\">隔离防护</span>：治疗期间应避免与婴幼儿密切接触，减少传播风险。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- <span style=\"font-weight: bold;\">接种疫苗是关键</span>：百日咳疫苗（如百白破疫苗）是预防该病最有效手段，儿童应按时接种，成人可考虑加强免疫。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">- <span style=\"font-weight: bold;\">增强免疫力</span>：保持良好作息、均衡饮食和适度锻炼，有助于加快康复。</span></section></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">百日咳虽“咳”不容缓，但只要早诊断、早治疗，配合科学护理，绝大多数患者都能顺利康复。别让“百日咳”真的咳上一百天，及时就医才是正解。</span></section>"
}

```

*   错误响应:
    
    *   Content-Type: `application/json`
        
    *   字段说明:
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `msg` | `string` | 描述信息。 |
| `data` | `null` | 错误时无数据返回。 |

*   错误响应示例:
    

```json
{
  "code": 500,
  "success": false,
  "msg": "服务异常！",
  "data": null
}

```

### 7. 转换为公众号图文 /convert2gzh 用于将输入的原文内容转换为公众号图文的模版格式。

*   路由：`/convert2gzh`
    
*   请求方式：POST
    
*   Content-Type: `application/json`
    
*   字段说明:
    

| 字段名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `content` | `list[dict]` | 是 | 原文内容（不含大标题）。 |

content数据根据生成的科普内容前端解析而来，type为元素class，如果内含有其他网页元素，则content为数组格式，否则为字符串。

特殊场景：图片元素，type固定为："image"，content是完整的HTML标签，包含style属性。如：<img src="[https://example.com/headline-impact.jpg](https://example.com/headline-impact.jpg\)" style="max-width: 50%;" />

*   请求示例:
    

```json
{
    "content": [
        {
            "type": "text_card",
            "content": [
                {
                    "type": "text",
                    "content": "你知道吗？超过70%的女性一生中至少会经历一次妇科疾病的困扰。你是否也曾在体检报告上看到“宫颈炎”“子宫肌瘤”这样的字眼时感到焦虑？其实，很多妇科问题并不可怕，关键在于早发现、早干预。今天我们就来聊聊那些“藏在裙摆下的健康隐患”，用科学知识武装自己，让身体更安心。"
                }
            ]
        },
        {
            "type": "double_title",
            "content": [
                {
                    "type": "main_title",
                    "content": "妇科常见疾病"
                },
                {
                    "type": "subtitle",
                    "content": "发病率有多高？"
                }
            ]
        },
        {
            "type": "block_card",
            "content": [
                {
                    "type": "numbered_title",
                    "content": "01 宫颈炎"
                },
                {
                    "type": "text",
                    "content": "很多时候，我们花了大量时间写内容，却忽略了最重要的第一步：<span style=\"font-weight: bold;\">吸引点击</span>。一个好的标题，能让你的文章在一秒内抓住用户的眼球。"
                },
                {
                    "type": "image",
                    "content": "<img src=\"https://example.com/headline-impact.jpg\" style=\"max-width: 100%;\">"
                },
                {
                    "type": "numbered_title",
                    "content": "02 内容才是留住粉丝的关键"
                },
                {
                    "type": "text",
                    "content": "如果你想让粉丝持续关注，甚至分享你的文章，那么提供<span style=\"color: #1a73e8;\">有价值、有深度</span>的内容是必不可少的。"
                },
                {
                    "type": "numbered_title",
                    "content": "03 互动带来更多曝光"
                },
                {
                    "type": "text",
                    "content": "除了内容本身，积极与粉丝互动也是提升公众号影响力的重要方式。"
                }
            ]
        },
        {
            "type": "single_title",
            "content": "标题的重要性"
        },
        {
            "type": "block_card",
            "content": [
                {
                    "type": "single_title",
                    "content": "吸引点击"
                },
                {
                    "type": "text",
                    "content": "很多时候，我们花了大量时间写内容，却忽略了最重要的第一步：<span style=\"font-weight: bold;\">吸引点击</span>。一个好的标题，能让你的文章在一秒内抓住用户的眼球。"
                },
                {
                    "type": "image",
                    "content": "<img src=\"https://example.com/headline-impact.jpg\" style=\"max-width: 100%;\">"
                },
                {
                    "type": "text",
                    "content": "如果你想让粉丝持续关注，甚至分享你的文章，那么提供<span style=\"color: #1a73e8;\">有价值、有深度</span>的内容是必不可少的。"
                },
                {
                    "type": "single_title",
                    "content": "提升影响力"
                },
                {
                    "type": "text",
                    "content": "除了内容本身，积极与粉丝互动也是提升公众号影响力的重要方式。"
                }
            ]
        },
        {
            "type": "text",
            "content": "这是我做公众号的第 30 天，后台突然弹出“粉丝破万”的提示。"
        },
        {
            "type": "text",
            "content": "那一刻，我只做了一件事：把标题改了。"
        },
        {
            "type": "image",
            "content": "<img src=\"https://example.com/cover-sample.jpg\" style=\"max-width: 100%; width: 300px;\">"
        },
        {
            "type": "text",
            "content": "用户只给你 0.5 秒的停留时间。"
        }
    ]
}
```

**响应格式**

*   成功响应:
    
    *   Content-Type: `application/json`
        
    *   字段说明:
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `msg` | `string` | 描述信息。 |
| `data` | `object` | 返回文章的正文数据（不含大标题），HTML格式 |

*   成功响应示例:
    

```json
{
    "code": 200,
    "success": true,
    "msg": "ok",
    "data": "<p style=\"text-align: center; visibility: visible;\"><img class=\"rich_pages wxw-img\" data-galleryid=\"\" data-imgfileid=\"503453050\" data-ratio=\"0.159\" data-s=\"300,640\" data-src=\"https://mmbiz.qpic.cn/sz_mmbiz_jpg/o47NALMjEA5yHT4q05ql3hEwjdAlVMlyRULhlE78sQ7D23GK3lKKpVou4ExGsUbnttQXmuLCeiagYoBZxw9bp3g/640?wx_fmt=jpeg&amp;from=appmsg&amp;randomid=wx8dlqk2\" data-type=\"jpeg\" data-w=\"1000\" style=\"height: auto !important; visibility: visible !important; width: 677px !important;\" data-original-style=\"\" data-index=\"1\" src=\"https://mmbiz.qpic.cn/sz_mmbiz_jpg/o47NALMjEA5yHT4q05ql3hEwjdAlVMlyRULhlE78sQ7D23GK3lKKpVou4ExGsUbnttQXmuLCeiagYoBZxw9bp3g/640?wx_fmt=jpeg&amp;from=appmsg&amp;randomid=wx8dlqk2&amp;tp=webp&amp;wxfrom=10005&amp;wx_lazy=1\" _width=\"677px\" alt=\"图片\" data-report-img-idx=\"2\" data-fail=\"0\"></p><section style=\"visibility: visible;\" data-id=\"155504\"><section style=\"margin: 10px auto; visibility: visible;\"><section style=\"width: 25px; margin-left: 15px; margin-bottom: -13px; transform: rotateZ(0deg); visibility: visible;\"><img data-imgfileid=\"503454082\" data-ratio=\"1\" data-src=\"https://mmbiz.qpic.cn/sz_mmbiz_png/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2Gchta1njqpk4Uu1to7bCGl718atVicDzGnicCHIrcDynDp8KbvKiasRibg/640?wx_fmt=png&amp;from=appmsg&amp;randomid=t0vm18cv\" data-type=\"png\" data-w=\"41\" data-width=\"100%\" style=\"width: 25px !important; display: block; vertical-align: baseline; height: auto !important; visibility: visible !important;\" data-original-style=\"width: 100%;display: block;vertical-align: baseline;\" data-index=\"3\" src=\"https://mmbiz.qpic.cn/sz_mmbiz_png/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2Gchta1njqpk4Uu1to7bCGl718atVicDzGnicCHIrcDynDp8KbvKiasRibg/640?wx_fmt=png&amp;from=appmsg&amp;randomid=t0vm18cv&amp;tp=webp&amp;wxfrom=10005&amp;wx_lazy=1\" class=\"\" _width=\"100%\" alt=\"图片\" data-report-img-idx=\"0\" data-fail=\"0\"></section><section style=\"border-width: 1px; border-style: solid; border-color: rgb(89, 195, 249); border-radius: 10px; padding: 15px; visibility: visible;\" class=\"js_darkmode__0\"><section data-autoskip=\"1\" style=\"line-height: 1.75em; letter-spacing: 1.5px; font-size: 14px; color: rgb(51, 51, 51); padding-top: 5px; visibility: visible;\" class=\"js_darkmode__1\"><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">你知道吗？超过70%的女性一生中至少会经历一次妇科疾病的困扰。你是否也曾在体检报告上看到“宫颈炎”“子宫肌瘤”这样的字眼时感到焦虑？其实，很多妇科问题并不可怕，关键在于早发现、早干预。今天我们就来聊聊那些“藏在裙摆下的健康隐患”，用科学知识武装自己，让身体更安心。</span></section></section></section><section style=\"width: 25px;margin-left: auto;margin-top: -14px;margin-right: 20px;transform: rotateZ(0deg);\"><img data-imgfileid=\"503454083\" data-ratio=\"1\" data-src=\"https://mmbiz.qpic.cn/sz_mmbiz_png/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2Gchta1njqpk4Uu1to7bCGl718atVicDzGnicCHIrcDynDp8KbvKiasRibg/640?wx_fmt=png&amp;from=appmsg&amp;randomid=30zzm4s0\" data-type=\"png\" data-w=\"41\" data-width=\"100%\" style=\"width: 25px !important; display: block; vertical-align: baseline; height: auto !important; visibility: visible !important;\" data-original-style=\"width: 100%;display: block;vertical-align: baseline;\" data-index=\"7\" src=\"https://mmbiz.qpic.cn/sz_mmbiz_png/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2Gchta1njqpk4Uu1to7bCGl718atVicDzGnicCHIrcDynDp8KbvKiasRibg/640?wx_fmt=png&amp;from=appmsg&amp;randomid=30zzm4s0&amp;tp=webp&amp;wxfrom=10005&amp;wx_lazy=1\" class=\"\" _width=\"100%\" alt=\"图片\" data-report-img-idx=\"6\" data-fail=\"0\"></section></section></section><section data-role=\"title\" data-id=\"141293\"><section style=\"margin: 10px auto;display: flex;justify-content: center;padding-left: 30px;\"><section><section style=\"display: flex;justify-content: flex-start;\"><section style=\"margin-bottom: -5px;transform: translateX(-30px);-webkit-transform: translateX(-30px);-moz-transform: translateX(-30px);-o-transform: translateX(-30px);\"><section style=\"background-color: rgb(255, 206, 89);border-radius: 25px;padding: 2px 15px;\" class=\"js_darkmode__14\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">妇科常见疾病</strong></section></section><section style=\"width: 0px;height: 1px;border-left: 8px solid transparent;border-top: 8px solid rgb(255, 206, 89);margin-left: 20px;\" class=\"js_darkmode__15\"><br></section></section></section><section style=\"display: flex;justify-content: center;\"><section style=\"display: flex;justify-content: space-between;\"><section style=\"padding: 2px 10px;background-color: rgb(30, 155, 232);border-radius: 5px 0px 0px 5px;display: flex;justify-content: space-between;align-items: center;\" class=\"js_darkmode__16\"><section style=\"font-size: 16px;color: #fffffe;text-align: center;\"><strong data-brushtype=\"text\">发病率有多高？</strong></section></section><section style=\"flex-shrink: 0;\"><section style=\"display: flex;justify-content: space-between;align-items: flex-start;flex-direction: column;\"><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section><section style=\"width: 4px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 7.41 17.86\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M0,0V17.86A9.08,9.08,0,0,0,7.41,8.93,9.08,9.08,0,0,0,0,0Z\" style=\"fill:#1e9be8;fill-rule:evenodd;\"></path></g></g></svg></section></section></section></section></section></section></section></section><section data-id=\"152673\"><section style=\"margin: 10px auto;background-color: rgb(182, 228, 253);border-radius: 10px;padding: 10px;\" class=\"js_darkmode__5\"><section style=\"border-width: 1px;border-style: dashed;border-color: rgb(90, 154, 242);border-radius: 10px;background-color: rgb(255, 255, 255);padding: 13px 10px;\" class=\"js_darkmode__6\"><section style=\"margin: 10px auto;text-align: left;\" data-id=\"105887\" data-tools=\"135编辑器\"><section data-brushtype=\"text\" style=\"font-size: 16px;letter-spacing: 1.5px;color: rgb(62, 157, 215);margin-bottom: -22px;margin-left: 16px;\">01 宫颈炎</section><section style=\"display: flex;justify-content: center;align-items: flex-end;\"><section style=\"width: 5px;height: 22px;background-color: rgb(66, 163, 225);\" class=\"js_darkmode__8\"><br></section><section style=\"width: 100%;height: 3px;background-color0: rgb(255, 238, 194);flex: 1 1 0%;margin-left: 8px;\" data-width=\"100%\" class=\"js_darkmode__9\"><br></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">很多时候，我们花了大量时间写内容，却忽略了最重要的第一步：<span style=\"font-weight: bold;\">吸引点击</span>。一个好的标题，能让你的文章在一秒内抓住用户的眼球。</span></section><p style=\"text-align: center;\"><img src=\"https://example.com/headline-impact.jpg\" style=\"max-width: 100%;\"></p><section style=\"margin: 10px auto;text-align: left;\" data-id=\"105887\" data-tools=\"135编辑器\"><section data-brushtype=\"text\" style=\"font-size: 16px;letter-spacing: 1.5px;color: rgb(62, 157, 215);margin-bottom: -22px;margin-left: 16px;\">02 内容才是留住粉丝的关键</section><section style=\"display: flex;justify-content: center;align-items: flex-end;\"><section style=\"width: 5px;height: 22px;background-color: rgb(66, 163, 225);\" class=\"js_darkmode__8\"><br></section><section style=\"width: 100%;height: 3px;background-color0: rgb(255, 238, 194);flex: 1 1 0%;margin-left: 8px;\" data-width=\"100%\" class=\"js_darkmode__9\"><br></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">如果你想让粉丝持续关注，甚至分享你的文章，那么提供<span style=\"color: #1a73e8;\">有价值、有深度</span>的内容是必不可少的。</span></section><section style=\"margin: 10px auto;text-align: left;\" data-id=\"105887\" data-tools=\"135编辑器\"><section data-brushtype=\"text\" style=\"font-size: 16px;letter-spacing: 1.5px;color: rgb(62, 157, 215);margin-bottom: -22px;margin-left: 16px;\">03 互动带来更多曝光</section><section style=\"display: flex;justify-content: center;align-items: flex-end;\"><section style=\"width: 5px;height: 22px;background-color: rgb(66, 163, 225);\" class=\"js_darkmode__8\"><br></section><section style=\"width: 100%;height: 3px;background-color0: rgb(255, 238, 194);flex: 1 1 0%;margin-left: 8px;\" data-width=\"100%\" class=\"js_darkmode__9\"><br></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">除了内容本身，积极与粉丝互动也是提升公众号影响力的重要方式。</span></section></section></section></section><section data-role=\"title\" data-tools=\"135编辑器\" data-id=\"158694\"><section style=\"margin: 10px auto;display: flex;justify-content: center;\"><section><section style=\"width: 26px;margin-bottom: -7px;margin-left: -14px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 135.21 135.21\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M67.6,0l2.25,27.81C72.09,47,88.22,63.12,107.4,65.36l27.81,2.24L107.4,69.85C88.22,72.09,72.09,88.22,69.85,107.4L67.6,135.21,65.36,107.4C63.12,88.22,47,72.09,27.81,69.85L0,67.6l27.81-2.24C47,63.12,63.12,47,65.36,27.81Z\" style=\"fill:#ffd220;\"></path></g></g></svg></section><section style=\"display: flex;flex-direction: column;\"><section style=\"background-color: rgb(30, 155, 232);padding: 9px 14px;z-index: 4;transform: skew(-15deg);\"><section style=\"transform: skew(15deg);-webkit-transform: skew(15deg);-moz-transform: skew(15deg);-o-transform: skew(15deg);\"><section style=\"font-size: 16px;color: #ffffff;text-align: center;\"><strong data-brushtype=\"text\">标题的重要性</strong></section></section></section><section style=\"width: 100%;height: 25px;background-image: linear-gradient(to right, transparent, rgb(182, 228, 253));background-position: initial;background-size: initial;background-repeat: initial;background-attachment: initial;background-origin: initial;background-clip: initial;margin-top: -18px;z-index: 1;transform: skew(20deg);\" data-width=\"100%\"><br></section></section></section></section></section><section data-id=\"152673\"><section style=\"margin: 10px auto;background-color: rgb(182, 228, 253);border-radius: 10px;padding: 10px;\" class=\"js_darkmode__5\"><section style=\"border-width: 1px;border-style: dashed;border-color: rgb(90, 154, 242);border-radius: 10px;background-color: rgb(255, 255, 255);padding: 13px 10px;\" class=\"js_darkmode__6\"><section data-role=\"title\" data-tools=\"135编辑器\" data-id=\"158694\"><section style=\"margin: 10px auto;display: flex;justify-content: center;\"><section><section style=\"width: 26px;margin-bottom: -7px;margin-left: -14px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 135.21 135.21\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M67.6,0l2.25,27.81C72.09,47,88.22,63.12,107.4,65.36l27.81,2.24L107.4,69.85C88.22,72.09,72.09,88.22,69.85,107.4L67.6,135.21,65.36,107.4C63.12,88.22,47,72.09,27.81,69.85L0,67.6l27.81-2.24C47,63.12,63.12,47,65.36,27.81Z\" style=\"fill:#ffd220;\"></path></g></g></svg></section><section style=\"display: flex;flex-direction: column;\"><section style=\"background-color: rgb(30, 155, 232);padding: 9px 14px;z-index: 4;transform: skew(-15deg);\"><section style=\"transform: skew(15deg);-webkit-transform: skew(15deg);-moz-transform: skew(15deg);-o-transform: skew(15deg);\"><section style=\"font-size: 16px;color: #ffffff;text-align: center;\"><strong data-brushtype=\"text\">吸引点击</strong></section></section></section><section style=\"width: 100%;height: 25px;background-image: linear-gradient(to right, transparent, rgb(182, 228, 253));background-position: initial;background-size: initial;background-repeat: initial;background-attachment: initial;background-origin: initial;background-clip: initial;margin-top: -18px;z-index: 1;transform: skew(20deg);\" data-width=\"100%\"><br></section></section></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">很多时候，我们花了大量时间写内容，却忽略了最重要的第一步：<span style=\"font-weight: bold;\">吸引点击</span>。一个好的标题，能让你的文章在一秒内抓住用户的眼球。</span></section><p style=\"text-align: center;\"><img src=\"https://example.com/headline-impact.jpg\" style=\"max-width: 100%;\"></p><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">如果你想让粉丝持续关注，甚至分享你的文章，那么提供<span style=\"color: #1a73e8;\">有价值、有深度</span>的内容是必不可少的。</span></section><section data-role=\"title\" data-tools=\"135编辑器\" data-id=\"158694\"><section style=\"margin: 10px auto;display: flex;justify-content: center;\"><section><section style=\"width: 26px;margin-bottom: -7px;margin-left: -14px;\"><svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 135.21 135.21\" style=\"display: block;\"><g data-name=\"图层 2\"><g data-name=\"图层 1\"><path d=\"M67.6,0l2.25,27.81C72.09,47,88.22,63.12,107.4,65.36l27.81,2.24L107.4,69.85C88.22,72.09,72.09,88.22,69.85,107.4L67.6,135.21,65.36,107.4C63.12,88.22,47,72.09,27.81,69.85L0,67.6l27.81-2.24C47,63.12,63.12,47,65.36,27.81Z\" style=\"fill:#ffd220;\"></path></g></g></svg></section><section style=\"display: flex;flex-direction: column;\"><section style=\"background-color: rgb(30, 155, 232);padding: 9px 14px;z-index: 4;transform: skew(-15deg);\"><section style=\"transform: skew(15deg);-webkit-transform: skew(15deg);-moz-transform: skew(15deg);-o-transform: skew(15deg);\"><section style=\"font-size: 16px;color: #ffffff;text-align: center;\"><strong data-brushtype=\"text\">提升影响力</strong></section></section></section><section style=\"width: 100%;height: 25px;background-image: linear-gradient(to right, transparent, rgb(182, 228, 253));background-position: initial;background-size: initial;background-repeat: initial;background-attachment: initial;background-origin: initial;background-clip: initial;margin-top: -18px;z-index: 1;transform: skew(20deg);\" data-width=\"100%\"><br></section></section></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">除了内容本身，积极与粉丝互动也是提升公众号影响力的重要方式。</span></section></section></section></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">这是我做公众号的第 30 天，后台突然弹出“粉丝破万”的提示。</span></section><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">那一刻，我只做了一件事：把标题改了。</span></section><p style=\"text-align: center;\"><img src=\"https://example.com/cover-sample.jpg\" style=\"max-width: 100%; width: 300px;\"></p><section style=\"text-indent: 2em;line-height: 1.6em;letter-spacing: 1px;\"><span style=\"text-indent: 0em;font-size: 15px;\">用户只给你 0.5 秒的停留时间。</span></section><p style=\"text-align: center;\"><img class=\"rich_pages wxw-img\" data-galleryid=\"\" data-imgfileid=\"503452183\" data-ratio=\"0.43796296296296294\" data-s=\"300,640\" data-src=\"https://mmbiz.qpic.cn/sz_mmbiz_jpg/o47NALMjEA5NWKWWQKdW66FIdnKbJUcbpqzSonTmd9cT60SnhAsZhgsc6SdTtTiaLADewMTibnAZiahLnOndcNScw/640?wx_fmt=jpeg&amp;from=appmsg&amp;randomid=q1s7qb7d\" data-type=\"jpeg\" data-w=\"1080\" style=\"height: auto !important; visibility: visible !important; width: 677px !important;\" data-original-style=\"\" data-index=\"17\" src=\"https://mmbiz.qpic.cn/sz_mmbiz_jpg/o47NALMjEA5NWKWWQKdW66FIdnKbJUcbpqzSonTmd9cT60SnhAsZhgsc6SdTtTiaLADewMTibnAZiahLnOndcNScw/640?wx_fmt=jpeg&amp;from=appmsg&amp;randomid=q1s7qb7d&amp;tp=webp&amp;wxfrom=10005&amp;wx_lazy=1\" _width=\"677px\" alt=\"图片\" data-report-img-idx=\"16\" data-fail=\"0\"></p><section data-tools=\"135编辑器\" data-id=\"131804\" style=\"margin-bottom: 0px;\"><section style=\"text-align: right;\"><section style=\"display: inline-block;\"><section style=\"display: flex;align-items: flex-end;\"><section style=\"display: flex;z-index: 9;transform: translateX(65px);-webkit-transform: translateX(65px);-moz-transform: translateX(65px);-o-transform: translateX(65px);\"><section style=\"width: 55px;margin-bottom: 12px;\"><img class=\"rich_pages wxw-img __bg_gif\" data-imgfileid=\"503454138\" data-ratio=\"1\" data-src=\"https://mmbiz.qpic.cn/sz_mmbiz_gif/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2vWlOQYRrr53XxEk4vzWUl9DUymX0YzzZbRthQMmia9Y9Qp84vePdhpg/640?wx_fmt=gif&amp;from=appmsg&amp;randomid=aflmg9g9\" data-type=\"gif\" data-w=\"640\" data-width=\"100%\" style=\"width: 55px !important; display: block; vertical-align: baseline; height: auto !important; visibility: visible !important;\" data-original-style=\"width: 100%;display: block;vertical-align: baseline;\" data-index=\"18\" src=\"https://mmbiz.qpic.cn/sz_mmbiz_gif/o47NALMjEA6CeBLFlydfP8K9DvdqyOw2vWlOQYRrr53XxEk4vzWUl9DUymX0YzzZbRthQMmia9Y9Qp84vePdhpg/640?wx_fmt=gif&amp;from=appmsg&amp;randomid=aflmg9g9&amp;tp=webp&amp;wxfrom=10005&amp;wx_lazy=1\" _width=\"100%\" data-order=\"0\" alt=\"图片\" data-report-img-idx=\"17\" data-fail=\"0\"></section></section><section><section style=\"background-color: rgb(255, 246, 217);border-radius: 25px;padding: 1px 15px 1px 55px;\" class=\"js_darkmode__42\"><span style=\"font-size: 14px;text-align: justify;color: #333333;\"><strong data-brushtype=\"text\">点个「赞」和「在看」再走吧</strong></span></section><section style=\"display: flex;justify-content: flex-end;padding-right: 2em;\"><section style=\"width: 0px;height: 1px;border-left: 6px solid transparent;border-right: 6px solid transparent;border-top: 12px solid rgb(255, 246, 217);\" class=\"js_darkmode__43\"><br></section></section></section></section></section></section></section>"
}
```

*   错误响应:
    
    *   Content-Type: `application/json`
        
    *   字段说明:
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `msg` | `string` | 描述信息。 |
| `data` | `null` | 错误时无数据返回。 |

*   错误响应示例:
    

```json
{
  "code": 500,
  "success": false,
  "msg": "服务异常！",
  "data": null
}

```

### 8. 科普内容纠错（重新生成）/science-regenerator 更新已生成的科普文章。支持流式输出。

*   路由：`/science-regenerator`
    
*   请求方式：POST
    
*   Content-Type: `application/json`
    
*   字段说明:
    

| 字段名 | 类型 | 必填 | 默认值 | 描述 |
| --- | --- | --- | --- | --- |
| `content` | `string` | 是 | \- | 科普内容原文。 |

*   请求示例:
    

```json
{
  "content": "# 百日咳的诊断与治疗全攻略↵百日咳，听上去像是“咳一百天年”，其实它还真不是夸张。这个病名来源于其漫长的病程，咳嗽可能持续数周甚至数月。百日咳是一种由百日咳杆菌引起的急性呼吸道传染病，尤其在儿童中高发，但成人也不能掉以轻心。今天我们就来聊聊百日咳的诊断与治疗，帮你科学应对这场“持久战”。↵## 一、典型临床表现↵百日咳的病程可分为三个阶段：↵1. **卡他期（初期）**：  ↵   持续约1～2周，症状类似普通感冒，包括流涕、打喷嚏、轻咳、低热等，容易被忽视或误诊。↵2. **痉咳期（高峰期）**：  ↵   通常在发病后1～2周进入此阶段，持续2～6周甚至更久。典型表现为阵发性、痉挛性咳嗽，咳嗽剧烈且频繁，常伴有“鸡鸣样”吸气声，咳后呕吐也较常见。儿童尤其容易出现这种特征性表现。↵3. **恢复期（缓解期）**：  ↵   咳嗽频率和强度逐渐减轻，但仍可能持续数周，遇冷、烟雾等刺激时容易复发。↵## 二、实验室诊断方法↵确诊百日咳不能只靠“听咳辨病”，还需借助科学手段：↵1. **鼻咽拭子检测**：  ↵   通过PCR技术检测百日咳杆菌的DNA，敏感性高、出结果快，是早期诊断的首选方法。↵2. **血清学检查**：  ↵   检测特异性抗体水平，适用于病程中后期或无法采集呼吸道样本的患者。↵3. **细菌培养**：  ↵   虽然特异性高，但对技术和时间要求较高，临床应用相对较少。↵## 三、治疗方案↵### 1. 药物治疗↵百日咳一旦确诊，应尽早开始抗菌治疗，以缩短病程并减少传染性。↵- **首选药物**：大环内酯类抗生素（如阿奇霉素、克拉霉素、红霉素）  ↵  特别适用于儿童，尤其是婴幼儿。↵- **替代方案**：四环素类（如多西环素）适用于8岁以上儿童及成人；喹诺酮类（如左氧氟沙星）适用于18岁以上成人。↵- **家庭成员预防性用药**：密切接触者即使无症状，也建议预防性使用抗生素，防止传播。↵### 2. 对症治疗与护理↵- **止咳治疗**：目前尚无特效止咳药，但可使用镇咳剂缓解症状，尤其在夜间影响睡眠时。↵- **止吐与营养支持**：咳嗽剧烈导致呕吐者，可适当使用止吐药，并注意补充水分和营养。↵- **环境管理**：保持空气流通，避免烟雾、灰尘等刺激物，饮食宜清淡易消化。↵- **休息与观察**：尤其婴幼儿需密切观察是否有呼吸暂停、缺氧等严重并发症。↵## 四、不同人群治疗建议↵### 儿童（特别是婴幼儿）↵- 抗生素首选红霉素或阿奇霉素，需严格遵医嘱调整剂量。↵- 婴幼儿病情进展快，建议早期住院治疗。↵- 家庭护理要特别注意防止呛咳引发窒息。↵### 成人↵- 成人症状相对较轻，但仍可能成为传染源。↵- 可使用多西环素或左氧氟沙星治疗。↵- 注意避免传染给儿童，尤其是未接种疫苗的婴幼儿。↵## 五、康复注意事项↵- **坚持完成疗程**：即使症状减轻，也应按疗程服药，防止复发或耐药。↵- **隔离防护**：治疗期间应避免与婴幼儿密切接触，减少传播风险。↵- **接种疫苗是关键**：百日咳疫苗（如百白破疫苗）是预防该病最有效手段，儿童应按时接种，成人可考虑加强免疫。↵- **增强免疫力**：保持良好作息、均衡饮食和适度锻炼，有助于加快康复。↵---↵百日咳虽“咳”不容缓，但只要早诊断、早治疗，配合科学护理，绝大多数患者都能顺利康复。别让“百日咳”真的咳上一百天，及时就医才是正解。"
}

```

**响应格式**： 同科普内容生成接口（/science-generator）

### 二、数字人接口 

### 用于管理数字人模特、训练模型以及根据文本生成视频。

*   **基础路径**: `/dh`
    

---

#### 1. 获取作品列表（待开放）

....

#### 2. 获取数字模特列表 /dh/models/

获取当前用户已训练的数字人模特列表。

*   **路由**: `GET /dh/models`
    
*   **请求方式**: GET
    
*   **请求参数**: 无
    
*   **响应格式**
    
    *   Content-Type: `application/json`
        
    *   字段说明:
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `msg` | `string` | 描述信息。 |
| `data` | `object` | 响应数据。错误时无数据返回。 |

*   成功响应字段说明 (`data` 字段):
    
    *   `List[object]`: 用户数字人模特信息列表。
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `name` | `string` | 模特名称。 |
| `video` | `string` | 关联视频文件的url。（需拼接接口域名+端口） |
| `thumbnail` | `string` | 模特封面图url。（需拼接接口域名+端口） |
| `preview` | `string` | 模特视频预览图图url。（需拼接接口域名+端口） |

*   **成功响应示例**:
    

```json
{
  "code": 200,
  "success": true,
  "msg": "ok",
  "data": [
    {
      "name": "模特A",
      "video": "/media/bhnw/模特A.mp4",
      "thumbnail": "/media/bhnw/模特A_thumbnail_s.jpg",
      "preview": "/media/bhnw/模特A_thumbnail.jpg"
    },
    {
      "name": "销售员B",
      "video": "/media/bhnw/销售员B.mp4",
      "thumbnail": "/media/bhnw/销售员B_thumbnail_s.jpg",
      "preview": "/media/bhnw/销售员B_thumbnail.jpg"
    }
  ]
}

```

*   **错误响应示例**:
    

```json
{
  "code": 500,
  "success": false,
  "msg": "获取模特列表失败",
  "data": null
}

```
---

#### 3. 上传视频训练数字人 /dh/train/

上传一个视频文件，从中提取音频并训练数字人语音模型。

*   **路由**: `POST /dh/train`
    
*   **请求方式**: POST
    
*   **Content-Type**: `multipart/form-data`
    
*   **请求参数**:
    

| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `file` | `file` | 是 | 要上传的视频文件。 |
| `model_name` | `string` | 是 | 为训练的数字人模特指定的名称。 |

*   **响应格式**
    
    *   Content-Type: `application/json`
        
    *   字段说明:
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `msg` | `string` | 描述信息。 |
| `data` | `object` | 响应数据。错误时无数据返回。 |

*   成功响应字段说明 (`data` 字段):
    

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `model_name` | `string` | 训练的模特名称。 |
| `video_url` | `string` | 保存的视频文件URL。 |

*   **成功响应示例**:
    

```json
{
  "code": 200,
  "success": true,
  "msg": "ok",
  "data": {
    "model_name": "我的数字人",
    "video_url": "/file/media/bhnw/我的数字人.mp4"
  }
}

```

*   **错误响应示例**:
    

```json
{
  "code": 400,
  "success": false,
  "msg": "请先上传视频文件",
  "data": null
}

```
```json
{
  "code": 500,
  "success": false,
  "msg": "训练过程中发生错误: 音频提取服务暂时不可用",
  "data": null
}

```
---

#### 4. 通过文字生成数字人视频 /dh/generate/

使用指定的已训练数字人模特和输入文本，生成一段视频。

*   **路由**: `POST /dh/generate`
    
*   **请求方式**: POST
    
*   **Content-Type**: `application/json`
    
*   **请求参数**:
    

| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `model_name` | `string` | 是 | 要使用的数字人模特名称。 |
| `text` | `string` | 是 | 要合成为语音并驱动数字人的文本内容。 |
| `voice` | `string` | 是 | 语音音色。目前可选：男性音色：Male\_Voice\_1、Male\_Voice\_2<br>女性音色：Female\_Voice\_1、Female\_Voice\_2 |
| `text_split_len` | `int` | 否 | 提取音频字幕 -> 文本超过长度后自动进行切分。默认值：10，单位：字符 |
| `speech_pauses_split_ms` | `int` | 否 | 提取音频字幕 -> 说话停顿一定时间后切分文本。默认值：100，单位：ms |

*   **请求体示例**:
    

```json
{
  "model_name": "女医生",
  "text": "大家好，我是你的数字人助手。今天天气真不错！"
}

```

*   **响应格式**
    
    *   Content-Type: `application/json`
        
    *   字段说明:
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `msg` | `string` | 描述信息。 |
| `data` | `object` | task\_id：生成视频任务的唯一标识符<br>audio\_data：视频字幕数据，text：字幕内容，time：字幕起止时间 |

*   **成功响应示例**:
    

```json
{
    "code": 200,
    "success": true,
    "msg": "ok",
    "data": {
        "task_id": "ca9d0100-c117-45b1-bb57-82d0eaa02d49",
        "audio_data": [
            {
                "text": "大家好我是你的数字人",
                "time": [
                    "00:00:00,170",
                    "00:00:01,670"
                ]
            },
            {
                "text": "助手今天",
                "time": [
                    "00:00:01,670",
                    "00:00:02,430"
                ]
            },
            {
                "text": "天气真不错",
                "time": [
                    "00:00:02,590",
                    "00:00:03,370"
                ]
            }
        ]
    }
}

```

*   **错误响应示例**:
    

```json
{
  "code": 400,
  "success": false,
  "msg": "请选择数字人模特",
  "data": null
}

```
---

#### 5. 通过音频生成数字人视频 /dh/generate\_by\_audio

使用指定的已训练数字人模特和音频文件，生成一段视频。

*   **路由**: `POST /dh/generate_by_audio`
    
*   **请求方式**: POST
    
*   **Content-Type**: `multipart/form-data`
    
*   **请求参数**:
    

| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| `file` | `file` | 是 | 要上传的音频文件。 |
| `model_name` | `string` | 是 | 为训练的数字人模特指定的名称。 |

*   **响应格式**
    
    *   Content-Type: `application/json`
        
    *   字段说明:
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `code` | `integer` | 状态码。 |
| `success` | `boolean` | 操作是否成功。 |
| `msg` | `string` | 描述信息。 |
| `data` | `string` | 生成视频任务的唯一标识符 (task\_id)。 |

*   **成功响应示例**:
    

```json
{
    "code": 200,
    "success": true,
    "msg": "ok",
    "data": "a4444dd9-6d38-433f-8768-480b14609d80"
}

```

*   **错误响应示例**:
    

```json
{
  "code": 400,
  "success": false,
  "msg": "请选择数字人模特",
  "data": null
}

```
---

#### 6. 检查任务状态 /dh/status/{task\_id}

根据任务ID查询视频生成任务的当前状态。

*   **路由**: `GET /dh/status/{task_id}`
    
*   **请求方式**: GET
    
*   **路径参数**:
    
    *   `task_id` (`string`, 必填): 视频生成任务的唯一标识符。
        
*   **查询参数**: 无
    
*   **响应格式**
    
    *   Content-Type: `application/json`
        
    *   成功响应字段说明 (`data` 字段):
        

| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| `status` | `string` | 任务状态 ("processing", "completed")。 |
| `progress` | `integer` | 任务进度百分比 (0-100)。仅在 processing 状态时有效。 |
| `result_url` | `string` 或 `null` | 生成完成视频的访问URL。仅在 completed 状态时有效。 |

*   错误响应字段说明 (`msg` 字段):
    
    *   `string`: 错误信息。
        
*   **成功响应示例 (处理中)**:
    

```json
{
  "code": 200,
  "success": true,
  "msg": "ok",
  "data": {
    "status": "processing",
    "progress": 65,
    "result_url": null
  }
}

```

*   **成功响应示例 (已完成)**:
    

```json
{
  "code": 200,
  "success": true,
  "msg": "ok",
  "data": {
    "status": "completed",
    "progress": 100,
    "result_url": "/file/media/bhnw/generated_video_abc123.mp4"
  }
}

```

*   **错误响应示例 (任务失败)**:
    

```json
{
  "code": 200,
  "success": false,
  "msg": "视频合成过程中发生内部错误",
  "data": null
}

```