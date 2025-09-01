package com.zhongjia.web.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于本地内存的Token黑名单服务
 * 仅适用于单机部署，不支持集群
 */
@Service
@ConditionalOnProperty(name = "app.token-blacklist.type", havingValue = "local", matchIfMissing = true)
public class LocalTokenBlacklistService implements TokenBlacklistService {

    // 使用ConcurrentHashMap存储黑名单token，key为token，value为过期时间戳
    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    
    // 定时清理过期的黑名单token
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public LocalTokenBlacklistService() {
        // 每小时清理一次过期的黑名单token
        scheduler.scheduleAtFixedRate(this::cleanExpiredTokens, 1, 1, TimeUnit.HOURS);
    }

    /**
     * 将token添加到黑名单
     * @param token JWT token
     * @param expireTime token的过期时间戳
     */
    @Override
    public void addToBlacklist(String token, long expireTime) {
        blacklistedTokens.put(token, expireTime);
    }

    /**
     * 检查token是否在黑名单中
     * @param token JWT token
     * @return true表示在黑名单中，false表示不在
     */
    @Override
    public boolean isBlacklisted(String token) {
        Long expireTime = blacklistedTokens.get(token);
        if (expireTime == null) {
            return false;
        }
        
        // 如果token已过期，从黑名单中移除并返回false
        if (System.currentTimeMillis() > expireTime) {
            blacklistedTokens.remove(token);
            return false;
        }
        
        return true;
    }

    /**
     * 清理过期的黑名单token
     */
    private void cleanExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> currentTime > entry.getValue());
    }

    /**
     * 获取当前黑名单中的token数量（主要用于监控）
     * @return 黑名单token数量
     */
    @Override
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}
