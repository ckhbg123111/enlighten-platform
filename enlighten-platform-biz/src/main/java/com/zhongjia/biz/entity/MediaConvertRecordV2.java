package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("media_convert_record_v2")
public class MediaConvertRecordV2 {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 关联的外部ID（如 gzh_article.id） */
    private Long externalId;

    /** 媒体平台：gzh/xiaohongshu/douyin */
    private String platform;

    /** 状态：PROCESSING/SUCCESS/FAILED */
    private String status;

    /** 原文内容 */
    private String originalText;

    /** 生成内容 */
    private String generatedText;

    /** 逻辑删除：0-未删除，1-已删除 */
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime deleteTime;
}


