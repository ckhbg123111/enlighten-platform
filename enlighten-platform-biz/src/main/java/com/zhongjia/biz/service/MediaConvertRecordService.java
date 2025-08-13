package com.zhongjia.biz.service;

import com.zhongjia.biz.service.dto.UpstreamResult;

/**
 * 媒体转化记录服务：承接上游交互与记录持久化
 */

public interface MediaConvertRecordService {
    /**
     * 通用媒体转换。负责：参数校验、创建记录、调用上游、更新记录并返回上游结果。
     */
    UpstreamResult convertCommon(Long userId, Long tenantId, String mediaCode, String essayCode, String content, String platform);

    /**
     * 公众号重新生成（纯文本）。
     */
    UpstreamResult convertGzhRe(Long userId, Long tenantId, String mediaCode, String essayCode, String content);

    /**
     * 公众号按结构化内容生成。contentJson 为 JSON 字符串。
     */
    UpstreamResult convertGzh(Long userId, Long tenantId, String mediaCode, String essayCode, String contentJson);
}


