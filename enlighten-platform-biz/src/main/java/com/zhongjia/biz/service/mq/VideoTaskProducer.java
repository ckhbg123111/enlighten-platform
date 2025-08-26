package com.zhongjia.biz.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 视频任务消息生产者
 */
@Slf4j
@Service
public class VideoTaskProducer {
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Topic和Tag常量
    public static final String TOPIC_VIDEO_TASK = "video_task_topic";
    public static final String TAG_PROCESS = "PROCESS";
    public static final String TAG_POLL = "POLL";
    public static final String TAG_RETRY = "RETRY";
    
    /**
     * 发送立即执行的任务消息
     */
    public void sendTaskMessage(VideoTaskMessage message) {
        sendTaskMessage(message, 0);
    }
    
    /**
     * 发送延迟执行的任务消息
     */
    public void sendTaskMessage(VideoTaskMessage message, long delaySeconds) {
        try {
            message.setDelaySeconds(delaySeconds);
            String messageBody = objectMapper.writeValueAsString(message);
            String destination = buildDestination(message);
            String messageKey = message.generateMessageKey();
            
            if (delaySeconds > 0) {
                // 发送延迟消息 (RocketMQ支持最大18个等级的延迟)
                int delayLevel = calculateDelayLevel(delaySeconds);
                rocketMQTemplate.syncSend(destination, 
                    org.springframework.messaging.support.MessageBuilder.withPayload(messageBody)
                        .setHeader("KEYS", messageKey)
                        .build(), 
                    3000, delayLevel);
                log.info("发送延迟任务消息 - 任务ID: {}, 动作: {}, 延迟: {}秒, 等级: {}", 
                        message.getTaskId(), message.getAction(), delaySeconds, delayLevel);
            } else {
                // 发送立即执行消息
                rocketMQTemplate.syncSend(destination, 
                    org.springframework.messaging.support.MessageBuilder.withPayload(messageBody)
                        .setHeader("KEYS", messageKey)
                        .build());
                log.info("发送立即任务消息 - 任务ID: {}, 动作: {}", 
                        message.getTaskId(), message.getAction());
            }
            
        } catch (Exception e) {
            log.error("发送任务消息失败 - 任务ID: {}, 动作: {}", 
                    message.getTaskId(), message.getAction(), e);
            throw new RuntimeException("发送消息失败", e);
        }
    }
    
    /**
     * 发送数字人处理消息
     */
    public void sendProcessDhMessage(String taskId, Long userId) {
        VideoTaskMessage message = new VideoTaskMessage(taskId, VideoTaskMessage.ActionType.PROCESS_DH)
                .setUserId(userId)
                .setTag(TAG_PROCESS);
        sendTaskMessage(message);
    }
    
    /**
     * 发送数字人状态轮询消息
     */
    public void sendPollDhMessage(String taskId, Long userId, long delaySeconds) {
        VideoTaskMessage message = new VideoTaskMessage(taskId, VideoTaskMessage.ActionType.POLL_DH, delaySeconds)
                .setUserId(userId)
                .setTag(TAG_POLL);
        sendTaskMessage(message, delaySeconds);
    }
    
    /**
     * 发送字幕烧录处理消息
     */
    public void sendProcessBurnMessage(String taskId, Long userId) {
        VideoTaskMessage message = new VideoTaskMessage(taskId, VideoTaskMessage.ActionType.PROCESS_BURN)
                .setUserId(userId)
                .setTag(TAG_PROCESS);
        sendTaskMessage(message);
    }
    
    /**
     * 发送字幕烧录状态轮询消息
     */
    public void sendPollBurnMessage(String taskId, Long userId, long delaySeconds) {
        VideoTaskMessage message = new VideoTaskMessage(taskId, VideoTaskMessage.ActionType.POLL_BURN, delaySeconds)
                .setUserId(userId)
                .setTag(TAG_POLL);
        sendTaskMessage(message, delaySeconds);
    }
    
    /**
     * 发送重试消息
     */
    public void sendRetryMessage(VideoTaskMessage originalMessage) {
        originalMessage.incrementRetry();
        originalMessage.setTag(TAG_RETRY);
        
        if (originalMessage.isMaxRetriesExceeded()) {
            log.warn("任务超过最大重试次数，不再重试 - 任务ID: {}, 重试次数: {}", 
                    originalMessage.getTaskId(), originalMessage.getRetryCount());
            return;
        }
        
        // 指数退避重试，重试间隔逐渐增大
        long retryDelay = Math.min(300, (long) Math.pow(2, originalMessage.getRetryCount()) * 5); // 最大5分钟
        sendTaskMessage(originalMessage, retryDelay);
        
        log.info("发送重试消息 - 任务ID: {}, 重试次数: {}, 延迟: {}秒", 
                originalMessage.getTaskId(), originalMessage.getRetryCount(), retryDelay);
    }
    
    /**
     * 构建消息目标 (Topic:Tag)
     */
    private String buildDestination(VideoTaskMessage message) {
        return TOPIC_VIDEO_TASK + ":" + message.getTag();
    }
    
    /**
     * 计算RocketMQ延迟等级
     * RocketMQ延迟等级: 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     */
    private int calculateDelayLevel(long delaySeconds) {
        if (delaySeconds <= 1) return 1;        // 1s
        if (delaySeconds <= 5) return 2;        // 5s
        if (delaySeconds <= 10) return 3;       // 10s
        if (delaySeconds <= 30) return 4;       // 30s
        if (delaySeconds <= 60) return 5;       // 1m
        if (delaySeconds <= 120) return 6;      // 2m
        if (delaySeconds <= 180) return 7;      // 3m
        if (delaySeconds <= 240) return 8;      // 4m
        if (delaySeconds <= 300) return 9;      // 5m
        if (delaySeconds <= 360) return 10;     // 6m
        if (delaySeconds <= 420) return 11;     // 7m
        if (delaySeconds <= 480) return 12;     // 8m
        if (delaySeconds <= 540) return 13;     // 9m
        if (delaySeconds <= 600) return 14;     // 10m
        if (delaySeconds <= 1200) return 15;    // 20m
        if (delaySeconds <= 1800) return 16;    // 30m
        if (delaySeconds <= 3600) return 17;    // 1h
        return 18; // 2h (最大延迟)
    }
}
