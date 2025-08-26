package com.zhongjia.biz.service;

import com.zhongjia.biz.entity.VideoGenerationTask;

/**
 * 视频生成服务接口
 */
public interface VideoGenerationService {
    
    /**
     * 创建视频生成任务
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @param inputText 输入文本
     * @param modelName 数字人模型名称
     * @param voice 语音类型
     * @return 任务ID
     */
    String createTask(Long userId, String inputText, String modelName, String voice);
    
    /**
     * 查询任务状态
     * @param taskId 任务ID
     * @param userId 用户ID (用于权限校验)
     * @param tenantId 租户ID (用于权限校验)
     * @return 任务对象
     */
    VideoGenerationTask getTaskStatus(String taskId, Long userId);
    
    /**
     * 处理数字人阶段任务
     * @param task 任务对象
     */
    void processDhPhase(VideoGenerationTask task);
    
    /**
     * 处理字幕烧录阶段任务
     * @param task 任务对象
     */
    void processBurnPhase(VideoGenerationTask task);
    
    /**
     * 轮询数字人任务状态
     * @param task 任务对象
     * @return 是否完成
     */
    boolean pollDhStatus(VideoGenerationTask task);
    
    /**
     * 轮询字幕烧录任务状态
     * @param task 任务对象
     * @return 是否完成
     */
    boolean pollBurnStatus(VideoGenerationTask task);
    
    /**
     * 更新任务状态
     * @param task 任务对象
     * @param status 新状态
     * @param progress 进度
     * @param errorMessage 错误信息
     */
    void updateTaskStatus(VideoGenerationTask task, String status, Integer progress, String errorMessage);
}
