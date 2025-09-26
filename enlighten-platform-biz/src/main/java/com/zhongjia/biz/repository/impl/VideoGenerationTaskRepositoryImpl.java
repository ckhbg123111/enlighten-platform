package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.VideoGenerationTask;
import com.zhongjia.biz.mapper.VideoGenerationTaskMapper;
import com.zhongjia.biz.repository.VideoGenerationTaskRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 视频生成任务 Repository 实现类
 */
@Repository
public class VideoGenerationTaskRepositoryImpl extends ServiceImpl<VideoGenerationTaskMapper, VideoGenerationTask> 
        implements VideoGenerationTaskRepository {

    @Override
    public List<VideoGenerationTask> findPendingTasks(String status, int limit) {
        return baseMapper.selectPendingTasks(status, limit);
    }

    @Override
    public VideoGenerationTask findByDhTaskId(String dhTaskId) {
        return baseMapper.selectByDhTaskId(dhTaskId);
    }

    @Override
    public VideoGenerationTask findByBurnTaskId(String burnTaskId) {
        return baseMapper.selectByBurnTaskId(burnTaskId);
    }

    @Override
    public List<VideoGenerationTask> findByUser(Long userId) {
        return baseMapper.selectByUser(userId);
    }

    @Override
    public List<VideoGenerationTask> findByUserAndStatuses(Long userId, java.util.List<String> statuses) {
        return baseMapper.selectByUserAndStatuses(userId, statuses);
    }
}
