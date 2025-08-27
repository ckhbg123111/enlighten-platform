package com.zhongjia.biz.rocketmq;

import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * 运行本测试以重置 RocketMQ 指定消费组在指定 Topic 上的消费位点，从而跳过旧消息。
 *
 * 使用 System Properties 传参（IDE 运行或 Maven 命令行 -D）：
 * -DnamesrvAddr=127.0.0.1:9876 -Dtopic=YOUR_TOPIC -Dgroup=video_task_consumer_group -Dtime=now
 * 可选：
 * -DincludeRetry=true  重置 %RETRY%<group>
 * -DincludeDlq=false   重置 %DLQ%<group>
 * -Dforce=true         强制重置（默认 true）
 * -Dtime=2025-08-27 12:00:00  指定时间
 */
public class RmqResetOffsetTest {

    @Test
    public void resetOffsets() throws Exception {
        String namesrvAddr = System.getProperty("namesrvAddr", "192.168.3.10:9876");
        String topic = "video_task_topic";
        String group = System.getProperty("group", "video_task_consumer_group");
        String time = System.getProperty("time", "now");
        boolean includeRetry = Boolean.parseBoolean(System.getProperty("includeRetry", "true"));
        boolean includeDlq = Boolean.parseBoolean(System.getProperty("includeDlq", "false"));
        boolean force = Boolean.parseBoolean(System.getProperty("force", "true"));

        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("请通过 -Dtopic=YOUR_TOPIC 指定要重置的 Topic");
        }

        long timestamp = parseTimestamp(time);

        DefaultMQAdminExt admin = new DefaultMQAdminExt(10_000);
        admin.setNamesrvAddr(namesrvAddr);
        admin.setInstanceName("resetOffset_" + System.currentTimeMillis());
        admin.start();
        try {
            resetTopic(admin, topic, group, timestamp, force);

            if (includeRetry) {
                String retryTopic = "%RETRY%" + group;
                resetTopic(admin, retryTopic, group, timestamp, force);
            }
            if (includeDlq) {
                String dlqTopic = "%DLQ%" + group;
                resetTopic(admin, dlqTopic, group, timestamp, force);
            }
        } finally {
            admin.shutdown();
        }
    }

    private static void resetTopic(DefaultMQAdminExt admin, String topic, String group, long timestamp, boolean force) throws Exception {
        System.out.println("[RESET] topic=" + topic + ", group=" + group + ", ts=" + timestamp + ", force=" + force);
        Map<MessageQueue, Long> result = admin.resetOffsetByTimestamp(topic, group, timestamp, force);
        System.out.println("[RESULT] size=" + (result == null ? 0 : result.size()));
        if (result != null) {
            for (Map.Entry<MessageQueue, Long> entry : result.entrySet()) {
                System.out.println("  MQ=" + entry.getKey() + " => offset=" + entry.getValue());
            }
        }
    }

    private static long parseTimestamp(String time) {
        if (time == null || time.isBlank() || "now".equalsIgnoreCase(time)) {
            return System.currentTimeMillis();
        }
        // 支持格式：yyyy-MM-dd HH:mm:ss
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = sdf.parse(time);
            if (date == null) {
                throw new IllegalArgumentException("无法解析 time: " + time);
            }
            return date.getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException("time 格式应为 'now' 或 'yyyy-MM-dd HH:mm:ss'，实际: " + time, e);
        }
    }
}


