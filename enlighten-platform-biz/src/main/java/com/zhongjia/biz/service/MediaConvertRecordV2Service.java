package com.zhongjia.biz.service;

import com.zhongjia.biz.entity.MediaConvertRecordV2;

public interface MediaConvertRecordV2Service {
    /** 插入或更新为 PROCESSING，返回记录实体 */
    MediaConvertRecordV2 insertOrUpdateProcessing(Long userId, Long externalId, String platform);

    /** 将记录更新为成功 */
    boolean markSuccess(Long id);

    /** 将记录更新为失败 */
    boolean markFailed(Long id);
}


