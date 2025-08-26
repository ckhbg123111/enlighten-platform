package com.zhongjia.biz.service;

import com.zhongjia.biz.entity.VideoGenerationTask;
import com.zhongjia.biz.repository.VideoGenerationTaskRepository;
import com.zhongjia.biz.service.mq.VideoTaskProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 基于MQ的视频生成服务
 * 这个服务替代原来的VideoGenerationService，专门处理MQ集成
 */
@Slf4j
@Service
public class VideoGenerationMQService {
    
    @Autowired
    private VideoGenerationTaskRepository taskRepository;
    
    @Autowired
    private VideoTaskProducer videoTaskProducer;
    
    /**
     * 创建视频生成任务并发送到MQ
     */
    public String createTaskWithMQ(Long userId, String inputText, String modelName, String voice) {
        log.info("创建视频生成任务(MQ版本) - 用户: {}, 文本长度: {}", userId, inputText.length());
        
        // 创建任务记录
        VideoGenerationTask task = new VideoGenerationTask()
                .setUserId(userId)
                .setInputText(inputText)
                .setModelName(modelName)
                .setVoice(voice != null ? voice : "Female_Voice_1")
                .setStatus("CREATED")
                .setProgress(0)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());
        
        // 保存到数据库
        taskRepository.save(task);
        
        // 发送消息到MQ开始处理
        try {
            videoTaskProducer.sendProcessDhMessage(task.getId(), userId);
            log.info("视频生成任务创建成功并发送到MQ - 任务ID: {}", task.getId());
        } catch (Exception e) {
            log.error("发送任务消息到MQ失败，标记任务为失败 - 任务ID: {}", task.getId(), e);
            task.setStatus("FAILED").setErrorMessage("发送消息到MQ失败: " + e.getMessage());
            taskRepository.updateById(task);
            throw new RuntimeException("创建任务失败", e);
        }
        
        return task.getId();
    }
    
    /**
     * 手动重试任务 (管理员功能)
     */
    public void retryTask(String taskId) {
        VideoGenerationTask task = taskRepository.getById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        
        log.info("手动重试任务 - 任务ID: {}, 当前状态: {}", taskId, task.getStatus());
        
        try {
            switch (task.getStatus()) {
                case "CREATED":
                    videoTaskProducer.sendProcessDhMessage(taskId, task.getUserId());
                    break;
                case "DH_PROCESSING":
                    videoTaskProducer.sendPollDhMessage(taskId, task.getUserId(), 0);
                    break;
                case "DH_DONE":
                    videoTaskProducer.sendProcessBurnMessage(taskId, task.getUserId());
                    break;
                case "BURN_PROCESSING":
                    videoTaskProducer.sendPollBurnMessage(taskId, task.getUserId(), 0);
                    break;
                case "FAILED":
                    // 重置状态并重新开始
                    task.setStatus("CREATED").setErrorMessage(null).setProgress(0).setUpdatedAt(LocalDateTime.now());
                    taskRepository.updateById(task);
                    videoTaskProducer.sendProcessDhMessage(taskId, task.getUserId());
                    break;
                case "COMPLETED":
                    log.warn("任务已完成，无需重试 - 任务ID: {}", taskId);
                    return;
                default:
                    throw new IllegalStateException("不支持重试的任务状态: " + task.getStatus());
            }
            
            log.info("任务重试消息发送成功 - 任务ID: {}", taskId);
            
        } catch (Exception e) {
            log.error("重试任务失败 - 任务ID: {}", taskId, e);
            throw new RuntimeException("重试任务失败", e);
        }
    }
    
    /**
     * 批量重试失败的任务 (管理员功能)
     */
    public void retryFailedTasks(int maxCount) {
        log.info("开始批量重试失败任务，最大数量: {}", maxCount);
        
        try {
            var failedTasks = taskRepository.findPendingTasks("FAILED", maxCount);
            
            for (VideoGenerationTask task : failedTasks) {
                try {
                    retryTask(task.getId());
                    // 添加延迟避免MQ消息过于密集
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("重试失败任务异常 - 任务ID: {}", task.getId(), e);
                }
            }
            
            log.info("批量重试失败任务完成，处理数量: {}", failedTasks.size());
            
        } catch (Exception e) {
            log.error("批量重试失败任务异常", e);
            throw new RuntimeException("批量重试失败", e);
        }
    }
    
    /**
     * 获取任务统计信息
     */
    public TaskStatistics getTaskStatistics() {
        try {
            TaskStatistics stats = new TaskStatistics();
            
            // 这里可以实现具体的统计逻辑
            // 比如查询各种状态的任务数量
            stats.createdCount = taskRepository.findPendingTasks("CREATED", Integer.MAX_VALUE).size();
            stats.dhProcessingCount = taskRepository.findPendingTasks("DH_PROCESSING", Integer.MAX_VALUE).size();
            stats.dhDoneCount = taskRepository.findPendingTasks("DH_DONE", Integer.MAX_VALUE).size();
            stats.burnProcessingCount = taskRepository.findPendingTasks("BURN_PROCESSING", Integer.MAX_VALUE).size();
            stats.completedCount = taskRepository.findPendingTasks("COMPLETED", Integer.MAX_VALUE).size();
            stats.failedCount = taskRepository.findPendingTasks("FAILED", Integer.MAX_VALUE).size();
            
            return stats;
            
        } catch (Exception e) {
            log.error("获取任务统计信息异常", e);
            throw new RuntimeException("获取统计信息失败", e);
        }
    }
    
    /**
     * 任务统计信息
     */
    public static class TaskStatistics {
        public int createdCount;
        public int dhProcessingCount;
        public int dhDoneCount;
        public int burnProcessingCount;
        public int completedCount;
        public int failedCount;
        public LocalDateTime timestamp = LocalDateTime.now();
        
        public int getTotalCount() {
            return createdCount + dhProcessingCount + dhDoneCount + burnProcessingCount + completedCount + failedCount;
        }
        
        public double getSuccessRate() {
            int total = getTotalCount();
            return total > 0 ? (double) completedCount / total * 100 : 0.0;
        }
    }
}
