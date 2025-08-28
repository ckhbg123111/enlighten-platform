package com.zhongjia.web.filter;

import com.zhongjia.web.security.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class TraceIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final int MAX_LOG_BODY = 2048; // 防止日志过大

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        long startNs = System.nanoTime();
        String incoming = request.getHeader(TRACE_ID_HEADER);
        String traceId = (incoming != null && !incoming.isEmpty()) ? incoming : generateTraceId();
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String ua = request.getHeader("User-Agent");
        String remote = request.getRemoteAddr();

        // 包装请求与响应，便于读取body（SSE场景仅包装请求，避免影响流式返回）
        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);
        boolean sse = isSseRequest(request);
        HttpServletResponse effectiveResponse = response;
        ContentCachingResponseWrapper cachingResponse = null;
        if (!sse) {
            cachingResponse = new ContentCachingResponseWrapper(response);
            effectiveResponse = cachingResponse;
        }

        log.info("REQ {} {}{} from={} ua={}", method, uri, (query == null ? "" : ("?" + query)), remote, ua);
        try {
            filterChain.doFilter(cachingRequest, effectiveResponse);
        } finally {
            long costMs = (System.nanoTime() - startNs) / 1_000_000L;
            UserContext.UserInfo user = UserContext.get();
            // 记录请求体
            String reqBodyLog = buildRequestLog(cachingRequest);
            if (reqBodyLog != null && !reqBodyLog.isEmpty()) {
                log.info("REQ_BODY {} {} => {}", method, uri, toSingleLine(reqBodyLog));
            }

            // 记录响应体（SSE不记录body）
            if (!sse && cachingResponse != null) {
                String respBodyLog = buildResponseLog(cachingResponse);
                if (respBodyLog != null && !respBodyLog.isEmpty()) {
                    log.info("RESP_BODY {} {} -> {}", method, uri, toSingleLine(respBodyLog));
                }
            }

            if (user != null) {
                log.info("RESP {} {} -> status={} costMs={} userId={} tenantId={}", method, uri, response.getStatus(), costMs, user.userId(), user.tenantId());
            } else {
                log.info("RESP {} {} -> status={} costMs={}", method, uri, response.getStatus(), costMs);
            }
            if (!sse && cachingResponse != null) {
                // 将缓存的响应体写回客户端
                cachingResponse.copyBodyToResponse();
            }
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }

    private static String generateTraceId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid.length() > 16 ? uuid.substring(0, 16) : uuid;
    }

    private boolean isSseRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/event-stream")) return true;
        String uri = request.getRequestURI();
        return uri.startsWith("/api/fill_in") || uri.startsWith("/api/science-generator") || uri.startsWith("/api/science-chat");
    }

    private String buildRequestLog(ContentCachingRequestWrapper request) {
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("multipart/")) {
            return "[multipart omitted]";
        }
        byte[] body = request.getContentAsByteArray();
        if (body == null || body.length == 0) {
            // 回退到参数表
            if (request.getParameterMap() == null || request.getParameterMap().isEmpty()) return "";
            StringBuilder sb = new StringBuilder(128).append("{");
            boolean first = true;
            for (java.util.Map.Entry<String, String[]> e : request.getParameterMap().entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(safe(e.getKey())).append('"').append(':');
                String[] vs = e.getValue();
                if (vs == null) {
                    sb.append("null");
                } else if (vs.length == 1) {
                    sb.append('"').append(escapeJson(safe(vs[0]))).append('"');
                } else {
                    sb.append('[');
                    for (int i = 0; i < vs.length; i++) {
                        if (i > 0) sb.append(',');
                        sb.append('"').append(escapeJson(safe(vs[i]))).append('"');
                    }
                    sb.append(']');
                }
            }
            sb.append('}');
            return truncate(sb.toString());
        }
        String raw = new String(body, java.nio.charset.StandardCharsets.UTF_8);
        return truncate(raw);
    }

    private String buildResponseLog(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();
        if (contentType != null && contentType.startsWith("text/event-stream")) {
            return "[sse omitted]";
        }
        if (contentType != null && contentType.startsWith("application/octet-stream")) {
            return "[binary omitted]";
        }
        byte[] body = response.getContentAsByteArray();
        if (body == null || body.length == 0) return "";
        String raw = new String(body, java.nio.charset.StandardCharsets.UTF_8);
        return truncate(raw);
    }

    private static String truncate(String s) {
        if (s == null) return "";
        if (s.length() <= MAX_LOG_BODY) return s;
        return s.substring(0, MAX_LOG_BODY) + "...[truncated]";
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String toSingleLine(String s) {
        if (s == null) return "";
        String tmp = s.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
        return tmp.replaceAll(" {2,}", " ").trim();
    }
}


