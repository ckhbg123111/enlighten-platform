package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.GzhArticle;
import com.zhongjia.biz.mapper.GzhArticleMapper;
import com.zhongjia.biz.repository.GzhArticleRepository;
import org.springframework.stereotype.Repository;

@Repository
public class GzhArticleRepositoryImpl extends ServiceImpl<GzhArticleMapper, GzhArticle> implements GzhArticleRepository {
}


