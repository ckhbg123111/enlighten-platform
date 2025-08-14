package com.zhongjia.web.vo;

import lombok.Data;
import com.zhongjia.web.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 统一响应结果类
 */
@Data
@Schema(name = "Result", description = "统一响应包装")
public class Result<T> {
    
    @Schema(description = "业务状态码", example = "200")
    private Integer code;
    @Schema(description = "提示信息", example = "操作成功")
    private String message;
    @Schema(description = "业务数据")
    private T data;
    
    public static <T> Result<T> success() {
        return success(null);
    }
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getDefaultMessage());
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(message);
        return result;
    }
}
