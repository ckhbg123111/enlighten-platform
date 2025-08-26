package com.zhongjia.web.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * RocketMQ配置类
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RocketMQProperties.class)
public class RocketMQConfig {
    
    @Autowired
    private RocketMQProperties rocketMQProperties;
    
    @PostConstruct
    public void init() {
        log.info("RocketMQ配置初始化完成:");
        log.info("  NameServer: {}", rocketMQProperties.getNameServer());
        log.info("  Producer Group: {}", rocketMQProperties.getProducer().getGroup());
        log.info("  Send Message Timeout: {}ms", rocketMQProperties.getProducer().getSendMessageTimeout());
        log.info("  Retry Times: {}", rocketMQProperties.getProducer().getRetryTimesWhenSendFailed());
        
        // 可以在这里添加更多的RocketMQ配置
    }
}
