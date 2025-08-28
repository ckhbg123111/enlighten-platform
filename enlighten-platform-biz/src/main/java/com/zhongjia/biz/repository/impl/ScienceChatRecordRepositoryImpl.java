package com.zhongjia.biz.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongjia.biz.entity.ScienceChatRecord;
import com.zhongjia.biz.mapper.ScienceChatRecordMapper;
import com.zhongjia.biz.repository.ScienceChatRecordRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ScienceChatRecordRepositoryImpl extends ServiceImpl<ScienceChatRecordMapper, ScienceChatRecord>
        implements ScienceChatRecordRepository {
}


