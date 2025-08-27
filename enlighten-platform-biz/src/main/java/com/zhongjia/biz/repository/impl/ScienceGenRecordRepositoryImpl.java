package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.ScienceGenRecord;
import com.zhongjia.biz.mapper.ScienceGenRecordMapper;
import com.zhongjia.biz.repository.ScienceGenRecordRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ScienceGenRecordRepositoryImpl extends ServiceImpl<ScienceGenRecordMapper, ScienceGenRecord> implements ScienceGenRecordRepository {
    @Override
    public ScienceGenRecord getByCode(String code) {
        return lambdaQuery().eq(ScienceGenRecord::getCode, code).one();
    }
}


