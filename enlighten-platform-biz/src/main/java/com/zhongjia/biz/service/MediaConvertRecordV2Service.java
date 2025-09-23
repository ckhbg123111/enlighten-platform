package com.zhongjia.biz.service;

public interface MediaConvertRecordV2Service {
    /** 插入或更新为 PROCESSING，返回记录ID */
    Long insertOrUpdateProcessing(Long userId, Long externalId, String platform);

    /** 将记录更新为成功，并写入原文与生成内容 */
    boolean markSuccess(Long id, String originalText, String generatedText);

    /** 将记录更新为失败，并写入原文 */
    boolean markFailed(Long id, String originalText);
}


