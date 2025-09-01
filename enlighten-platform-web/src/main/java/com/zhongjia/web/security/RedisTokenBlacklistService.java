package com.zhongjia.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的Token黑名单服务
 * 支持分布式集群部署
 */
@Service
@ConditionalOnProperty(name = "app.token-blacklist.type", havingValue = "redis", matchIfMissing = false)
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 将token添加到黑名单
     * @param token JWT token
     * @param expireTime token的过期时间戳
     */
    @Override
    public void addToBlacklist(String token, long expireTime) {
        String key = BLACKLIST_KEY_PREFIX + token;
        long ttl = Math.max(0, expireTime - System.currentTimeMillis());
        
        if (ttl > 0) {
            // 设置Redis键值，TTL为token的剩余有效时间
            redisTemplate.opsForValue().set(key, expireTime, ttl, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 检查token是否在黑名单中
     * @param token JWT token
     * @return true表示在黑名单中，false表示不在
     */
    @Override
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取当前黑名单中的token数量（主要用于监控）
     * @return 黑名单token数量
     */
    @Override
    public int getBlacklistSize() {
        // Redis中统计所有黑名单键的数量
        return Math.toIntExact(redisTemplate.keys(BLACKLIST_KEY_PREFIX + "*").size());
    }
}
