package com.zhongjia.biz.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.biz.entity.GzhArticle;
import com.zhongjia.biz.service.ArticleStructureService;
import com.zhongjia.biz.service.GzhArticleService;
import com.zhongjia.biz.service.MediaConvertCancelService;
import com.zhongjia.biz.service.MediaConvertRecordV2Service;
import com.zhongjia.biz.service.TemplateApplyService;
import com.zhongjia.biz.service.dto.ArticleStructure;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RocketMQMessageListener(topic = MediaConvertTaskProducer.TOPIC, consumerGroup = "media_convert_worker", selectorExpression = MediaConvertTaskProducer.TAG_CONVERT)
public class MediaConvertTaskConsumer implements RocketMQListener<String> {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MediaConvertRecordV2Service recordV2Service;
    @Autowired
    private MediaConvertCancelService cancelService;
    @Autowired
    private ArticleStructureService articleStructureService;
    @Autowired
    private TemplateApplyService templateApplyService;
    @Autowired
    private GzhArticleService gzhArticleService;

    @Override
    public void onMessage(String message) {
        try {
            MediaConvertTaskMessage msg = objectMapper.readValue(message, MediaConvertTaskMessage.class);
            Long v2Id = msg.getRecordV2Id();

            // 消费前先检查是否被取消
            if (cancelService.isCancelled(v2Id)) {
                log.info("任务已被取消，标记为 INTERRUPTED，不执行。v2Id={}", v2Id);
                // 尽量保留原文：优先使用传入 essay，其次从文章记录获取
                String original = msg.getEssay();
                if (original == null) {
                    GzhArticle record = gzhArticleService.getById(msg.getExternalId());
                    if (record != null) original = record.getOriginalText();
                }
                recordV2Service.markInterrupted(v2Id, original);
                return;
            }

            // 执行渲染
            ArticleStructure structure;
            if (msg.getEssay() != null) {
                structure = articleStructureService.parse(msg.getEssay());
            } else {
                GzhArticle record = gzhArticleService.getById(msg.getExternalId());
                if (record == null) {
                    recordV2Service.markFailed(v2Id, null);
                    return;
                }
                if (cancelService.isCancelled(v2Id)) {
                    recordV2Service.markInterrupted(v2Id, record.getOriginalText());
                    return;
                }
                structure = articleStructureService.parse(record.getOriginalText());
            }

            String html = templateApplyService.render(msg.getTemplateId(), structure);

            // 渲染完成后再次检查取消（处理10秒等耗时场景）
            if (cancelService.isCancelled(v2Id)) {
                log.info("任务在渲染后被取消，标记为 INTERRUPTED，不落库生成内容。v2Id={}", v2Id);
                String original = msg.getEssay();
                if (original == null) {
                    GzhArticle record = gzhArticleService.getById(msg.getExternalId());
                    if (record != null) original = record.getOriginalText();
                }
                recordV2Service.markInterrupted(v2Id, original);
                return;
            }

            // 只更新 v2 记录为 SUCCESS，并写入生成内容；不改 gzhArticle（由前端保存）
            String original = msg.getEssay();
            if (original == null) {
                GzhArticle record = gzhArticleService.getById(msg.getExternalId());
                if (record != null) original = record.getOriginalText();
            }
            recordV2Service.markSuccess(v2Id, original, html);
        } catch (Exception e) {
            log.error("处理媒体转换消息失败", e);
        }
    }
}


