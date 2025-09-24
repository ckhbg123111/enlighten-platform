package com.zhongjia.biz.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MediaConvertTaskProducer {

    public static final String TOPIC = "media_convert_topic";
    public static final String TAG_CONVERT = "APPLY_TEMPLATE";

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public void send(MediaConvertTaskMessage msg) {
        try {
            String payload = objectMapper.writeValueAsString(msg);
            rocketMQTemplate.syncSend(TOPIC + ":" + TAG_CONVERT, payload);
            log.info("发送媒体转换任务: v2Id={}, externalId={}, templateId={}", msg.getRecordV2Id(), msg.getExternalId(), msg.getTemplateId());
        } catch (Exception e) {
            log.error("发送媒体转换任务失败", e);
            throw new RuntimeException(e);
        }
    }
}


