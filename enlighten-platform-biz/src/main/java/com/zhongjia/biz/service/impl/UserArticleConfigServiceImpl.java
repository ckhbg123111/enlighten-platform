package com.zhongjia.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.UserArticleConfig;
import com.zhongjia.biz.mapper.UserArticleConfigMapper;
import com.zhongjia.biz.service.UserArticleConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserArticleConfigServiceImpl extends ServiceImpl<UserArticleConfigMapper, UserArticleConfig>
        implements UserArticleConfigService {

    @Override
    public List<UserArticleConfig> listByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<UserArticleConfig>()
                .eq(UserArticleConfig::getUserId, userId)
                .orderByAsc(UserArticleConfig::getCategory)
                .orderByAsc(UserArticleConfig::getSort)
                .orderByAsc(UserArticleConfig::getId));
    }

    @Override
    public List<UserArticleConfig> listByUserIdAndCategory(Long userId, String category) {
        return this.list(new LambdaQueryWrapper<UserArticleConfig>()
                .eq(UserArticleConfig::getUserId, userId)
                .eq(UserArticleConfig::getCategory, category)
                .orderByAsc(UserArticleConfig::getSort)
                .orderByAsc(UserArticleConfig::getId));
    }

    @Override
    public boolean removeByUserIdAndOptionCode(Long userId, String optionCode) {
        return this.remove(new LambdaQueryWrapper<UserArticleConfig>()
                .eq(UserArticleConfig::getUserId, userId)
                .eq(UserArticleConfig::getOptionCode, optionCode));
    }
}


