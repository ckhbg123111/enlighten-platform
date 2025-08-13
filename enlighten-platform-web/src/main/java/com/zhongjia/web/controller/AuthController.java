package com.zhongjia.web.controller;

import com.zhongjia.biz.entity.User;
import com.zhongjia.biz.service.UserService;
import com.zhongjia.web.security.JwtUtil;
import com.zhongjia.web.vo.Result;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
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

    private String md5(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    @Data
    public static class LoginReq {
        private String username;
        private String password;
    }
}


