package com.zhongjia.biz.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhongjia.biz.entity.VideoGenerationTask;

import java.util.List;

/**
 * 视频生成任务 Repository 接口
 */
public interface VideoGenerationTaskRepository extends IService<VideoGenerationTask> {
    
    /**
     * 根据状态查询待处理的任务
     * @param status 任务状态
     * @param limit 限制条数
     * @return 任务列表
     */
    List<VideoGenerationTask> findPendingTasks(String status, int limit);
    
    /**
     * 根据数字人任务ID查询
     * @param dhTaskId 数字人任务ID
     * @return 任务对象
     */
    VideoGenerationTask findByDhTaskId(String dhTaskId);
    
    /**
     * 根据字幕烧录任务ID查询
     * @param burnTaskId 字幕烧录任务ID
     * @return 任务对象
     */
    VideoGenerationTask findByBurnTaskId(String burnTaskId);
    
    /**
     * 查询用户的任务列表
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 任务列表
     */
    List<VideoGenerationTask> findByUserAndTenant(Long userId, Long tenantId);
}
