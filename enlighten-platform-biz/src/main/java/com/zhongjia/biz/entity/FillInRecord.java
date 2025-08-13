package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("fill_in_record")
public class FillInRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long tenantId;

    /** 用户请求的 content 文本 */
    private String reqContent;

    /** 上游 SSE 拼接后的完整 content 文本（含 <FIELD:...><SEP> 标记） */
    private String respContent;

    /** 是否成功 */
    private Boolean success;

    /** 失败时的错误信息 */
    private String errorMessage;

    private LocalDateTime createTime;
}


