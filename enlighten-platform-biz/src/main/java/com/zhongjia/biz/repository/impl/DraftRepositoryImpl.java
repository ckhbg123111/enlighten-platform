package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.DraftPO;
import com.zhongjia.biz.mapper.DraftMapper;
import com.zhongjia.biz.repository.DraftRepository;
import org.springframework.stereotype.Repository;

@Repository
public class DraftRepositoryImpl extends ServiceImpl<DraftMapper, DraftPO> implements DraftRepository {
}


