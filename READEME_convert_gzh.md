# 转公众号
该接口功能已经实现，接口文档在 AI 科普平台 API 文档 v1.6.2.md 中的第6节 转换为公众号图文(重新生成) /convert2gzh_re   第七节转换为公众号图文 /convert2gzh 中实现

## 需求
你需要做的是对这两个接口进行包装，方便前端调用。

## 要求
1. 接口包装在MediaConvertController中
2. 逻辑等同于MediaConvertController 中的common接口
3. 返回不要透传，而是包装上游的data
4. 请求参数中加上 String essayCode; 额外字段 和 String mediaCode; 媒体唯一编码 uuid
5. platform类型固定为ghz