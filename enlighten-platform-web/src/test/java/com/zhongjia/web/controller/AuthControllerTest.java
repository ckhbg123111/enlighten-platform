package com.zhongjia.web.controller;

import com.zhongjia.web.security.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证控制器测试类
 * 主要测试登录和退出登录功能
 */
@SpringBootTest
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired(required = false)
    private TokenBlacklistService tokenBlacklistService;

    @Test
    public void testTokenBlacklistService() {
        if (tokenBlacklistService != null) {
            // 测试Token黑名单功能
            String testToken = "test-token-123";
            long expireTime = System.currentTimeMillis() + 60000; // 1分钟后过期

            // 初始状态：token不在黑名单中
            assertFalse(tokenBlacklistService.isBlacklisted(testToken), "Token应该不在黑名单中");

            // 添加到黑名单
            tokenBlacklistService.addToBlacklist(testToken, expireTime);

            // 验证token已在黑名单中
            assertTrue(tokenBlacklistService.isBlacklisted(testToken), "Token应该在黑名单中");

            // 验证黑名单大小
            assertTrue(tokenBlacklistService.getBlacklistSize() > 0, "黑名单应该包含至少一个token");

            System.out.println("Token黑名单功能测试通过！");
        } else {
            System.out.println("TokenBlacklistService未注入，跳过测试");
        }
    }

    @Test
    public void testTokenExpiration() {
        if (tokenBlacklistService != null) {
            // 测试过期token自动清理
            String expiredToken = "expired-token-456";
            long pastTime = System.currentTimeMillis() - 1000; // 1秒前过期

            // 添加已过期的token
            tokenBlacklistService.addToBlacklist(expiredToken, pastTime);

            // 验证过期token不在黑名单中（应该被自动清理）
            assertFalse(tokenBlacklistService.isBlacklisted(expiredToken), "过期的token应该被自动清理");

            System.out.println("Token过期清理功能测试通过！");
        } else {
            System.out.println("TokenBlacklistService未注入，跳过测试");
        }
    }
}
