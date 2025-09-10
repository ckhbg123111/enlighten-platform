package com.zhongjia.biz.service;

import com.zhongjia.biz.service.dto.UpstreamResult;

import java.util.List;

public interface DhModelService {

    /**
     * 获取用户可用的数字人模型列表（合并上游、默认配置与用户自训练）
     */
    List<String> listModelsForUser(Long userId);

    /**
     * 获取数字人模型详细信息列表（数据来自上游三方接口）。
     * 返回值为上游提供的对象列表（字段保持与上游一致）。
     */
    List<java.util.Map<String, Object>> listModelDetailsForUser(Long userId);

    /**
     * 转发训练请求到上游，成功后记录用户-模型映射。
     * @param userId 用户ID
     * @param requestJson 原始请求JSON（字段需与上游严格一致）
     * @return 上游通用结果
     */
    UpstreamResult trainAndRecord(Long userId, String requestJson);
}


