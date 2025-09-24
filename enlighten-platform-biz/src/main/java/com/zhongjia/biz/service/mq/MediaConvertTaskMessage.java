package com.zhongjia.biz.service.mq;

import lombok.Data;

@Data
public class MediaConvertTaskMessage {
    /** v2记录ID */
    private Long recordV2Id;
    /** 业务外部记录ID，如 gzhArticle.id */
    private Long externalId;
    /** 模板ID */
    private Long templateId;
    /** 发起人用户ID */
    private Long userId;
    /** 平台标识，例如 gzh */
    private String platform;
    /** 文章原文（apply_template直接传入）*/
    private String essay;
    /** traceId 用于链路跟踪 */
    private String traceId;
}


