package com.zhongjia.biz.service.impl;

import com.zhongjia.biz.service.MediaConvertCancelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MediaConvertCancelServiceRedisImpl implements MediaConvertCancelService {

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Value("${convert.cancel.redis.keyPrefix:convert:cancel:}")
    private String keyPrefix;

    @Value("${convert.cancel.redis.ttlSeconds:3600}")
    private long ttlSeconds;

    private String key(Long id) { return keyPrefix + id; }

    @Override
    public boolean cancel(Long recordV2Id) {
        if (stringRedisTemplate == null) {
            return false;
        }
        stringRedisTemplate.opsForValue().set(key(recordV2Id), "1", Duration.ofSeconds(ttlSeconds));
        return true;
    }

    @Override
    public boolean isCancelled(Long recordV2Id) {
        if (stringRedisTemplate == null) {
            return false;
        }
        Boolean has = stringRedisTemplate.hasKey(key(recordV2Id));
        return Boolean.TRUE.equals(has);
    }
}


