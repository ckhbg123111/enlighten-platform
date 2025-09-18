# 模板需求

## 表结构
- 模板表
  - id 自增主键 bigint
  - name 模板名称 非空，varchar
  - hospital 所属医院 varchar
  - department 所属科室 varchar
  - tag 标签 varchar （字段预留，暂时不用）
  - sort 顺序
  - deleted 是否删除
  - header 模板头 text类型
  - footer 模板脚 text类型
  - text 正文模板 text类型
  - image 图片样式 text类型
  - single_title 单行标题样式 text类型
  - double_title 多行标题样式 text类型
  - text_card 文本框样式 text类型
  - block_card 图文框样式 text类型
  - numbered_title 副标题样式 text类型
  - create_time 创建时间
  - update_time 更新时间

## 需求
- 基本要求
    鉴权、用户ID获取方式、代码风格、异常处理、日志打印等与项目现有风格保持一致
- 模板的查询接口
  - GET请求
  - 支持分页
  - 要求按照用户所在医院和科室过滤
  - 返回值：模板表中除 医院、科室、创建时间、更新时间、删除标志 之外的所有字段
  - 控制器名称为 
  

  