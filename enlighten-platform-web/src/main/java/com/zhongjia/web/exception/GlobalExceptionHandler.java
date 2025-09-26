package com.zhongjia.web.exception;

import com.zhongjia.web.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.MDC;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Object handleBiz(BizException e, HttpServletRequest request) {
        try {
            log.warn("BizException uri={} code={} msg={} traceId={}",
                    request != null ? request.getRequestURI() : "-",
                    e.getCode(), e.getMessage(), MDC.get("traceId"));
        } catch (Throwable ignore) { }
        if (isSse(request)) {
            return sseError(e.getCode(), e.getMessage());
        }
        if (isConvert2Media(request)) {
            return convert2MediaError(e.getCode(), e.getMessage());
        }
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class,
            MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public Object handleBadRequest(Exception e, HttpServletRequest request) {
        String msg = firstMsg(e);
        try {
            log.warn("BadRequest {} msg={} uri={} traceId={}",
                    e.getClass().getSimpleName(), msg,
                    request != null ? request.getRequestURI() : "-",
                    MDC.get("traceId"));
        } catch (Throwable ignore) { }
        if (isSse(request)) {
            return sseError(ErrorCode.BAD_REQUEST.getCode(), msg);
        }
        if (isConvert2Media(request)) {
            return convert2MediaError(ErrorCode.BAD_REQUEST.getCode(), msg);
        }
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        try {
            log.warn("MethodNotAllowed method={} uri={} traceId={}",
                    e.getMethod(),
                    request != null ? request.getRequestURI() : "-",
                    MDC.get("traceId"));
        } catch (Throwable ignore) { }
        if (isSse(request)) {
            return sseError(ErrorCode.METHOD_NOT_ALLOWED.getCode(), e.getMessage());
        }
        if (isConvert2Media(request)) {
            return convert2MediaError(ErrorCode.METHOD_NOT_ALLOWED.getCode(), e.getMessage());
        }
        return Result.error(ErrorCode.METHOD_NOT_ALLOWED.getCode(), e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxUpload(MaxUploadSizeExceededException e, HttpServletRequest request) {
        long max = e.getMaxUploadSize();
        String msg = "上传文件过大，超过限制" + (max > 0 ? "（" + humanReadable(max) + "）" : "");
        try {
            log.warn("MaxUploadSizeExceeded uri={} max={} traceId={}",
                    request != null ? request.getRequestURI() : "-", max, MDC.get("traceId"));
        } catch (Throwable ignore) { }
        if (isSse(request)) {
            return sseError(ErrorCode.BAD_REQUEST.getCode(), msg);
        }
        if (isConvert2Media(request)) {
            return convert2MediaError(ErrorCode.BAD_REQUEST.getCode(), msg);
        }
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    public Object handleOther(Exception e, HttpServletRequest request) {
        try {
            log.error("Unhandled exception uri={} traceId={}",
                    request != null ? request.getRequestURI() : "-",
                    MDC.get("traceId"), e);
        } catch (Throwable ignore) { }
        if (isSse(request)) {
            return sseError(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        }
        if (isConvert2Media(request)) {
            return convert2MediaError(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getDefaultMessage());
        }
        return Result.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getDefaultMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNotFound(NoResourceFoundException e, HttpServletRequest request) {
        try {
            log.info("NotFound uri={} traceId={}",
                    request != null ? request.getRequestURI() : "-",
                    MDC.get("traceId"));
        } catch (Throwable ignore) { }
        if (isSse(request)) {
            return sseError(ErrorCode.NOT_FOUND.getCode(), ErrorCode.NOT_FOUND.getDefaultMessage());
        }
        if (isConvert2Media(request)) {
            return convert2MediaError(ErrorCode.NOT_FOUND.getCode(), ErrorCode.NOT_FOUND.getDefaultMessage());
        }
        return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON)
                .body(Result.error(ErrorCode.NOT_FOUND));
    }

    private String firstMsg(Exception e) {
        if (e instanceof MethodArgumentNotValidException manv) {
            if (manv.getBindingResult() != null && manv.getBindingResult().hasErrors()) {
                return manv.getBindingResult().getAllErrors().get(0).getDefaultMessage();
            }
        }
        if (e instanceof BindException be) {
            if (be.getBindingResult() != null && be.getBindingResult().hasErrors()) {
                return be.getBindingResult().getAllErrors().get(0).getDefaultMessage();
            }
        }
        if (e instanceof ConstraintViolationException cve) {
            if (!cve.getConstraintViolations().isEmpty()) {
                return cve.getConstraintViolations().iterator().next().getMessage();
            }
        }
        return ErrorCode.BAD_REQUEST.getDefaultMessage();
    }

    private boolean isConvert2Media(HttpServletRequest request) {
        if (request == null) return false;
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/convert2media/");
    }

    private java.util.Map<String, Object> convert2MediaError(int code, String msg) {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("code", code);
        m.put("success", false);
        m.put("msg", msg);
        m.put("data", null);
        return m;
    }

    private boolean isSse(HttpServletRequest request) {
        if (request == null) return false;
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        boolean acceptSse = accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
        boolean uriIsSseEndpoint = uri != null && uri.startsWith("/api/science-generator");
        return acceptSse || uriIsSseEndpoint;
    }

    private ResponseEntity<String> sseError(int code, String msg) {
        String json = "{\"code\":" + code + ",\"success\":false,\"msg\":\"" + safe(msg) + "\",\"data\":null}";
        String body = "event: error\n" + "data: " + json + "\n\n";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_EVENT_STREAM);
        return ResponseEntity.status(200).headers(headers).body(body);
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    private String humanReadable(long bytes) {
        if (bytes <= 0) return "0B";
        long kb = 1024L;
        long mb = kb * 1024L;
        long gb = mb * 1024L;
        if (bytes >= gb) return String.format(java.util.Locale.ROOT, "%.1fGB", bytes * 1.0 / gb);
        if (bytes >= mb) return String.format(java.util.Locale.ROOT, "%.0fMB", bytes * 1.0 / mb);
        if (bytes >= kb) return String.format(java.util.Locale.ROOT, "%.0fKB", bytes * 1.0 / kb);
        return bytes + "B";
    }
}


