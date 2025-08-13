package com.zhongjia.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("science_gen_record")
public class ScienceGenRecord {
	@TableId(type = IdType.AUTO)
	private Long id;

	private Long userId;

	private Long tenantId;

	/** 唯一编码（由调用方提供） */
	private String code;

	/** 上游请求体（不包含 code），原始 JSON 字符串 */
	private String reqBody;

	/** 上游 SSE 拼接后的完整内容输出 */
	private String respContent;

	/** 是否成功 */
	private Boolean success;

	/** 失败时的错误信息 */
	private String errorMessage;

	private LocalDateTime createTime;
}


