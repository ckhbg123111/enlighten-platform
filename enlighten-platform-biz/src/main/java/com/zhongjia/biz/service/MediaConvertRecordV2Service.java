package com.zhongjia.biz.service;

public interface MediaConvertRecordV2Service {
    /** 插入或更新为 PROCESSING，返回记录ID */
    Long insertOrUpdateProcessing(Long userId, Long externalId, String platform);

    /** 将记录更新为成功 */
    boolean markSuccess(Long id);

    /** 将记录更新为失败 */
    boolean markFailed(Long id);
}


