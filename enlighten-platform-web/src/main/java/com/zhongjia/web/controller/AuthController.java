package com.zhongjia.web.controller;

import com.zhongjia.biz.entity.User;
import com.zhongjia.biz.service.UserService;
import com.zhongjia.web.security.JwtUtil;
import com.zhongjia.web.security.TokenBlacklistService;
import com.zhongjia.web.security.UserContext;
import com.zhongjia.web.vo.Result;
import lombok.Data;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@Tag(name = "认证与登录")
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据用户名与密码登录，返回 JWT Token")
    public Result<Map<String, String>> login(@Validated @RequestBody LoginReq req) {
        User user = userService.getByUsername(req.getUsername());
        if (user == null) {
            return Result.error(401, "用户名或密码错误");
        }
        // 简单的MD5校验（示例）。生产建议使用BCrypt/Argon2。
        String reqPwd = md5(req.getPassword());
        if (!reqPwd.equals(user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error(403, "用户已禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getTenantId(), Map.of(
                "role", user.getRole()
        ));
        return Result.success(Map.of("token", token));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出登录", description = "退出当前用户登录状态", security = {@SecurityRequirement(name = "bearer-jwt")})
    public Result<String> logout(HttpServletRequest request) {
        UserContext.UserInfo userInfo = UserContext.get();
        if (userInfo != null) {
            // 记录退出日志（可选）
            System.out.println("用户 " + userInfo.username() + " (ID: " + userInfo.userId() + ") 已退出登录");
        }
        
        // 获取当前请求的token
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            // 获取token的过期时间
            long expireTime = jwtUtil.getExpirationTime(token);
            if (expireTime > 0) {
                // 将token添加到黑名单，防止被恶意重复使用
                tokenBlacklistService.addToBlacklist(token, expireTime);
            }
        }
        
        // 清理当前请求的用户上下文
        UserContext.clear();
        
        return Result.success("退出登录成功");
    }

    private String md5(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    @Data
    @Schema(name = "LoginReq", description = "登录请求")
    public static class LoginReq {
        @Schema(description = "用户名", example = "admin")
        private String username;
        @Schema(description = "密码(明文)", example = "123456")
        private String password;
    }
}


