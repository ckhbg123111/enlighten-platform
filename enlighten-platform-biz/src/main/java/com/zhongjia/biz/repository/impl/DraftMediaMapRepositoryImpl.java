package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.DraftMediaMap;
import com.zhongjia.biz.mapper.DraftMediaMapMapper;
import com.zhongjia.biz.repository.DraftMediaMapRepository;
import org.springframework.stereotype.Repository;

@Repository
public class DraftMediaMapRepositoryImpl extends ServiceImpl<DraftMediaMapMapper, DraftMediaMap> implements DraftMediaMapRepository {
}



