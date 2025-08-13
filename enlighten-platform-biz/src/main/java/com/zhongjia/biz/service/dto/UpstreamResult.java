package com.zhongjia.biz.service.dto;

/**
 * 通用上游调用结果，用于业务层返回给控制层，由控制层决定如何序列化输出
 */
public class UpstreamResult {
    private Integer code;
    private Boolean success;
    private String msg;
    /**
     * 上游返回的 data 字段的原始 JSON 字符串（已是 JSON 序列化后的文本，控制层需原样写入）
     */
    private String dataRaw;

    public Integer getCode() {
        return code;
    }

    public UpstreamResult setCode(Integer code) {
        this.code = code;
        return this;
    }

    public Boolean getSuccess() {
        return success;
    }

    public UpstreamResult setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public UpstreamResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getDataRaw() {
        return dataRaw;
    }

    public UpstreamResult setDataRaw(String dataRaw) {
        this.dataRaw = dataRaw;
        return this;
    }
}


