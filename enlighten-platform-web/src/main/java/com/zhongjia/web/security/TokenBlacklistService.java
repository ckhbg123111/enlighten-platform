package com.zhongjia.web.security;

/**
 * Token黑名单服务接口
 * 支持不同的实现方式：内存存储、Redis存储等
 */
public interface TokenBlacklistService {

    /**
     * 将token添加到黑名单
     * @param token JWT token
     * @param expireTime token的过期时间戳
     */
    void addToBlacklist(String token, long expireTime);

    /**
     * 检查token是否在黑名单中
     * @param token JWT token
     * @return true表示在黑名单中，false表示不在
     */
    boolean isBlacklisted(String token);

    /**
     * 获取当前黑名单中的token数量（主要用于监控）
     * @return 黑名单token数量
     */
    int getBlacklistSize();
}
