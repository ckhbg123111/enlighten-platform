package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("science_chat_record")
public class ScienceChatRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String sessionId;

    /** 请求携带的 messages 原始JSON */
    private String reqMessages;

    private Boolean needRecommend;

    private String prompt;

    /** 上游 SSE 拼接后的完整 assistant 文本 */
    private String respContent;

    private Boolean success;

    private String errorMessage;

    private LocalDateTime createTime;
}


