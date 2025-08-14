package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("draft_media_map")
public class DraftMediaMap {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long tenantId;

    private Long draftId;

    private String mediaCode;

    private String tag;

    /** 逻辑删除：0-未删除，1-已删除 */
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime deleteTime;
}



