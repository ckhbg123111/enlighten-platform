package com.zhongjia.web.security;

import com.zhongjia.web.vo.Result;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        // 登录接口放行、创建用户放行
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/user/create")) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtUtil.parse(token);
                Long userId = Long.valueOf(claims.getSubject());
                String username = claims.get("username", String.class);
                Long tenantId = claims.get("tenantId", Long.class);
                String role = claims.get("role", String.class);
                UserContext.set(new UserContext.UserInfo(userId, username, tenantId, role));
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    UserContext.clear();
                }
                return;
            } catch (Exception e) {
                writeJson(response, 401, Result.error(401, "无效的令牌"));
                return;
            }
        }

        writeJson(response, 401, Result.error(401, "未认证"));
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String json = "{\"code\":" + status + ",\"message\":\"未认证\"}";
        if (body instanceof Result<?> r) {
            json = "{\"code\":" + r.getCode() + ",\"message\":\"" + r.getMessage() + "\"}";
        }
        response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
    }
}


