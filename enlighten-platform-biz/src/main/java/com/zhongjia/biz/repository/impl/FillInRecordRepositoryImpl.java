package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.FillInRecord;
import com.zhongjia.biz.mapper.FillInRecordMapper;
import com.zhongjia.biz.repository.FillInRecordRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FillInRecordRepositoryImpl extends ServiceImpl<FillInRecordMapper, FillInRecord> implements FillInRecordRepository {
}


