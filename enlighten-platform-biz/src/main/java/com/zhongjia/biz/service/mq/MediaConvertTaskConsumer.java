package com.zhongjia.biz.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.biz.service.ArticleStructureService;
import com.zhongjia.biz.service.MediaConvertCancelService;
import com.zhongjia.biz.service.MediaConvertRecordV2Service;
import com.zhongjia.biz.service.TemplateApplyService;
import com.zhongjia.biz.service.dto.ArticleStructure;
import com.zhongjia.biz.service.dto.RenderResult;
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
    // 不再依赖数据库进行原文兜底

    @Override
    public void onMessage(String message) {
        try {
            MediaConvertTaskMessage msg = objectMapper.readValue(message, MediaConvertTaskMessage.class);
            Long v2Id = msg.getRecordV2Id();
            // 设定MDC traceId，便于下游带上请求头
            if (msg.getTraceId() != null && !msg.getTraceId().isEmpty()) {
                org.slf4j.MDC.put("traceId", msg.getTraceId());
            }

            // 消费前先检查是否被取消
            if (cancelService.isCancelled(v2Id)) {
                log.info("任务已被取消，标记为 INTERRUPTED，不执行。v2Id={}", v2Id);
                String original = msg.getEssay();
                recordV2Service.markInterrupted(v2Id, original);
                return;
            }

            // 统一获取原文，仅使用消息内容
            String original = msg.getEssay();
            if (original == null || original.isEmpty()) {
                recordV2Service.markFailed(v2Id, null);
                return;
            }

            // 解析
            ArticleStructure structure;
            try {
                structure = articleStructureService.parse(original);
            } catch (Exception parseEx) {
                log.warn("解析文章结构失败，标记为FAILED。v2Id={}", v2Id, parseEx);
                recordV2Service.markFailed(v2Id, original);
                return;
            }

            // 渲染
            String html;
            String title;
            try {
                RenderResult render = templateApplyService.render(msg.getTemplateId(), structure);
                html = render.getHtml();
                title = render.getTitle();
            } catch (Exception renderEx) {
                log.warn("模板渲染失败，标记为FAILED。v2Id={}", v2Id, renderEx);
                recordV2Service.markFailed(v2Id, original);
                return;
            }

            // 渲染完成后再次检查取消（处理10秒等耗时场景）
            if (cancelService.isCancelled(v2Id)) {
                log.info("任务在渲染后被取消，标记为 INTERRUPTED，不落库生成内容。v2Id={}", v2Id);
                recordV2Service.markInterrupted(v2Id, original);
                return;
            }

            // 只更新 v2 记录为 SUCCESS，并写入生成内容；不改 gzhArticle（由前端保存）
            recordV2Service.markSuccess(v2Id, original, html, title);
        } catch (Exception e) {
            log.error("处理媒体转换消息失败", e);
            try {
                MediaConvertTaskMessage msg = objectMapper.readValue(message, MediaConvertTaskMessage.class);
                Long v2Id = msg.getRecordV2Id();
                String original = msg.getEssay();
                if (v2Id != null) {
                    recordV2Service.markFailed(v2Id, original);
                }
            } catch (Exception ignore) {
                // 忽略兜底失败
            }
        } finally {
            // 清理MDC，避免线程复用污染
            org.slf4j.MDC.remove("traceId");
        }
    }
}


