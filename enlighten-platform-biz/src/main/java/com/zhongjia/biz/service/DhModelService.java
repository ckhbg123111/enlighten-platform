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
     * 以 multipart/form-data 方式训练（上游规范）：file + model_name
     */
    UpstreamResult trainWithFile(Long userId, String modelName, org.springframework.web.multipart.MultipartFile file);
}


