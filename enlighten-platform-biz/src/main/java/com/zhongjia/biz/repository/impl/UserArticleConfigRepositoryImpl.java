package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.UserArticleConfig;
import com.zhongjia.biz.mapper.UserArticleConfigMapper;
import com.zhongjia.biz.repository.UserArticleConfigRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserArticleConfigRepositoryImpl extends ServiceImpl<UserArticleConfigMapper, UserArticleConfig> implements UserArticleConfigRepository {
}


