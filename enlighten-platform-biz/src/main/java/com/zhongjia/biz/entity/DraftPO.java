package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("draft")
public class DraftPO {
	@TableId(type = IdType.AUTO)
	private Long id;

	private Long userId;

	private Long tenantId;

	/** 生成文章编码（与科普生成关联） */
	private String essayCode;

	private String title;

	private String content;

	private String mediaIdListString;

	/** 逻辑删除标记：0-未删除，1-已删除 */
	private Integer deleted;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private LocalDateTime deleteTime;
}



