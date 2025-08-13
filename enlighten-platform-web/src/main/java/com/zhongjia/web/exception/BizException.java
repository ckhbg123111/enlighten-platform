package com.zhongjia.web.exception;

/**
 * 业务异常
 */
public class BizException extends RuntimeException {
    private final int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}


