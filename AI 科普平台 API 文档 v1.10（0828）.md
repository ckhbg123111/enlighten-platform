# AI 科普平台 API 文档 v1.10（0828）

### DEMO

*   [http://192.168.1.65:8000/demo](http://192.168.1.65:8000/demo)
    

### 一、基础API 接口

### 1. 患者端对话 /science-chat 携带上下文对话接口。支持流式输出。

*   路由：`/science-chat`
    
*   请求方式：POST
    
*   Content-Type: `application/json`
    
*   字段说明:
    

| 字段名 | 类型 | 必填 | 默认值 | 描述 |
| --- | --- | --- | --- | --- |
| `messages` | List\[Dict\] | 是 | \- | 聊天对话历史（**需要控制携带对话记录条数，比如10条**） |
| `need_recommend` | `boolean` | 否 | `true` | 是否需要返回推荐问题 |
| `prompt` | `string` | 否 | `null` | 自定义用于调整AI对话风格的系统提示词，留空则使用代码预置提示词。 |

> `**messages**`**字段说明：**

*   role: 对话角色，user：用户消息； assistant：AI回答
    
*   content：对话内容
    
    *   纯文字对话，该值类型为字符串
        
    *   **携带图片对话**，该值类型为 List 列表，可以携带多张图片或者文字内容，格式如下：
        

```json
[
  {
    "type": "image_url",
    "image_url": {
      "url": "图片url或者base64编码"
    }
  },
  {
    "type": "text",
    "text": "用户消息内容"
  }
]

```

*   请求示例:
    

```json
{
  "messages": [
    {
      "role": "user",
      "content": "你是谁。"
    },
    {
      "role": "assistant",
      "content": "您好，我是赣州人民医院的科普助手，可以为您提供专业的医学健康知识解答。有什么健康问题我可以帮您吗？"
    },
    {
      "role": "user",
      "content": [
        {
          "type": "image_url",
          "image_url": {
            "url": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAAAeCAIAAABVOSykAAABfUlEQVR42u2ZUQqDMAyG8zrYBXZPD7DT7gLCnicIRdI2Jn9TG53Ql4FZ/3z9E2tL8+d7D+WgluD3NC3jT2FZk3eHFZb+KoxCaY0DiykpwOo6d6eHj1w2OmbWNLHSO/IzowxIQ/zc6K/LwoLLKmCn94H1fD2W4Z5zV16APcmLFAZLju3KC+ieHWHtqklRo3hZ3U1eBWiFxQDJvISFUQa69AFPWCbRcM6MV/ppRbaGsJ3NxWFh5lLyVcFSNr/tqpoU5w8HhJVDcIAFKIZh5UaGSRX7rPxiIXj3wVQuUVZ35LiPh2V6CxO8SWsppVraXWGl7DSxRRSE7WXzPgW3LQyWdVJGarfPlmE5frhhvExRGKytWk2frbmHDvikspq0dsiT2mJuE0FejZTAq7b5ojnqScCWkUBHhsUEa2DlkyKwTnc3USS122eF0qEhpRfnEI3xWqNwWGe87II1y7agCKR8ndtCSg6nIEaI419PWNe+hZZTu2H1hHWW2nRZVPYnP2wlBq4PIuulAAAAAElFTkSuQmCC"
          }
        },
        {
          "type": "image_url",
          "image_url": {
            "url": "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png"
          }
        },
        {
          "type": "text",
          "text": "图片上面是什么"
        }
      ]
    }
  ],
  "need_recommend": true
}

```

**响应格式**

*   Content-Type: `text/event-stream`
    

> 接口采用 **Server-Sent Events (SSE)** 流式输出。客户端将持续接收 `data:` 前缀的文本行，每行包含一个 JSON 片段。客户端需要解析每个 data: 行后面的 JSON 数据，并将其中的 `choices[0].delta.content` 字段内容进行拼接，以构建完整的生成结果。通过判断  
`choices[0].finish_reason === "stop"` 判断响应是否结束

> **响应字段**`**type**`**说明**：

> **当前接口目前有两种可选值：**`**llm_token**` **和** `**recommend_question**`

`llm_token`：正常的内容输出

`recommend_question`：推荐的问题。可能有多条，每个 Chunk 是一条。

*   响应示例:
    

```plaintext
data: {"model":"zj-llm","type":"llm_token","id":"chatcmpl-4ae1ac12-2283-9305-a477-c1aff97e5174","created":1756197366,"choices":[{"index":0,"finish_reason":"null","delta":{"role":"assistant","content":""}}]}

data: {"model":"zj-llm","type":"llm_token","id":"chatcmpl-4ae1ac12-2283-9305-a477-c1aff97e5174","created":1756197366,"choices":[{"index":0,"finish_reason":"null","delta":{"role":"assistant","content":"\u60a8\u597d"}}]}

data: {"model":"zj-llm","type":"llm_token","id":"chatcmpl-4ae1ac12-2283-9305-a477-c1aff97e5174","created":1756197366,"choices":[{"index":0,"finish_reason":"null","delta":{"role":"assistant","content":"\uff0c"}}]}

data: {"model":"zj-llm","type":"llm_token","id":"chatcmpl-4ae1ac12-2283-9305-a477-c1aff97e5174","created":1756197366,"choices":[{"index":0,"finish_reason":"null","delta":{"role":"assistant","content":"\u5173\u4e8e"}}]}

data: {"model":"zj-llm","type":"llm_token","id":"chatcmpl-4ae1ac12-2283-9305-a477-c1aff97e5174","created":1756197366,"choices":[{"index":0,"finish_reason":"null","delta":{"role":"assistant","content":"\u7cd6\u5c3f\u75c5"}}]}

data: {"model":"zj-llm","type":"llm_token","id":"chatcmpl-4ae1ac12-2283-9305-a477-c1aff97e5174","created":1756197366,"choices":[{"index":0,"finish_reason":"null","delta":{"role":"assistant","content":"\u7684\u6cbb\u7597\uff0c\u6211"}}]}

...

data: {"model":"zj-llm","type":"llm_token","id":"chatcmpl-4ae1ac12-2283-9305-a477-c1aff97e5174","created":1756197366,"choices":[{"index":0,"finish_reason":"null","delta":{"role":"assistant","content":"\u5efa\u8bae\u54e6\u3002"}}]}

data: {"model":"zj-llm","type":"llm_token","id":"chatcmpl-4ae1ac12-2283-9305-a477-c1aff97e5174","created":1756197366,"choices":[{"index":0,"finish_reason":"null","delta":{"role":"assistant","content":""}}]}

data: {"model":"zj-preset","type":"recommend_question","choices":[{"finish_reason":"null","delta":{"role":"assistant","content":"\u7cd6\u5c3f\u75c5\u7684\u996e\u98df\u63a7\u5236\u5177\u4f53\u6709\u54ea\u4e9b\u6ce8\u610f\u4e8b\u9879\uff1f"}}]}

data: {"model":"zj-preset","type":"recommend_question","choices":[{"finish_reason":"null","delta":{"role":"assistant","content":"\u7cd6\u5c3f\u75c5\u60a3\u8005\u5982\u4f55\u901a\u8fc7\u8fd0\u52a8\u6765\u8f85\u52a9\u6cbb\u7597\uff1f"}}]}

data: {"model":"zj-preset","type":"recommend_question","choices":[{"finish_reason":"null","delta":{"role":"assistant","content":"\u7cd6\u5c3f\u75c5\u5e38\u89c1\u7684\u5e76\u53d1\u75c7\u6709\u54ea\u4e9b\uff0c\u5982\u4f55\u9884\u9632\uff1f"}}]}

data: {"model":"zj-preset","type":"llm_token","choices":[{"finish_reason":"null","delta":{"role":"assistant","content":""}}]}

data: {"model":"zj-preset","type":"llm_token","choices":[{"finish_reason":"stop","delta":{"role":"assistant","content":""}}]}

```
