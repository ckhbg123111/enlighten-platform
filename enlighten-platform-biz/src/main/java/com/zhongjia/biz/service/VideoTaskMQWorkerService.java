package com.zhongjia.biz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.biz.entity.VideoGenerationTask;
import com.zhongjia.biz.repository.VideoGenerationTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 基于MQ的视频任务处理器 (示例实现)
 * 这是一个演示如何使用MQ改造现有架构的示例
 */
@Slf4j
@Service
public class VideoTaskMQWorkerService {
    
    @Autowired
    private VideoGenerationTaskRepository taskRepository;
    
    @Autowired
    private VideoGenerationService videoGenerationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * MQ消息结构
     */
    public static class TaskMessage {
        public String taskId;
        public String action; // PROCESS_DH, POLL_DH, PROCESS_BURN, POLL_BURN
        public long delaySeconds; // 延迟执行时间(用于轮询)
        
        public TaskMessage() {}
        
        public TaskMessage(String taskId, String action, long delaySeconds) {
            this.taskId = taskId;
            this.action = action;
            this.delaySeconds = delaySeconds;
        }
    }
    
    /**
     * 发送任务到MQ (需要集成具体的MQ实现)
     */
    public void sendTaskMessage(String taskId, String action, long delaySeconds) {
        try {
            TaskMessage message = new TaskMessage(taskId, action, delaySeconds);
            String messageBody = objectMapper.writeValueAsString(message);
            
            // 这里需要集成具体的MQ发送逻辑
            // 例如：rocketMQTemplate.sendDelayMessage("video_task_topic", messageBody, delaySeconds);
            
            log.info("发送任务消息到MQ - 任务ID: {}, 动作: {}, 延迟: {}秒", taskId, action, delaySeconds);
            
        } catch (Exception e) {
            log.error("发送任务消息到MQ失败 - 任务ID: {}", taskId, e);
        }
    }
    
    /**
     * 处理MQ消息 (消费者方法)
     * 需要添加MQ消费者注解，例如：@RocketMQMessageListener
     */
    // @RocketMQMessageListener(topic = "video_task_topic", consumerGroup = "video_worker_group")
    public void handleTaskMessage(String messageBody) {
        try {
            TaskMessage message = objectMapper.readValue(messageBody, TaskMessage.class);
            VideoGenerationTask task = taskRepository.getById(message.taskId);
            
            if (task == null) {
                log.warn("任务不存在，跳过处理: {}", message.taskId);
                return;
            }
            
            log.info("处理MQ任务消息 - 任务ID: {}, 动作: {}", message.taskId, message.action);
            
            switch (message.action) {
                case "PROCESS_DH":
                    processDigitalHumanPhase(task);
                    break;
                case "POLL_DH":
                    pollDigitalHumanStatus(task);
                    break;
                case "PROCESS_BURN":
                    processBurnPhase(task);
                    break;
                case "POLL_BURN":
                    pollBurnStatus(task);
                    break;
                default:
                    log.warn("未知的任务动作: {}", message.action);
            }
            
        } catch (Exception e) {
            log.error("处理MQ任务消息异常: {}", messageBody, e);
            // 这里可以实现重试逻辑或死信队列处理
        }
    }
    
    private void processDigitalHumanPhase(VideoGenerationTask task) {
        try {
            videoGenerationService.processDhPhase(task);
            // 处理完成后，发送轮询消息
            sendTaskMessage(task.getId(), "POLL_DH", 5); // 5秒后开始轮询
        } catch (Exception e) {
            log.error("处理数字人阶段失败 - 任务ID: {}", task.getId(), e);
            videoGenerationService.updateTaskStatus(task, "FAILED", 0, "数字人阶段处理失败: " + e.getMessage());
        }
    }
    
    private void pollDigitalHumanStatus(VideoGenerationTask task) {
        try {
            boolean completed = videoGenerationService.pollDhStatus(task);
            
            if (completed) {
                if ("DH_DONE".equals(task.getStatus())) {
                    // 数字人完成，开始字幕烧录
                    sendTaskMessage(task.getId(), "PROCESS_BURN", 0);
                } else if ("FAILED".equals(task.getStatus())) {
                    log.error("数字人任务失败 - 任务ID: {}", task.getId());
                }
            } else {
                // 继续轮询
                sendTaskMessage(task.getId(), "POLL_DH", 5);
            }
        } catch (Exception e) {
            log.error("轮询数字人状态异常 - 任务ID: {}", task.getId(), e);
            // 网络异常继续重试
            sendTaskMessage(task.getId(), "POLL_DH", 10); // 延长间隔重试
        }
    }
    
    private void processBurnPhase(VideoGenerationTask task) {
        try {
            videoGenerationService.processBurnPhase(task);
            // 处理完成后，发送轮询消息
            sendTaskMessage(task.getId(), "POLL_BURN", 5);
        } catch (Exception e) {
            log.error("处理字幕烧录阶段失败 - 任务ID: {}", task.getId(), e);
            videoGenerationService.updateTaskStatus(task, "FAILED", 60, "字幕烧录阶段处理失败: " + e.getMessage());
        }
    }
    
    private void pollBurnStatus(VideoGenerationTask task) {
        try {
            boolean completed = videoGenerationService.pollBurnStatus(task);
            
            if (completed) {
                if ("COMPLETED".equals(task.getStatus())) {
                    log.info("视频生成任务完全完成 - 任务ID: {}", task.getId());
                } else if ("FAILED".equals(task.getStatus())) {
                    log.error("字幕烧录任务失败 - 任务ID: {}", task.getId());
                }
            } else {
                // 继续轮询
                sendTaskMessage(task.getId(), "POLL_BURN", 5);
            }
        } catch (Exception e) {
            log.error("轮询字幕烧录状态异常 - 任务ID: {}", task.getId(), e);
            sendTaskMessage(task.getId(), "POLL_BURN", 10);
        }
    }
}
