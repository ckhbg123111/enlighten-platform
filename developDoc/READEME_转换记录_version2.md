# 媒体转换记录 v2.0
## 说明
- 新增接口需要鉴权
- 代码风格和其他接口保持一致
- 持久层建表方式和现有代码保持统一

## 表结构
- 主键ID
- user_id
- 关联的外部ID (当前特指 关联的 gzh_article 的 id)
- platform (包含： gzh, xiaohongshu, douyin 等)
- 状态 (包含：转换中，转换成功，转换失败)
- 创建时间
- 更新时间

## 需求
- 提供一个查询接口，写在 MediaConvertController 中
- 要求，请求参数包含 platform, user_id 获取方式同其他接口
- 持久层提供一个插入接口和更新接口，插入接口返回主键ID
- MediaConvertController 的 applyTemplateByRecord 方法中，转换前插入一条记录，转换成功后更新状态为成功，转换失败后更新状态为失败
