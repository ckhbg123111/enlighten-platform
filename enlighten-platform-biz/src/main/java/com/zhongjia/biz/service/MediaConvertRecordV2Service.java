package com.zhongjia.biz.service;

public interface MediaConvertRecordV2Service {
    /** 插入或更新为 PROCESSING，返回记录ID */
    Long insertProcessingRecord(Long userId, Long externalId, String platform);

    /** 将记录更新为成功，并写入原文与生成内容 */
    boolean markSuccess(Long id, String originalText, String generatedText);

    /** 将记录更新为失败，并写入原文 */
    boolean markFailed(Long id, String originalText);

    enum SoftDeleteResult {
        SUCCESS,
        NOT_FOUND,
        FORBIDDEN,
        ALREADY_DELETED,
        FAILED
    }

    /**
     * 按ID软删除记录：仅允许本人删除；返回结果标识
     */
    SoftDeleteResult softDeleteById(Long userId, Long id);

    /**
     * 分页查询当前用户的记录（可选按平台过滤），按更新时间倒序
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.zhongjia.biz.entity.MediaConvertRecordV2> pageRecords(
            Long userId, String platform, int page, int size);
}


