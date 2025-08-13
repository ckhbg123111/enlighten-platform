package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("media_convert_record")
public class MediaConvertRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private Long userId;

    private Long tenantId;

    /** 生成文章编码（与科普生成关联） */
    private String essayCode;

    /** 原文内容（HTML/Markdown/纯文本） */
    private String content;

    /** 媒体平台：xiaohongshu / douyin */
    private String platform;

	/** 上游返回：code */
	private Integer respCode;

	/** 上游返回：msg */
	private String respMsg;

	/** 上游返回：success */
	private Boolean respSuccess;

	/** 上游返回：data（原样JSON字符串） */
	private String respData;

    /** 是否成功 */
    private Boolean success;

    /** 失败时的错误信息 */
    private String errorMessage;

    /** 逻辑删除：0-未删除，1-已删除 */
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime deleteTime;
}


